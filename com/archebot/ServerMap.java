/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.UnknownServerException;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerMap implements Iterable<Server> {

    private final TreeMap<String, Server> servers = new TreeMap<>();
    private String name;

    public ServerMap() {
        this("default");
    }

    public ServerMap(String name) {
        this.name = name;
    }

    public boolean contains(String name) {
        return servers.containsKey(name.toLowerCase());
    }

    public boolean contains(Server server) {
        return servers.containsValue(server);
    }

    public String getName() {
        return name;
    }

    public Server getServer(String name) throws UnknownServerException {
        if (contains(name))
            return servers.get(name.toLowerCase());
        throw new UnknownServerException(name);
    }

    public TreeSet<String> getServerNames() {
        return new TreeSet<>(servers.keySet());
    }

    public TreeSet<Server> getServers() {
        return new TreeSet<>(servers.values());
    }

    public TreeSet<Server> getServers(Predicate<Server> predicate) {
        return new TreeSet<>(servers.values().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return servers.size();
    }

    public int size(Predicate<Server> predicate) {
        return (int) servers.values().stream().filter(predicate).count();
    }

    @Override
    public Iterator<Server> iterator() {
        return servers.values().iterator();
    }

    @Override
    public String toString() {
        return "ServerMap [" + servers.size() + "]";
    }

    protected void addServer(Server server) {
        servers.put(server.getName().toLowerCase(), server);
    }

    protected void clear() {
        servers.clear();
    }

    protected void removeServer(String name) {
        if (contains(name))
            servers.remove(name.toLowerCase());
    }
}
