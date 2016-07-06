/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.UnknownCommandException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandMap implements Iterable<Command> {

    private static int count = 0;
    private final TreeMap<String, Command> commands = new TreeMap<>();
    private final int id = count++;

    public Command getCommand(String id) throws UnknownCommandException {
        if (isRegistered(id))
            return commands.get(id.toLowerCase());
        throw new UnknownCommandException(id);
    }

    public TreeSet<Command> getCommands() {
        return new TreeSet<>(commands.values());
    }

    public TreeSet<Command> getCommands(Predicate<Command> predicate) {
        return new TreeSet<>(commands.values().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public int getId() {
        return id;
    }

    public TreeSet<String> getRegisteredIds() {
        return new TreeSet<>(commands.keySet());
    }

    public boolean isRegistered(Command command) {
        return commands.containsValue(command);
    }

    public boolean isRegistered(String id) {
        return commands.containsKey(id.toLowerCase());
    }

    public void register(Command command) {
        register(command.getName(), command);
        for (String id : command.getIds())
            if (!isRegistered(id))
                register(id, command);
    }

    public void register(String id, Command command) {
        commands.put(id.toLowerCase(), command);
    }

    public int size() {
        return commands.size();
    }

    public int totalCommands() {
        return new HashSet<>(commands.values()).size();
    }

    public int totalCommands(Predicate<Command> predicate) {
        return (int) commands.values().stream().filter(predicate).count();
    }

    public void unregister(Command command) {
        unregister(command.getName());
        for (String id : command.getIds())
            if (isRegistered(id) && getCommand(id) == command)
                unregister(id);
    }

    public void unregister(String id) {
        if (isRegistered(id))
            commands.remove(id.toLowerCase());
    }

    @Override
    public Iterator<Command> iterator() {
        return getCommands().iterator();
    }

    @Override
    public String toString() {
        return "CommandMap [" + id + "]";
    }

    public static int getCount() {
        return count;
    }
}
