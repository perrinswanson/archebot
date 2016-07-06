/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.utilities.Queue;
import com.archebot.utilities.StringUtils;

import java.io.*;
import java.net.Socket;

@SuppressWarnings("unchecked")
final class Connection {

    private final ArcheBot bot;
    private final Socket socket;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final Queue<String> outgoing = new Queue<>();
    private final Queue<String> incoming = new Queue<>();
    private HandlerThread handler = new HandlerThread();
    private boolean active = false;
    private int handlerThreads = 0;
    private int inputThreads = 0;
    private int outputThreads = 0;

    Connection(ArcheBot bot) throws IOException {
        this.bot = bot;
        Configuration configuration = bot.getConfiguration();

        String server = configuration.getString(Property.server);
        String password = configuration.getString(Property.password);
        int port = configuration.getInteger(Property.port);

        bot.log("Attempting to connect to %s on port %d...", server, port);
        socket = new Socket(server, port);
        bot.log("Connection successful!");
        bot.setState(State.connecting);
        active = true;

        socket.setSoTimeout(configuration.getInteger(Property.timeoutDelay));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String nick = configuration.getString(Property.nick);
        String login = configuration.getString(Property.login);
        boolean visible = configuration.getBoolean(Property.visible);
        String realname = configuration.getString(Property.realname);
        if (!password.isEmpty())
            send("PASS " + password);
        send("NICK " + nick);
        send(String.format("USER %s %d * :%s", login, visible ? 0 : 8, realname));

        UserMap userMap = bot.getUserMap();
        userMap.removeUser(bot.getNick());
        bot.setNick(nick);
        userMap.addUser(bot);

        handler.start();
        new OutputThread().start();
        new InputThread().start();
    }

    void breakThread() {
        handler.current = false;
        handler = new HandlerThread();
        handler.start();
    }

    void close() {
        active = false;
        try {
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            bot.logError("[Connection::close] An internal exception has occurred (%s)", e.getMessage());
        }
    }

    synchronized void queue(String line) {
        if (line != null && !line.isEmpty())
            outgoing.add(line);
    }

    synchronized void send(String line) {
        Configuration configuration = bot.getConfiguration();
        if (line != null && !line.isEmpty()) {
            int size = configuration.getInteger(Property.lineLength);
            if (line.length() > size)
                line = line.substring(0, size);
            try {
                if (configuration.getBoolean(Property.enableFormatting))
                    line = line.replace("\\&", "\0")
                            .replace("&r", "\17")
                            .replace("&b", "\2")
                            .replace("&", "\3")
                            .replace("\0", "&");
                writer.write(line);
                writer.newLine();
                writer.flush();
                if (configuration.getBoolean(Property.logOutput))
                    bot.logOutput(line.replaceAll("\\002|\\003\\d\\d?(,\\d\\d?)?|\\017", ""));
            } catch (IOException e) {
                bot.logError("[Connection::send] An internal exception has occurred (%s)", e.getMessage());
            }
        }
    }

