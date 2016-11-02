/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.UnknownModeException;
import com.archebot.utilities.StringUtils;

import java.util.*;

public class Server implements Comparable<Server> {

    protected final ArcheBot bot;
    protected final String name;
    private final ArrayList<String> motd = new ArrayList<>();
    private final HashMap<String, String> data = new HashMap<>();
    private final TreeMap<Character, ModeType> modes = new TreeMap<>();
    private final TreeSet<Character> userModes = new TreeSet<>();
    private final TreeSet<Character> valueModes = new TreeSet<>();
    private final HashMap<Character, Character> prefixes = new HashMap<>();
    private String description = "";
    private String version = "";

    public Server(ArcheBot bot, String name) {
        this.bot = bot;
        this.name = name;
    }

    public void debug() {
        bot.log(name);
        if (!description.isEmpty())
            bot.log("   Description: " + description);
        if (!version.isEmpty())
            bot.log("   Version: " + version);
        bot.log("   MOTD: %d lines", motd.size());
        if (modes.size() > 0)
            bot.log("   Modes: " + StringUtils.compact(modes.keySet(), ""));
        if (userModes.size() > 0)
            bot.log("   User modes: " + StringUtils.compact(userModes, ""));
        if (prefixes.size() > 0)
            bot.log("   Prefixes: " + StringUtils.compact(prefixes.keySet(), ""));
        bot.log("   Data types: %d known", data.size());
    }

    public ArcheBot getBot() {
        return bot;
    }

    public String getData(String type) {
        if (isDataType(type))
            return data.get(type.toLowerCase());
        throw new IllegalArgumentException("[Server::getData] Attempted to get data for unknown type: " + type);
    }

    public HashSet<String> getDataTypes() {
        return new HashSet<>(data.keySet());
    }

    public String getDescription() {
        return description;
    }

    public char getMode(char prefix) {
        if (supportsPrefix(prefix))
            return prefixes.get(prefix);
        throw new IllegalArgumentException("[Server::getMode] Attempted to get mode for unknown prefix: " + prefix);
    }

    public TreeSet<Character> getModes() {
        return new TreeSet<>(modes.keySet());
    }

    public ModeType getModeType(char mode) throws UnknownModeException {
        if (supportsMode(mode))
            return modes.get(mode);
        throw new UnknownModeException(mode, ModeType.unknown);
    }

    public ArrayList<String> getMotd() {
        return new ArrayList<>(motd);
    }

    public String getName() {
        return name;
    }

    public HashSet<Character> getPrefixes() {
        return new HashSet<>(prefixes.keySet());
    }

    public TreeSet<Character> getUserModes() {
        return new TreeSet<>(userModes);
    }

    public String getVersion() {
        return version;
    }

    public boolean isDataType(String type) {
        return data.containsKey(type.toLowerCase());
    }

    public boolean isValueMode(char mode) {
        return valueModes.contains(mode);
    }

    public boolean supportsPrefix(char prefix) {
        return prefixes.containsKey(prefix);
    }

    public boolean supportsMode(char mode) {
        return modes.containsKey(mode);
    }

    public boolean supportsUserMode(char mode) {
        return userModes.contains(mode);
    }

    @Override
    public int compareTo(Server server) {
        return name.compareToIgnoreCase(server.name);
    }

    @Override
    public String toString() {
        return name;
    }

    protected void addData(String type, String value) {
        data.put(type.toLowerCase(), value);
    }

    protected void addMode(char mode, ModeType type) {
        modes.put(mode, type);
    }

    protected void addMotdLine(String line) {
        motd.add(line);
    }

    protected void addPrefix(char prefix, char mode) {
        prefixes.put(prefix, mode);
    }

    protected void addUserMode(char mode) {
        userModes.add(mode);
    }

    protected void addValueMode(char mode) {
        valueModes.add(mode);
    }

    protected void clearMotd() {
        motd.clear();
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setVersion(String version) {
        this.version = version;
    }
}
