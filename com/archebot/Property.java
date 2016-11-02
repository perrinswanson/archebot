/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

public enum Property {

    /** Lets the configuration be saved automatically on shutdown */
    autoSaveConfig(true),

    /** Lets permissions be saved automatically before memory clearing */
    autoSavePerms(true),

    /** Lets the nick be reset to the default if it is different */
    checkNick(false),

    /** The time in milliseconds between thread loops to prevent CPU over-usage (Requires reconnecting to apply changes) */
    cycleDelay(50),

    /** Lets commands be turned on or off */
    enableCommands(true),

    /** Lets &[#] shortcuts be used to format messages */
    enableFormatting(true),

    /** Lets logging be turned on and off */
    enableLogging(true),

    /** Lets users use the bot's name instead of the prefix */
    enableNickPrefix(true),

    /** Lets users separate arguments with quotation marks in addition to spaces */
    enableQuoteSplit(true),

    /** Lets the bot start shutting down immediately, ignoring messages that haven't yet been sent to the server */
    immediateDisconnect(false),

    /** The maximum number of characters in a single line */
    lineLength(510),

    /** The bot's server login identification */
    login("ArcheBot"),

    /** Lets internal errors be logged */
    logErrorTrace(true),

    /** Lets messages sent to the bot be logged */
    logInput(true),

    /** Lets messages sent by the bot be logged */
    logOutput(true),

    /** The time in milliseconds between sending each message (Requires reconnecting to apply changes) */
    messageDelay(1000),

    /** The bot's default nick - must be set before connecting */
    nick(null),

    /** The bot's identification for NickServ */
    nickservLogin(""),

    /** The bot's password for NickServ */
    nickservPass(""),

    /** The password for connecting to a server */
    password(""),

    /** The server port */
    port(6667),

    /** The prefix used by the bot to recognize commands */
    prefix(""),

    /** The bot's realname */
    realname("ArcheBot (Version " + ArcheBot.VERSION + ") by Perrin Swanson"),

    /** The time in milliseconds between reconnect attempts */
    reconnectDelay(0),

    /** Removes extra spaces after command arguments */
    removeTrailingSpaces(false),

    /** Lets the bot rename itself if its default nick is already used on the server */
    rename(false),

    /** The server name - must be set before connecting */
    server(null),

    /** The time in milliseconds of server silence before timing out (Requires reconnecting to apply changes) */
    timeoutDelay(240000),

    /** Adds/removes channels from the current configuration */
    updateChannels(false),

    /** Lets the configuration nick be modified when the server-side nick is */
    updateNick(false),

    /** Lets the bot be set as visible or hidden on the server */
    visible(false);

    private final Object defaultValue;

    Property(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static boolean isValue(String name) {
        for (Property property : Property.values())
            if (property.name().equalsIgnoreCase(name))
                return true;
        return false;
    }
}