    private void handle(String line) {
        Configuration configuration = bot.getConfiguration();
        UserMap userMap = bot.getUserMap();
        if (line.charAt(0) == ':')
            line = line.substring(1);
        else
            line = (bot.getServer() == null ? "" : bot.getServer().getName()) + " " + line;
        String[] parts = line.split(" :", 2);
        String[] lineArgs = parts[0].split(" ");
        String tail = parts.length > 1 ? parts[1] : "";
        User source = userMap.getUser(lineArgs[0]);
        String command = lineArgs[1];
        String[] args = new String[lineArgs.length - 2];
        System.arraycopy(lineArgs, 2, args, 0, args.length);
        switch (command.toUpperCase()) {
            case "ERROR": onError(tail); return;
            case "INVITE": onInvite(source, args, tail); break;
            case "JOIN": onJoin(source, args, tail); break;
            case "KICK": onKick(source, args, tail); break;
            case "MODE": onMode(source, args, tail); break;
            case "NICK": onNick(source, args, tail); break;
            case "NOTICE": onNotice(source, args, tail); break;
            case "PART": onPart(source, args, tail); break;
            case "PING": onPing(tail); break;
            case "PONG": onPong(args, tail); break;
            case "PRIVMSG": onPrivmsg(source, args, tail); break;
            case "QUIT": onQuit(source, tail); break;
            case "TOPIC": onTopic(source, args, tail); break;
            case "001": on001(); break;
            case "004": on004(args); break;
            case "005":
            case "105": on005(lineArgs[0], args); break;
            case "311": on311(args, tail); break;
            case "312": on312(args, tail); break;
            case "318": on318(args); break;
            case "322": on322(args); break;
            case "324": on324(args); break;
            case "330": on330(args); break;
            case "332": on332(args, tail); break;
            case "333": on333(args); break;
            case "351": on351(args); break;
            case "352": on352(args, tail); break;
            case "372": on372(lineArgs[0], tail); break;
            case "375": on375(lineArgs[0]); break;
            case "376": on376(lineArgs[0]); break;
            case "433":
            case "436":
            case "437":
                if (bot.getState() == State.connecting) {
                    if (configuration.getBoolean(Property.rename)) {
                        bot.logError("Nick rejected (Trying another one...)");
                        userMap.removeUser(bot.getNick());
                        bot.setNick(bot.getNick() + "_");
                        send("NICK " + bot.getNick());
                        userMap.addUser(bot);
                    } else {
                        bot.shutdown("Nick unavailable: " + tail);
                        return;
                    }
                }
                break;
            default:
                if (bot.getState() == State.connecting && command.matches("[45]\\d\\d")) {
                    bot.shutdown("An error occurred during connection: " + tail);
                    return;
                }
        }
        if (command.matches("\\d+"))
            bot.getHandler().onCode(bot, Integer.parseInt(command), args, tail);
        else
            bot.getHandler().onLine(bot, source, command, args, tail);
    }

    private void onError(String tail) {
        bot.shutdown(tail);
    }

    private void onInvite(User source, String[] args, String tail) {
        Channel channel = bot.getChannelMap().getChannel(args.length == 0 ? tail : args[0]);
        bot.getHandler().onInvite(bot, channel, source);
    }

    private void onJoin(User source, String[] args, String tail) {
        ChannelMap channelMap = bot.getChannelMap();
        Channel channel = channelMap.getChannel(args.length == 0 ? tail : args[0]);
        channel.addUser(source);
        if (source == bot) {
            channelMap.addChannel(channel);
            bot.send("WHO " + channel.getName());
            bot.send("MODE " + channel.getName());
            Configuration configuration = bot.getConfiguration();
            if (configuration.getBoolean(Property.updateChannels))
                configuration.addChannel(channel.getName());
        } else
            bot.send("WHOIS " + source.getNick());
        source.setKnown(true);
        bot.getHandler().onJoin(bot, channel, source);
    }

    private void onKick(User source, String[] args, String tail) {
        ChannelMap channelMap = bot.getChannelMap();
        UserMap userMap = bot.getUserMap();
        Channel channel = channelMap.getChannel(args[0]);
        User user = userMap.getUser(args[1]);
        channel.removeUser(user);
        if (user == bot) {
            channelMap.removeChannel(args[0]);
            channel.getUsers(u -> channelMap.size(c -> c.contains(u)) == 0).forEach(u -> {
                userMap.removeUser(u.getNick());
                u.setKnown(false);
            });
        } else if (channelMap.size(c -> c.contains(source)) == 0) {
            userMap.removeUser(user.getNick());
            user.setKnown(false);
        }
        bot.getHandler().onKick(bot, channel, source, user, tail);
    }

