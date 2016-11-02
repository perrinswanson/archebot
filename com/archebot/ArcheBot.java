/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.ConnectionStateException;
import com.archebot.exceptions.UnknownCommandException;
import com.archebot.utilities.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The ArcheBot class acts as a bridge between developers and the IRC server. Through it, developers can access lists
 * of users, channels, and servers on the network. All messages to the server are sent via this class.
 * Additionally, this class is used to keep track of local objects such as the configuration, handler, and commands.
 * When creating bots, developers have the option of using this class as-is, or extending and modifying its
 * features.
 *
 * @author Perrin Swanson
 * @version ArcheBot 2.1
 * @see Connection
 * @see User
 * @since ArcheBot 1.0
 */
public class ArcheBot extends User {

    public static final String VERSION = "2.1";
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    private final long startTime = System.currentTimeMillis();
    private long connectTime = 0;
    private ChannelMap channelMap = new ChannelMap();
    private CommandMap commandMap = new CommandMap();
    private ServerMap serverMap = new ServerMap();
    private UserMap userMap = new UserMap();
    private PrintStream logStream = System.out;
    private State state = State.idle;
    private Configuration configuration;
    private Connection connection;
    private Handler handler;

    public ArcheBot() {
        this(new Configuration());
    }

    public ArcheBot(String nick, String server) {
        this(new Configuration(nick, server));
    }

    public ArcheBot(Configuration configuration) {
        super(null, configuration.isSet(Property.nick) ? configuration.getNick() : "");
        setKnown(true);
        setConfiguration(configuration);
        userMap.addUser(this);
        log("ArcheBot (Version %s) loaded.", VERSION);
        log("Initial configuration: " + configuration.getName());
    }

    public void breakThread() throws ConnectionStateException {
        if (state == State.idle)
            throw new ConnectionStateException(state, "Unable to break handler thread");
        connection.breakThread();
    }

