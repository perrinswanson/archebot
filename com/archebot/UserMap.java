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

    private static int count = 0;
    private final ArcheBot bot;
    private final TreeMap<String, User> users = new TreeMap<>();
    private final int id = count++;
    private boolean current = true;

    UserMap(ArcheBot bot) {
        this.bot = bot;
        addUser(bot);
    }

    public boolean contains(String nick) {
        return users.containsKey(nick.toLowerCase());
    }

    public boolean contains(User user) {
        return users.containsValue(user);
    }

    public int getId() {
        return id;
    }

    public User getUser(String identity) {
        return getUser(identity, true);
    }

    public User getUser(String identity, boolean createNew) throws UnknownUserException {
        if (identity.isEmpty())
            return new User();
        boolean hasLogin = identity.contains("!");
        boolean hasHostmask = identity.contains("@");
        String nick;
        if (hasLogin)
            nick = identity.substring(0, identity.indexOf('!'));
        else if (hasHostmask)
            nick = identity.substring(0, identity.indexOf('@'));
        else
            nick = identity;
        if (!contains(nick)) {
            if (!createNew)
                throw new UnknownUserException(nick);
            User user = new User(nick);
            user.setBot(bot);
            if (hasLogin)
                user.setLogin(identity.substring(identity.indexOf('!') + 1, hasHostmask ? identity.indexOf('@') : identity.length()));
            if (hasHostmask)
                user.setHostmask(identity.substring(identity.indexOf('@') + 1));
            addUser(user);
        }
        return users.get(nick.toLowerCase());
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

    public boolean isCurrent() {
        return current;
    }

    public int size() {
        return users.size();
    }

    public int size(Predicate<User> predicate) {
        return (int) users.values().stream().filter(predicate).count();
    }

    @Override
    public Iterator<User> iterator() {
        return users.values().iterator();
    }

    @Override
    public String toString() {
        return "UserMap [" + id + "]";
    }

    void addUser(User user) {
        bot.getConfiguration().loadPermissions(user);
        users.put(user.getNick().toLowerCase(), user);
    }

    void deactivate() {
        current = false;
    }

    void removeUser(String nick) {
        if (contains(nick))
            bot.getConfiguration().storePermissions(getUser(nick));
        users.remove(nick.toLowerCase());
    }

    public static int getCount() {
        return count;
    }
}