    private void onMode(User source, String[] args, String tail) {
        if (!tail.isEmpty()) {
            boolean added = tail.charAt(0) == '+';
            for (char mode : tail.substring(1).toCharArray()) {
                if (added) {
                    source.addMode(mode);
                    bot.getHandler().onModeAdded(bot, source, mode);
                } else {
                    source.removeMode(mode);
                    bot.getHandler().onModeRemoved(bot, source, mode);
                }
            }
        } else {
            Channel channel = bot.getChannelMap().getChannel(args[0]);
            boolean added = args[1].charAt(0) == '+';
            int i = 2;
            for (char mode : args[1].substring(1).toCharArray()) {
                String value = args.length >= i + 1 ? args[i++] : "";
                ModeType type = bot.getServer().getModeType(mode);
                if (type == ModeType.status) {
                    User user = bot.getUserMap().getUser(value);
                    if (added) {
                        channel.addMode(user, mode);
                        bot.getHandler().onModeAdded(bot, channel, source, user, mode);
                    } else {
                        channel.removeMode(user, mode);
                        bot.getHandler().onModeRemoved(bot, channel, source, user, mode);
                    }
                } else if (added) {
                    if (type == ModeType.list)
                        channel.addListMode(mode, value);
                    else
                        channel.addMode(mode, value);
                    bot.getHandler().onModeAdded(bot, channel, source, mode, value);
                } else {
                    if (type == ModeType.list)
                        channel.removeListMode(mode, value);
                    else
                        channel.removeMode(mode);
                    bot.getHandler().onModeRemoved(bot, channel, source, mode);
                }
            }
        }
    }

    private void onNick(User source, String[] args, String tail) {
        Configuration configuration = bot.getConfiguration();
        UserMap userMap = bot.getUserMap();
        String oldNick = source.getNick();
        configuration.clearPermissions(oldNick);
        source.setNick(args.length == 0 ? tail : args[0]);
        userMap.removeUser(oldNick);
        userMap.addUser(source);
        if (source == bot && configuration.getBoolean(Property.updateNick))
            configuration.setValue(Property.nick, source.getNick());
        bot.getHandler().onNick(bot, source, oldNick);
    }

    private void onNotice(User source, String[] args, String tail) {
        if (args[0].equals(bot.getNick()))
            bot.getHandler().onNotice(bot, source, tail);
        else
            bot.getHandler().onNotice(bot, bot.getChannelMap().getChannel(args[0]), source, tail);
    }

    private void onPart(User source, String[] args, String tail) {
        ChannelMap channelMap = bot.getChannelMap();
        UserMap userMap = bot.getUserMap();
        Channel channel = channelMap.getChannel(args[0]);
        channel.removeUser(source);
        if (source == bot) {
            channelMap.removeChannel(args[0]);
            channel.getUsers(u -> channelMap.size(c -> c.contains(u)) == 0).forEach(u -> {
                userMap.removeUser(u.getNick());
                u.setKnown(false);
            });
            Configuration configuration = bot.getConfiguration();
            if (configuration.getBoolean(Property.updateChannels))
                configuration.removeChannel(args[0]);
        } else if (channelMap.size(c -> c.contains(source)) == 0) {
            userMap.removeUser(source.getNick());
            source.setKnown(false);
        }
        bot.getHandler().onPart(bot, channel, source, tail);
    }

    private void onPing(String tail) {
        Configuration configuration = bot.getConfiguration();
        send("PONG :" + tail);
        String nick = configuration.getString(Property.nick);
        if (configuration.getBoolean(Property.checkNick) && !bot.getNick().equals(nick) && bot.getState() == State.connected)
            queue("NICK " + nick);
        bot.getHandler().onPing(bot, tail);
    }

    private void onPong(String[] args, String tail) {
        bot.getHandler().onPong(bot, bot.getServerMap().getServer(args[0]), tail);
    }

