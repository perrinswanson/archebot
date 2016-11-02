/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import java.util.HashMap;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    private final String name;
    private final String[] ids;
    private final HashMap<String, String> data = new HashMap<>();
    private boolean enabled = true;
    private boolean requireLogin = false;
    private Permission permission = Permission.DEFAULT;

    public Command(String name, String... ids) {
        this.name = name;
        this.ids = ids;
    }

    public abstract void execute(B bot, Channel channel, User sender, String[] args);

    public void execute(B bot, User sender, String[] args) {
        execute(bot, bot.createChannel(sender.getNick()), sender, args);
    }

    public HashMap<String, String> getData() {
        return new HashMap<>(data);
    }

    public String getData(String type) {
        if (hasData(type))
            return data.get(type.toLowerCase());
        return "";
    }

    public String getDescription() {
        return getData("description");
    }

    public String[] getIds() {
        return ids;
    }

    public String getName() {
        return name;
    }

    public String getSyntax() {
        return getData("syntax");
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean hasData(String type) {
        return data.containsKey(type.toLowerCase());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean requiresNickservLogin() {
        return requireLogin;
    }

    public void setData(String type, String value) {
        data.put(type.toLowerCase(), value);
    }

    public void setDescription(String description) {
        setData("description", description);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSyntax(String syntax) {
        setData("syntax", syntax);
    }

    public void setPermission(String permission) {
        setPermission(Permission.get(permission));
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setRequiresNickservLogin(boolean requireLogin) {
        this.requireLogin = requireLogin;
    }

    @Override
    public int compareTo(Command<B> command) {
        return name.compareToIgnoreCase(command.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
