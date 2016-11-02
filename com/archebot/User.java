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
import com.archebot.utilities.StringUtils;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User implements Comparable<User> {

    protected final ArcheBot bot;
    private final HashMap<Permission, Boolean> permissions = new HashMap<>();
    private final TreeSet<Character> modes = new TreeSet<>();
    private boolean known = false;
    private String login = "";
    private String hostmask = "";
    private String realname = "";
    private Server server;
    private String nick;
    private String nickservLogin;

    public User(ArcheBot bot, String nick) {
        this.bot = bot;
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

    public void debug() {
        bot.log(getIdentity());
        if (!realname.isEmpty())
            bot.log("   Real name: " + realname);
        if (server != null)
            bot.log("   Server: " + server);
        if (isIdentified())
            bot.log("   Nickserv login: " + nickservLogin);
        bot.log("   Known: " + known);
        bot.log("   Permissions: " + StringUtils.compact(permissions.keySet()));
        if (modes.size() > 0)
            bot.log("   Modes: " + StringUtils.compact(modes, ""));
    }

    public ArcheBot getBot() {
        return bot;
    }

    public String getHostmask() {
        return hostmask;
    }

    public String getIdentity() {
        return nick + (login.isEmpty() ? "" : "!" + login) + (hostmask.isEmpty() ? "" : "@" + hostmask);
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
        permission.getSubPermissions(p -> hasPermission(p) && !isIncluded(p)).forEach(this::removePermission);
    }

    public void whois() {
        bot.send("WHOIS " + nick);
    }

    @Override
    public int compareTo(User user) {
        return getIdentity().compareToIgnoreCase(user.getIdentity());
    }

    @Override
    public String toString() {
        return nick;
    }

    protected void addMode(char mode) {
        modes.add(mode);
    }

    protected void clearModes() {
        modes.clear();
    }

    protected void removeMode(char mode) {
        modes.remove(mode);
    }

    protected void setHostmask(String hostmask) {
        this.hostmask = hostmask;
    }

    protected void setKnown(boolean known) {
        this.known = known;
    }

    protected void setLogin(String login) {
        this.login = login;
    }

    protected void setNick(String nick) {
        this.nick = nick;
    }

    protected void setNickservLogin(String nickservLogin) {
        this.nickservLogin = nickservLogin;
    }

    protected void setRealname(String realname) {
        this.realname = realname;
    }

    protected void setServer(Server server) {
        this.server = server;
    }

    private void givePermission(Permission permission, boolean included) {
        permissions.put(permission, included);
        permission.getSubPermissions(p -> !hasPermission(p)).forEach(p -> givePermission(p, false));
    }

    public static String parseNick(String identity) {
        if (identity.contains("!"))
            return identity.substring(0, identity.indexOf('!'));
        if (identity.contains("@"))
            return identity.substring(0, identity.indexOf('@'));
        return identity;
    }
}