    private void onPrivmsg(User source, String[] args, String tail) {
        Configuration configuration = bot.getConfiguration();
        ChannelMap channelMap = bot.getChannelMap();
        if (tail.matches("^\\001[A-Z]+.*\\001$")) {
            String[] parts = tail.substring(1, tail.length() - 1).split(" ", 2);
            String s = parts.length > 1 ? parts[1] : "";
            if (parts[0].equals("ACTION")) {
                if (args[0].equals(bot.getNick()))
                    bot.getHandler().onAction(bot, source, s);
                else
                    bot.getHandler().onAction(bot, channelMap.getChannel(args[0]), source, s);
            } else if (args[0].equals(bot.getNick()))
                bot.getHandler().onCTCPCommand(bot, source, parts[0], s);
            else
                bot.getHandler().onCTCPCommand(bot, channelMap.getChannel(args[0]), source, parts[0], s);
            return;
        }
        boolean nickPrefix = configuration.getBoolean(Property.enableNickPrefix) && tail.matches("^" + bot.getNick() + "[,:]? .+");
        String prefix = configuration.getString(Property.prefix);
        boolean isCommand = (!prefix.isEmpty() && tail.startsWith(prefix)) || nickPrefix;
        if (isCommand && configuration.getBoolean(Property.enableCommands) && onPrivmsgCommand(source, args, tail))
            return;
        if (args[0].equals(bot.getNick()))
            bot.getHandler().onMessage(bot, source, tail);
        else
            bot.getHandler().onMessage(bot, channelMap.getChannel(args[0]), source, tail);
    }

    private boolean onPrivmsgCommand(User source, String[] args, String tail) {
        Configuration configuration = bot.getConfiguration();
        String prefix = configuration.getString(Property.prefix);
        if (source.hasPermission(Permission.IGNORE)
                && !source.hasPermission(Permission.OPERATOR)
                && configuration.getBoolean(Property.enableIgnore))
            return false;
        tail = tail.replaceAll("^" + bot.getNick() + "[,:]? ", prefix);
        if (configuration.getBoolean(Property.removeTrailingSpaces))
            tail = tail.replaceAll(" +$", "");
        else if (tail.endsWith(" ") || tail.endsWith("\"\""))
            tail += " ";
        boolean separate = tail.startsWith(prefix + " ");
        if (separate && !configuration.getBoolean(Property.allowSeparatePrefix))
            return false;
        String[] parts = tail.substring(prefix.length() + (separate ? 1 : 0)).split(" ", 2);
        CommandMap commandMap = bot.getCommandMap();
        ChannelMap channelMap = bot.getChannelMap();
        String[] cmdArgs = parts.length > 1 ? configuration.getBoolean(Property.enableQuoteSplit) ? StringUtils.splitArgs(parts[1]) : parts[1].split(" ") : new String[0];
        if (commandMap.isRegistered(parts[0])) {
            if (args[0].equals(bot.getNick()))
                bot.getHandler().onCommand(bot, source, commandMap.getCommand(parts[0]), cmdArgs);
            else
                bot.getHandler().onCommand(bot, channelMap.getChannel(args[0]), source, commandMap.getCommand(parts[0]), cmdArgs);
        } else if (args[0].equals(bot.getNick()))
            bot.getHandler().onUnknownCommand(bot, source, parts[0], cmdArgs);
        else
            bot.getHandler().onUnknownCommand(bot, channelMap.getChannel(args[0]), source, parts[0], cmdArgs);
        return true;
    }

    private void onQuit(User source, String tail) {
        bot.getChannelMap().getChannels(c -> c.contains(source)).forEach(c -> c.removeUser(source));
        if (source != bot)
            bot.getUserMap().removeUser(source.getNick());
        source.setKnown(false);
        bot.getHandler().onQuit(bot, source, tail);
    }

    private void onTopic(User source, String[] args, String tail) {
        Channel channel = bot.getChannelMap().getChannel(args[0]);
        channel.setTopic(tail);
        channel.setTopicSetter(source.getIdentity());
        channel.setTopicTimestamp(System.currentTimeMillis());
        bot.getHandler().onTopic(bot, channel, source, tail);
    }

