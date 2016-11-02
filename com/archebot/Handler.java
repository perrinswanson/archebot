/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

public class Handler<B extends ArcheBot> {

    /**
     * Runs when an action to a channel is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the action was sent to
     * @param user the user who sent the action
     * @param action the action that was sent
     */
    public void onAction(B bot, Channel channel, User user, String action) {}

    /**
     * Runs when an action to the bot is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the action
     * @param action the action that was sent
     */
    public void onAction(B bot, User user, String action) {
        onAction(bot, bot.createChannel(user.getNick()), user, action);
    }

    /**
     * Runs when a 3-digit numeric code is received.
     *
     * @param bot the bot that received the message
     * @param code the 3-digit code
     * @param args the additional arguments that were received
     * @param tail the last argument
     */
    public void onCode(B bot, int code, String[] args, String tail) {}

    /**
     * Runs when a command is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the command was sent to
     * @param user the user who sent the command
     * @param command the command that was sent
     * @param args the additional arguments that were received
     */
    public void onCommand(B bot, Channel channel, User user, Command<B> command, String[] args) {
        if (!command.isEnabled() && !user.hasPermission(Permission.OPERATOR))
            user.notice("That command is not currently enabled.");
        else if (command.requiresNickservLogin() && !user.isIdentified())
            user.notice("You must be identified with NickServ to run that command.");
        else if (user.hasPermission(command.getPermission()))
            command.execute(bot, channel, user, args);
        else
            user.notice("You do not have permission to do that. (Required permission: %s)", command.getPermission());
    }

    /**
     * Runs when a command is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the command
     * @param command the command that was sent
     * @param args the additional arguments that were received
     */
    public void onCommand(B bot, User user, Command<B> command, String[] args) {
        if (!command.isEnabled() && !user.hasPermission(Permission.OPERATOR))
            user.message("That command is not currently enabled.");
        else if (command.requiresNickservLogin() && !user.isIdentified())
            user.message("You must be identified with NickServ to run that command.");
        else if (user.hasPermission(command.getPermission()))
            command.execute(bot, bot.createChannel(user.getNick()), user, args);
        else
            user.message("You do not have permission to do that. (Required permission: %s)", command.getPermission());
    }

    /**
     * Runs when a connection is successfully made.
     *
     * @param bot the bot that connected
     */
    public void onConnect(B bot) {}

    /**
     * Runs when a CTCP command to a channel is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the CTCP command was sent to
     * @param user the user who sent the CTCP command
     * @param command the CTCP command that was sent
     * @param args the additional arguments that were received
     */
    public void onCTCPCommand(B bot, Channel channel, User user, String command, String args) {}

    /**
     * Runs when a CTCP command to the bot is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the CTCP command
     * @param command the CTCP command that was sent
     * @param args the additional arguments that were received
     */
    public void onCTCPCommand(B bot, User user, String command, String args) {
        onCTCPCommand(bot, bot.createChannel(user.getNick()), user, command, args);
    }

    /**
     * Runs when the bot becomes disconnected from a server.
     *
     * @param bot the bot that disconnected
     * @param reason the reason for disconnected
     */
    public void onDisconnect(B bot, String reason) {}

    /**
     * Runs when a channel invite is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the invite is for
     * @param user the user who sent the invite
     */
    public void onInvite(B bot, Channel channel, User user) {}

    /**
     * Runs when a join message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the user joined
     * @param user the user who joined the channel
     */
    public void onJoin(B bot, Channel channel, User user) {}

    /**
     * Runs when a kick message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the user was kicked from
     * @param kicker the user who kicked the receiver
     * @param receiver the user who was kicked by the kicker
     * @param reason the reason why the user was kicked
     */
    public void onKick(B bot, Channel channel, User kicker, User receiver, String reason) {}

    /**
     * Runs when any line is received.
     *
     * @param bot the bot that received the message
     * @param source the user who send the line
     * @param command the type of message received
     * @param args the additional arguments that were received
     * @param tail the last argument
     */
    public void onLine(B bot, User source, String command, String[] args, String tail) {}

    /**
     * Runs when a message to a channel is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the message was sent to
     * @param user the user who sent the message
     * @param message the message that was sent
     */
    public void onMessage(B bot, Channel channel, User user, String message) {}

    /**
     * Runs when a message to the bot is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the message
     * @param message the message that was sent
     */
    public void onMessage(B bot, User user, String message) {
        onMessage(bot, bot.createChannel(user.getNick()), user, message);
    }

