/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.UnknownChannelException;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChannelMap implements Iterable<Channel> {

    private final TreeMap<String, Channel> channels = new TreeMap<>();
    private String name;

    public ChannelMap() {
        this("default");
    }

    public ChannelMap(String name) {
        this.name = name;
    }

    public boolean contains(String name) {
        return channels.containsKey(name.toLowerCase());
    }

    public boolean contains(Channel channel) {
        return channels.containsValue(channel);
    }

    public Channel getChannel(String name) throws UnknownChannelException {
        if (contains(name))
            return channels.get(name.toLowerCase());
        throw new UnknownChannelException(name);
    }

    public TreeSet<String> getChannelNames() {
        return new TreeSet<>(channels.keySet());
    }

    public TreeSet<Channel> getChannels() {
        return new TreeSet<>(channels.values());
    }

    public TreeSet<Channel> getChannels(Predicate<Channel> predicate) {
        return new TreeSet<>(channels.values().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return channels.size();
    }

    public int size(Predicate<Channel> predicate) {
        return (int) channels.values().stream().filter(predicate).count();
    }

    @Override
    public Iterator<Channel> iterator() {
        return channels.values().iterator();
    }

    @Override
    public String toString() {
        return "ChannelMap [" + name + "]";
    }

    protected void addChannel(Channel channel) {
        channels.put(channel.getName().toLowerCase(), channel);
    }

    protected void clear() {
        channels.clear();
    }

    protected void removeChannel(String name) {
        if (contains(name))
            channels.remove(name.toLowerCase());
    }
}
