/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.exceptions.UnknownUserException;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserMap implements Iterable<User> {

    private final TreeMap<String, User> users = new TreeMap<>();
    private String name;

    public UserMap() {
        this("default");
    }

    public UserMap(String name) {
        this.name = name;
    }

    public boolean contains(String identity) {
        return users.containsKey(User.parseNick(identity).toLowerCase());
    }

    public boolean contains(User user) {
        return users.containsValue(user);
    }

    public String getName() {
        return name;
    }

    public User getUser(String identity) throws UnknownUserException {
        String nick = User.parseNick(identity);
        if (contains(nick))
            return users.get(nick.toLowerCase());
        throw new UnknownUserException(nick);
    }

    public TreeSet<String> getUserNicks() {
        return new TreeSet<>(users.keySet());
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users.values());
    }

    public TreeSet<User> getUsers(Predicate<User> predicate) {
        return new TreeSet<>(users.values().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return users.size();
    }

    public int size(Predicate<User> predicate) {
        return (int) users.values().stream().filter(predicate).count();
    }

    public Group toGroup() {
        Group group = new Group(name, size());
        forEach(group::add);
        return group;
    }

    @Override
    public Iterator<User> iterator() {
        return users.values().iterator();
    }

    @Override
    public String toString() {
        return "UserMap [" + name + "]";
    }

    protected void addUser(User user) {
        users.put(user.getNick().toLowerCase(), user);
    }

    protected void clear() {
        users.clear();
    }

    protected void removeUser(String nick) {
        if (contains(nick))
            users.remove(nick.toLowerCase());
    }
}