    /**
     * Runs when a channel mode added message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that had the mode added
     * @param user the user who added the mode
     * @param mode the mode that was added
     * @param value the new value of the mode, if the mode supports values
     */
    public void onModeAdded(B bot, Channel channel, User user, char mode, String value) {}

    /**
     * Runs when a channel/user mode added message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that had the mode added
     * @param setter the user who added the receiver's mode
     * @param receiver the user whose mode was added by the setter
     * @param mode the mode that was added
     */
    public void onModeAdded(B bot, Channel channel, User setter, User receiver, char mode) {}

    /**
     * Runs when a user mode added message is received.
     *
     * @param bot the bot that received the message
     * @param user the user whose mode was added
     * @param mode the mode that was added
     */
    public void onModeAdded(B bot, User user, char mode) {}

    /**
     * Runs when a channel mode removed message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that had the mode removed
     * @param user the user who removed the mode
     * @param mode the mode that was removed
     */
    public void onModeRemoved(B bot, Channel channel, User user, char mode) {}

    /**
     * Runs when a channel/user mode removed message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that had the mode removed
     * @param setter the user who removed the receiver's mode
     * @param receiver the user whose mode was removed by the setter
     * @param mode the mode that was removed
     */
    public void onModeRemoved(B bot, Channel channel, User setter, User receiver, char mode) {}

    /**
     * Runs when a user mode removed message is received.
     *
     * @param bot the bot that received the message
     * @param user the user whose mode was removed
     * @param mode the mode that was removed
     */
    public void onModeRemoved(B bot, User user, char mode) {}

    /**
     * Runs when a MOTD completed message is received.
     *
     * @param bot the bot that received the message
     * @param server the server that the MOTD is for
     */
    public void onMOTD(B bot, Server server) {}

    /**
     * Runs when a nick message is received.
     *
     * @param bot the bot that received the message
     * @param user the user who changed their nick
     * @param oldNick the user's previous nick
     */
    public void onNick(B bot, User user, String oldNick) {}

    /**
     * Runs when a notice to a channel is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the notice was sent to
     * @param user the user who sent the notice
     * @param notice the notice that was sent
     */
    public void onNotice(B bot, Channel channel, User user, String notice) {}

    /**
     * Runs when a notice to the bot is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the notice
     * @param notice the notice that was sent
     */
    public void onNotice(B bot, User user, String notice) {
        onNotice(bot, bot.createChannel(user.getNick()), user, notice);
    }

    /**
     * Runs when a part message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the user is parting from
     * @param user the user who parted from the channel
     * @param reason the reason why the user parted the channel
     */
    public void onPart(B bot, Channel channel, User user, String reason) {}

    /**
     * Runs when a ping is received.
     *
     * @param bot the bot that received the message
     * @param message the message sent with the ping
     */
    public void onPing(B bot, String message) {}

    /**
     * Runs when a pong is received.
     *
     * @param bot the bot that received the message
     * @param server the server that the pong was received from
     * @param message the message sent with the pong
     */
    public void onPong(B bot, Server server, String message) {}

    /**
     * Runs when a quit message is received.
     *
     * @param bot the bot that received the message
     * @param user the user who quit
     * @param reason the reason why the user quit
     */
    public void onQuit(B bot, User user, String reason) {}

    /**
     * Runs when a topic message is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that had the topic change
     * @param user the user who changed the topic
     * @param topic the new channel topic
     */
    public void onTopic(B bot, Channel channel, User user, String topic) {}

    /**
     * Runs when an unknown command is received.
     *
     * @param bot the bot that received the message
     * @param channel the channel that the command was sent to
     * @param user the user who sent the command
     * @param command the command that was sent
     * @param args the additional arguments that were received
     */
    public void onUnknownCommand(B bot, Channel channel, User user, String command, String[] args) {
        user.notice("'%s' is not a registered command.", command);
    }

    /**
     * Runs when an unknown command is received.
     *
     * @param bot the bot that received the message
     * @param user the user who sent the command
     * @param command the command that was sent
     * @param args the additional arguments that were received
     */
    public void onUnknownCommand(B bot, User user, String command, String[] args) {
        user.message("'%s' is not a registered command.", command);
    }

    /**
     * Runs when a whois completed message is received.
     *
     * @param bot the bot that received the message
     * @param user the user targeted by the whois
     */
    public void onWhois(B bot, User user) {}
}