    private void on001() {
        Configuration configuration = bot.getConfiguration();
        bot.setState(State.connected);
        String login = configuration.getString(Property.nickservLogin);
        String password = configuration.getString(Property.nickservPass);
        if (!password.isEmpty())
            if (login.isEmpty())
                queue("NICKSERV IDENTIFY " + password);
            else
                queue("NICKSERV IDENTIFY " + login + " " + password);
        for (String channel : configuration.getChannels())
            queue("JOIN " + channel);
        bot.getHandler().onConnect(bot);
    }

    private void on004(String[] args) {
        ServerMap serverMap = bot.getServerMap();
        Server server = serverMap.getServer(args[1]);
        bot.setServer(server);
        serverMap.addServer(server);
        server.setVersion(args[2]);
        for (char mode : args[3].toCharArray())
            server.addUserMode(mode);
        if (args.length >= 6)
            for (char mode : args[5].toCharArray())
                server.addValueMode(mode);
    }

    private void on005(String source, String[] args) {
        ServerMap serverMap = bot.getServerMap();
        Server server = serverMap.getServer(source);
        if (serverMap.contains(server))
            serverMap.addServer(server);
        for (String arg : args) {
            String[] info = arg.split("=", 2);
            server.addData(info[0], info.length == 2 ? info[1] : "");
        }
        if (server.isDataType("CHANMODES")) {
            String[] modeBlocks = server.getData("CHANMODES").split(",");
            for (char mode : modeBlocks[0].toCharArray())
                server.addMode(mode, ModeType.list);
            for (int i = 1; i < modeBlocks.length; i++)
                for (char mode : modeBlocks[i].toCharArray())
                    if (server.isValueMode(mode))
                        server.addMode(mode, ModeType.value);
                    else
                        server.addMode(mode, ModeType.valueless);
        }
        if (server.isDataType("PREFIX")) {
            String[] prefixSplit = server.getData("PREFIX").split("\\)");
            char[] modes = prefixSplit[0].substring(1).toCharArray();
            char[] prefixes = prefixSplit[1].toCharArray();
            if (modes.length != prefixes.length)
                return;
            for (int i = 0; i < modes.length; i++) {
                server.addMode(modes[i], ModeType.status);
                server.addPrefix(prefixes[i], modes[i]);
            }
        }
    }

    private void on311(String[] args, String tail) {
        User user = bot.getUserMap().getUser(args[1]);
        user.setLogin(args[2]);
        user.setHostmask(args[3]);
        user.setRealname(tail);
    }

    private void on312(String[] args, String tail) {
        ServerMap serverMap = bot.getServerMap();
        Server server = serverMap.getServer(args[2]);
        if (!serverMap.contains(args[2]))
            serverMap.addServer(server);
        bot.getUserMap().getUser(args[1]).setServer(server);
        server.setDescription(tail);
    }

    private void on318(String[] args) {
        bot.getHandler().onWhois(bot, bot.getUserMap().getUser(args[1]));
    }

    private void on322(String[] args) {
        if (bot.getChannelMap().getChannel(args[1]).size() != Integer.parseInt(args[2]))
            bot.send("WHO " + args[1]);
    }

    private void on324(String[] args) {
        Channel channel = bot.getChannelMap().getChannel(args[1]);
        for (char mode : args[2].substring(1).toCharArray())
            channel.addMode(mode, "");
    }

    private void on330(String[] args) {
        bot.getUserMap().getUser(args[1]).setNickservLogin(args[2]);
    }

    private void on332(String[] args, String tail) {
        bot.getChannelMap().getChannel(args[1]).setTopic(tail);
    }

    private void on333(String[] args) {
        Channel channel = bot.getChannelMap().getChannel(args[1]);
        channel.setTopicSetter(args[2]);
        channel.setTopicTimestamp(Long.parseLong(args[3]));
    }

    public void on351(String[] args) {
        ServerMap serverMap = bot.getServerMap();
        Server server = serverMap.getServer(args[2]);
        if (!serverMap.contains(args[2]))
            serverMap.addServer(server);
        server.setVersion(args[1]);
    }

