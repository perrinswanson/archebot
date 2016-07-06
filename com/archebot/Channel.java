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
import com.archebot.exceptions.UnknownModeException;
import com.archebot.exceptions.UnknownUserException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Channel implements Comparable<Channel>, Iterable<User> {

    private final ArcheBot bot;
    private final String name;
    private final TreeMap<User, HashSet<Character>> users = new TreeMap<>();
    private final HashMap<Character, String> modes = new HashMap<>();
    private final HashMap<Character, HashSet<String>> listModes = new HashMap<>();
    private long topicTimestamp = -1;
    private String topic = "";
    private String topicSetter = "";

    protected Channel(ArcheBot bot, String name) {
        this.bot = bot;
        this.name = name;
    }

    public void action(String action, Object... objects) throws ConnectionStateException {
        ctcp("ACTION", action, objects);
    }

    public boolean contains(User user) {
        return users.containsKey(user);
    }

    public void ctcp(String command, String args, Object... objects) throws ConnectionStateException {
        bot.send("PRIVMSG " + name + " :\1" + command + " " + args + "\1", objects);
    }

    public HashSet<Character> getListModes() {
        return new HashSet<>(listModes.keySet());
    }

    public HashSet<Character> getModes() {
        return new HashSet<>(modes.keySet());
    }

    public HashSet<Character> getModes(User user) throws UnknownUserException {
        if (contains(user))
            return new HashSet<>(users.get(user));
        throw new UnknownUserException(user.getNick());
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public String getTopicSetter() {
        return topicSetter;
    }

    public long getTopicTimestamp() {
        return topicTimestamp;
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users.keySet());
    }

    public TreeSet<User> getUsers(Predicate<User> predicate) {
        return new TreeSet<>(users.keySet().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public String getValue(char mode) throws UnknownModeException {
        if (hasMode(mode))
            return modes.get(mode);
        throw new UnknownModeException(mode, ModeType.value);
    }

    public HashSet<String> getValues(char mode) throws UnknownModeException {
        if (hasListMode(mode))
            return new HashSet<>(listModes.get(mode));
        throw new UnknownModeException(mode, ModeType.list);
    }

    public boolean hasListMode(char mode) {
        return listModes.containsKey(mode);
    }

    public boolean hasMode(char mode) {
        return modes.containsKey(mode);
    }

    public boolean hasMode(User user, char mode) {
        return contains(user) && users.get(user).contains(mode);
    }

    public boolean isValue(char mode, String value) {
        return hasListMode(mode) && listModes.get(mode).contains(value);
    }

    public void join() throws ConnectionStateException {
        bot.send("JOIN " + name);
    }

    public void kick(User user) throws ConnectionStateException, UnknownUserException {
        if (!contains(user))
            throw new UnknownUserException(user.getNick());
        bot.send("KICK " + name + " " + user);
    }

    public void kick(User user, String reason, Object... objects) throws ConnectionStateException, UnknownUserException {
        if (!contains(user))
            throw new UnknownUserException(user.getNick());
        bot.send("KICK " + name + " " + user.getNick() + " :" + reason, objects);
    }

    public void message(String message, Object... objects) throws ConnectionStateException {
        bot.send("PRIVMSG " + name + " :" + message, objects);
    }

    public void modeAdd(char mode) throws ConnectionStateException {
        bot.send("MODE " + name + " +" + mode);
    }

    public void modeAdd(char mode, User user) throws ConnectionStateException {
        modeAdd(mode, user.getNick());
    }

    public void modeAdd(char mode, String value) throws ConnectionStateException {
        bot.send("MODE " + name + " +" + mode + " " + value);
    }

    public void modeRemove(char mode) throws ConnectionStateException {
        bot.send("MODE " + name + " -" + mode);
    }

    public void modeRemove(char mode, User user) throws ConnectionStateException {
        modeRemove(mode, user.getNick());
    }

    public void modeRemove(char mode, String value) throws ConnectionStateException {
        bot.send("MODE " + name + " -" + mode + " " + value);
    }

    public void notify(String notice, Object... objects) throws ConnectionStateException {
        bot.send("NOTICE " + name + " :" + notice, objects);
    }

    public void part() throws ConnectionStateException {
        bot.send("PART " + name);
    }

    public int size() {
        return users.size();
    }

    public int size(Predicate<User> predicate) {
        return (int) users.keySet().stream().filter(predicate).count();
    }

    public void topic(String topic, Object... objects) throws ConnectionStateException {
        bot.send("TOPIC " + name + " :" + topic, objects);
    }

    @Override
    public int compareTo(Channel channel) {
        return name.compareToIgnoreCase(channel.name);
    }

    @Override
    public Iterator<User> iterator() {
        return users.keySet().iterator();
    }

    @Override
    public String toString() {
        return name;
    }

    void addListMode(char mode, String value) {
        if (!hasListMode(mode))
            listModes.put(mode, new HashSet<>());
        listModes.get(mode).add(value);
    }

    void addMode(User user, char mode) {
        if (contains(user) && !hasMode(user, mode))
            users.get(user).add(mode);
    }

    void addMode(char mode, String value) {
        modes.put(mode, value);
    }

    void addUser(User user) {
        users.put(user, new HashSet<>());
    }

    void removeListMode(char mode, String value){
        if (hasListMode(mode)) {
            listModes.get(mode).remove(value);
            if (listModes.get(mode).size() == 0)
                listModes.remove(mode);
        }
    }

    void removeMode(User user, char mode) {
        if (contains(user) && hasMode(user, mode))
            users.get(user).remove(mode);
    }

    void removeMode(char mode) {
        modes.remove(mode);
    }

    void removeUser(User user) {
        users.remove(user);
    }

    void setTopic(String topic) {
        this.topic = topic;
    }

    void setTopicSetter(String topicSetter) {
        this.topicSetter = topicSetter;
    }

    void setTopicTimestamp(long topicTimestamp) {
        this.topicTimestamp = topicTimestamp;
    }
}
