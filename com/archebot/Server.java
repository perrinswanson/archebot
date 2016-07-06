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

import java.util.*;

public class Server implements Comparable<Server> {

    private final String name;
    private final ArrayList<String> motd = new ArrayList<>();
    private final HashMap<String, String> data = new HashMap<>();
    private final TreeMap<Character, ModeType> modes = new TreeMap<>();
    private final TreeSet<Character> userModes = new TreeSet<>();
    private final TreeSet<Character> valueModes = new TreeSet<>();
    private final HashMap<Character, Character> prefixes = new HashMap<>();
    private String description = "";
    private String version = "";

    protected Server(String name) {
        this.name = name;
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

    void addData(String type, String value) {
        data.put(type.toLowerCase(), value);
    }

    void addMode(char mode, ModeType type) {
        modes.put(mode, type);
    }

    void addMotdLine(String line) {
        motd.add(line);
    }

    void addPrefix(char prefix, char mode) {
        prefixes.put(prefix, mode);
    }

    void addUserMode(char mode) {
        userModes.add(mode);
    }

    void addValueMode(char mode) {
        valueModes.add(mode);
    }

    void clearMotd() {
        motd.clear();
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setVersion(String version) {
        this.version = version;
    }
}