    private void on352(String[] args, String tail) {
        ServerMap serverMap = bot.getServerMap();
        Channel channel = bot.getChannelMap().getChannel(args[1]);
        User user = bot.getUserMap().getUser(args[5]);
        user.setLogin(args[2]);
        user.setHostmask(args[3]);
        user.setRealname(tail.substring(2));
        user.setServer(serverMap.getServer(args[4]));
        user.setKnown(true);
        if (!serverMap.contains(args[4]))
            serverMap.addServer(user.getServer());
        if (!channel.contains(user))
            channel.addUser(user);
        for (char prefix : args[6].toCharArray())
            if (bot.getServer().supportsPrefix(prefix))
                channel.addMode(user, bot.getServer().getMode(prefix));
    }

    private void on372(String source, String tail) {
        bot.getServerMap().getServer(source).addMotdLine(tail);
    }

    private void on375(String source) {
        ServerMap serverMap = bot.getServerMap();
        Server server = serverMap.getServer(source);
        if (!serverMap.contains(server))
            serverMap.addServer(server);
        server.clearMotd();
    }

    private void on376(String source) {
        Server server = bot.getServerMap().getServer(source);
        bot.getHandler().onMOTD(bot, server);
    }

    private final class HandlerThread extends Thread {

        private boolean current = false;

        private HandlerThread() {
            super("Handler Thread [" + handlerThreads++ + "]");
        }

        @Override
        public void run() {
            current = true;
            int cycleDelay = bot.getConfiguration().getInteger(Property.cycleDelay);
            while (active && current) {
                try {
                    if (incoming.size() > 0)
                        handle(incoming.getNext());
                    else
                        Thread.sleep(cycleDelay);
                } catch (InterruptedException e) {
                    bot.logError("[Connection::HandlerThread:run] An internal exception has occurred (%s)", e.getMessage());
                } catch (Exception e) {
                    bot.logError("A handling exception has occurred (%s). The bot should continue functioning without major problems.", e.toString());
                    if (bot.getConfiguration().getBoolean(Property.logErrorTrace))
                        for (StackTraceElement element : e.getStackTrace())
                            bot.logTrace(element.toString());
                }
            }
            bot.log(getName() + " terminated.");
        }
    }

    private final class InputThread extends Thread {

        private InputThread() {
            super("Input Thread [" + inputThreads++ + "]");
        }

        @Override
        public void run() {
            String line;
            try {
                while (active && (line = reader.readLine()) != null) {
                    incoming.add(line);
                    if (bot.getConfiguration().getBoolean(Property.logInput))
                        bot.logInput(line.replaceAll("\\002|\\003\\d+(,\\d+)?|\\017", ""));
                }
            } catch (IOException e) {
                bot.logError("[Connection::InputThread:run] An internal exception has occurred (%s)", e.getMessage());
                bot.shutdown("A fatal exception occurred");
                if (bot.getConfiguration().getInteger(Property.reconnectDelay) > 0)
                    bot.connect();
            }
            bot.log(getName() + " terminated.");
        }
    }

    private final class OutputThread extends Thread {

        private OutputThread() {
            super("Output Thread [" + outputThreads++ + "]");
        }

        @Override
        public void run() {
            Configuration configuration = bot.getConfiguration();
            int cycleDelay = configuration.getInteger(Property.cycleDelay);
            int messageDelay = configuration.getInteger(Property.messageDelay);
            while (active) {
                try {
                    if (bot.getState() == com.archebot.State.connected && outgoing.hasNext()) {
                        send(outgoing.getNext());
                        if (messageDelay > 0)
                            Thread.sleep(messageDelay);
                    } else
                        Thread.sleep(cycleDelay);
                } catch (InterruptedException e) {
                    bot.logError("[Connection::OutputThread:run] An internal exception has occurred (%s)", e.getMessage());
                }
            }
            bot.log(getName() + " terminated.");
        }
    }
}
