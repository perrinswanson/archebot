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

import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User implements Comparable<User> {

    private final HashMap<Permission, Boolean> permissions = new HashMap<>();
    private final TreeSet<Character> modes = new TreeSet<>();
    private boolean known = false;
    private String login = "";
    private String hostmask = "";
    private String realname = "";
    private ArcheBot bot;
    private Server server;
    private String nick;
    private String nickservLogin;

    protected User() {
        this("");
    }

    protected User(String nick) {
        this.nick = nick;
        givePermission(Permission.DEFAULT);
    }

    public void action(String action, Object... objects) throws ConnectionStateException {
        ctcp("ACTION", action, objects);
    }

    public void ctcp(String command, String args, Object... objects) throws ConnectionStateException {
        bot.send("PRIVMSG " + nick + " :\1" + command + " " + args + "\1", objects);
    }

    public void clearPermissions() {
        permissions.clear();
        givePermission(Permission.DEFAULT);
    }

    public String getIdentity() {
        return nick + (login.isEmpty() ? "" : "!" + login) + (hostmask.isEmpty() ? "" : "@" + hostmask);
    }

    public String getHostmask() {
        return hostmask;
    }

    public String getLogin() {
        return login;
    }

    public TreeSet<Character> getModes() {
        return new TreeSet<>(modes);
    }

    public String getNick() {
        return nick;
    }

    public String getNickservLogin() {
        return nickservLogin;
    }

    public TreeSet<Permission> getPermissions() {
        return new TreeSet<>(permissions.keySet());
    }

    public TreeSet<Permission> getPermissions(Predicate<Permission> predicate) {
        return new TreeSet<>(permissions.keySet().stream().filter(predicate).collect(Collectors.toSet()));
    }

    public String getRealname() {
        return realname;
    }

    public Server getServer() {
        return server;
    }

    public void givePermission(String permission) {
        givePermission(Permission.get(permission), true);
    }

    public void givePermission(Permission permission) {
        givePermission(permission, true);
    }

    public boolean hasPermission(String permission) {
        return hasPermission(Permission.get(permission));
    }

    public boolean hasPermission(Permission permission) {
        return permissions.containsKey(permission);
    }

    public boolean hasMode(char mode) {
        return modes.contains(mode);
    }

    public boolean isIdentified() {
        return nickservLogin != null;
    }

    public boolean isIncluded(Permission permission) {
        return hasPermission(permission) && permissions.get(permission);
    }

    public boolean isKnown() {
        return known;
    }

    public void message(String message, Object... objects) throws ConnectionStateException {
        bot.send("PRIVMSG " + nick + " :" + message, objects);
    }

    public void notice(String notice, Object... objects) throws ConnectionStateException {
        bot.send("NOTICE " + nick + " :" + notice, objects);
    }

    public void removePermission(String permission) {
        removePermission(Permission.get(permission));
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.getInclusions(p -> hasPermission(p) && !isIncluded(p)).forEach(this::removePermission);
    }

    @Override
    public int compareTo(User user) {
        return getIdentity().compareToIgnoreCase(user.getIdentity());
    }

    @Override
    public String toString() {
        return nick;
    }

    void addMode(char mode) {
        modes.add(mode);
    }

    void clearModes() {
        modes.clear();
    }

    void removeMode(char mode) {
        modes.remove(mode);
    }

    void setBot(ArcheBot bot) {
        this.bot = bot;
    }

    void setHostmask(String hostmask) {
        this.hostmask = hostmask;
    }

    void setKnown(boolean known) {
        this.known = known;
    }

    void setLogin(String login) {
        this.login = login;
    }

    void setNick(String nick) {
        this.nick = nick;
    }

    void setNickservLogin(String nickservLogin) {
        this.nickservLogin = nickservLogin;
    }

    void setRealname(String realname) {
        this.realname = realname;
    }

    void setServer(Server server) {
        this.server = server;
    }

    private void givePermission(Permission permission, boolean included) {
        permissions.put(permission, included);
        permission.getInclusions(p -> !hasPermission(p)).forEach(p -> givePermission(p, false));
    }
}
