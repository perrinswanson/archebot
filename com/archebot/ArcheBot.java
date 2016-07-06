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
 * @version ArcheBot 2.0
 * @see Connection
 * @see User
 * @since ArcheBot 1.0
 */
public class ArcheBot extends User {

    public static final String VERSION = "2.0";
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss:SSS] ");
    private final long startTime = System.currentTimeMillis();
    private long connectTime = 0;
    private CommandMap commandMap = new CommandMap();
    private ServerMap serverMap = new ServerMap();
    private PrintStream logStream = System.out;
    private PrintStream errorLogStream = System.err;
    private PrintStream inputLogStream = System.out;
    private PrintStream outputLogStream = System.out;
    private State state = State.idle;
    private ChannelMap channelMap;
    private UserMap userMap;
    private Connection connection;
    private Handler handler;
    private Configuration configuration;

    public ArcheBot() {
        this(new Configuration());
    }

    public ArcheBot(String configuration) {
        this(new Configuration(configuration));
    }

    public ArcheBot(Configuration configuration) {
        setBot(this);
        setKnown(true);
        setConfiguration(configuration);
        channelMap = new ChannelMap(this);
        userMap = new UserMap(this);
        log("ArcheBot (Version %s) loaded.", VERSION);
        log("Initial configuration: " + configuration.getName());
    }

    public void breakThread() throws ConnectionStateException {
        if (state == State.idle)
            throw new ConnectionStateException(state, "Unable to break handler thread");
        connection.breakThread();
    }

    public void clearData() {
        if (configuration.getBoolean(Property.autoSavePerms))
            userMap.forEach(configuration::storePermissions);
        channelMap.deactivate();
        channelMap = new ChannelMap(this);
        serverMap.deactivate();
        serverMap = new ServerMap();
        userMap.deactivate();
        userMap = new UserMap(this);
        clearModes();
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

    public ChannelMap getChannelMap() {
        return channelMap;
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

    public ServerMap getServerMap() {
        return serverMap;
    }

    public State getState() {
        return state;
    }

    public long getUptime() {
        return state == State.idle ? -1 : System.currentTimeMillis() - connectTime;
    }

    public UserMap getUserMap() {
        return userMap;
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public void log(String message, Object... objects) {
        if (objects.length > 0)
            message = String.format(message, objects);
        print(logStream, "<> ", message);
    }

    public void logError(String error, Object... objects) {
        if (objects.length > 0)
            error = String.format(error, objects);
        print(errorLogStream, "== Error: ", error);
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

    public void setCommandMap(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    public void setConfiguration(Configuration configuration) throws ConnectionStateException {
        if (state == State.idle)
            this.configuration = configuration;
        else
            throw new ConnectionStateException(state, "Unable to set configuration");
    }

    public void setErrorLogStream(PrintStream stream) {
        errorLogStream = stream;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setInputLogStream(PrintStream stream) {
        inputLogStream = stream;
    }

    public void setLogStream(PrintStream stream) {
        logStream = stream;
    }

    public void setOutputLogStream(PrintStream stream) {
        outputLogStream = stream;
    }

    void logInput(String line) {
        print(inputLogStream, "<- ", line);
    }

    void logOutput(String line) {
        print(outputLogStream, "-> ", line);
    }

    void logTrace(String line) {
        print(errorLogStream, "<==> ", line);
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
        clearData();
        if (configuration.getBoolean(Property.autoSaveConfig))
            try {
                configuration.save();
            } catch (IOException e) {
                logError("[ArcheBot::shutdown] An error occurred while saving the configuration (%s)", e.getMessage());
            }
        log("Disconnected (%s)", reason);
        state = State.idle;
    }

    private synchronized void print(PrintStream stream, String prefix, String line) {
        if (configuration.getBoolean(Property.enableLogging)) {
            stream.print(dateFormat.format(new Date()));
            stream.print(prefix);
            stream.println(line);
        }
    }
}