    public void connect() throws ConnectionStateException {
        if (state == State.idle)  {
            if (!hasHandler()) {
                setHandler(new Handler());
                log("No handler found. A default handler has been set.");
            }
            for (Property property : Property.values())
                if (configuration.getValue(property) == null) {
                    logError("Unable to connect (Value of '%s' is set to null)", property.name());
                    return;
                }
            try {
                connection = new Connection(this);
                connectTime = System.currentTimeMillis();
            } catch (IOException e) {
                int delay = configuration.getInteger(Property.reconnectDelay);
                logError("[ArcheBot::connect] An internal exception has occurred (%s)", e.getMessage());
                log("Disconnected (A fatal exception occurred while connecting | Reconnect: %b)", delay > 0);
                if (delay > 0) {
                    log("Reconnecting in approximately %d seconds...", delay / 1000);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        logError("[ArcheBot::connect] An internal exception has occurred (%s)", ie.getMessage());
                    }
                    connect();
                }
            }
        } else
            throw new ConnectionStateException(state, "Unable to connect");
    }

    public Channel createChannel(String name) {
        return new Channel(this, name);
    }

    public Server createServer(String name) {
        return new Server(this, name);
    }

    public User createUser(String nick) {
        User user = new User(this, nick);
        configuration.loadPermissions(user);
        return user;
    }

    public void disconnect() throws ConnectionStateException {
        disconnect("Shutting down");
    }

    public void disconnect(String message) throws ConnectionStateException {
        disconnect(message, false);
    }

    public void disconnect(String message, boolean reconnect) throws ConnectionStateException {
        if (state == State.idle)
            throw new ConnectionStateException(state, "Unable to disconnect");
        if (configuration.getBoolean(Property.immediateDisconnect))
            connection.send("QUIT :" + message);
        else
            send("QUIT :" + message);
        if (reconnect) {
            breakThread();
            int cycleDelay = configuration.getInteger(Property.cycleDelay);
            try {
                while (state != State.idle)
                    Thread.sleep(cycleDelay);
            } catch (InterruptedException e) {
                logError("[ArcheBot::disconnect] An internal exception has occurred (%s)", e.getMessage());
            }
            connect();
        }
    }

    public Channel getChannel(String name) {
        if (!channelMap.contains(name)) {
            Channel channel = createChannel(name);
            channelMap.addChannel(channel);
            return channel;
        }
        return channelMap.getChannel(name);
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public Command getCommand(String id) throws UnknownCommandException {
        return commandMap.getCommand(id);
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Handler getHandler() {
        return handler;
    }

    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }

    public Server getServer(String name) {
        if (!serverMap.contains(name)) {
            Server server = createServer(name);
            serverMap.addServer(server);
            return server;
        }
        return serverMap.getServer(name);
    }

    public ServerMap getServerMap() {
        return serverMap;
    }

    public State getState() {
        return state;
    }

    public long getUptime() {
        return state == State.idle ? -1 : System.currentTimeMillis() - connectTime;
    }

    public User getUser(String identity) {
        if (identity.isEmpty())
            return new User(this, "");
        if (!userMap.contains(identity)) {
            User user = createUser(User.parseNick(identity));
            userMap.addUser(user);
            if (identity.contains("!"))
                user.setLogin(identity.substring(identity.indexOf('!') + 1, identity.contains("@") ? identity.indexOf('@') : identity.length()));
            if (identity.contains("@"))
                user.setHostmask(identity.substring(identity.indexOf('@') + 1));
            return user;
        }
        return userMap.getUser(identity);
    }

    public UserMap getUserMap() {
        return userMap;
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public boolean isConnected() {
        return state == State.connected;
    }

    public void log(String message, Object... objects) {
        if (objects.length > 0)
            message = String.format(message, objects);
        print("<> ", message);
    }

    public void logError(String error, Object... objects) {
        if (objects.length > 0)
            error = String.format(error, objects);
        print("== Error: ", error);
    }

    public void send(Output output) throws ConnectionStateException {
        send(output.toString());
    }

    public void send(String output, Object... objects) throws ConnectionStateException {
        if (objects.length > 0)
            output = String.format(output, objects);
        if (state == State.connecting || state == State.connected)
            connection.queue(output);
        else
            throw new ConnectionStateException(state, "Unable to send output [" + output + "]");
    }

    public void setChannelMap(ChannelMap channelMap) {
        this.channelMap = channelMap;
    }

    public void setCommandMap(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    public void setConfiguration(Configuration configuration) throws ConnectionStateException {
        if (state == State.idle)
            this.configuration = configuration;
        else
            throw new ConnectionStateException(state, "Unable to set configuration");
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setLogStream(PrintStream stream) {
        logStream = stream;
    }

    public void setServerMap(ServerMap serverMap) {
        this.serverMap = serverMap;
    }

    public void setUserMap(UserMap userMap) {
        this.userMap = userMap;
        if (!userMap.contains(this))
            userMap.addUser(this);
    }

    @Override
    public void action(String action, Object... objects) throws ConnectionStateException {
        ctcp("ACTION", action, objects);
    }

    @Override
    public void ctcp(String command, String args, Object... objects) throws ConnectionStateException {
        send("PRIVMSG " + getNick() + " :\1" + command + " " + args + "\1", objects);
    }

    @Override
    public void debug() {
        log(getIdentity());
        if (!getRealname().isEmpty())
            log("   Real name: " + getRealname());
        if (getServer() != null)
            log("   Server: " + getServer());
        if (isIdentified())
            log("   Nickserv login: " + getNickservLogin());
        log("   Known: " + isKnown());
        log("   Permissions: " + StringUtils.compact(getPermissions()));
        if (getModes().size() > 0)
            log("   Modes: " + StringUtils.compact(getModes(), ""));
    }

    @Override
    public ArcheBot getBot() {
        return this;
    }

    @Override
    public void message(String message, Object... objects) throws ConnectionStateException {
        send("PRIVMSG " + getNick() + " :" + message, objects);
    }

    @Override
    public void notice(String notice, Object... objects) throws ConnectionStateException {
        send("NOTICE " + getNick() + " :" + notice, objects);
    }

    protected void logInput(String line) {
        print("<- ", line);
    }

    protected void logOutput(String line) {
        print("-> ", line);
    }

    protected void logTrace(String line) {
        print("<==> ", line);
    }

    void setState(State state) {
        this.state = state;
    }

    @SuppressWarnings("unchecked")
    void shutdown(String reason) {
        if (connection == null)
            return;
        state = State.disconnecting;
        connection.close();
        connection = null;
        handler.onDisconnect(this, reason);
        connectTime = 0;
        if (configuration.getBoolean(Property.autoSavePerms))
            userMap.forEach(configuration::storePermissions);
        channelMap.clear();
        serverMap.clear();
        userMap.clear();
        clearModes();
        if (configuration.getBoolean(Property.autoSaveConfig))
            try {
                configuration.save();
            } catch (IOException e) {
                logError("[ArcheBot::shutdown] An error occurred while saving the configuration (%s)", e.getMessage());
            }
        log("Disconnected (%s)", reason);
        state = State.idle;
    }

    private synchronized void print(String prefix, String line) {
        if (configuration.getBoolean(Property.enableLogging)) {
            logStream.print(dateFormat.format(new Date()));
            logStream.print(prefix);
            logStream.println(line);
        }
    }
}
