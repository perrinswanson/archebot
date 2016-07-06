/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

public abstract class Command<B extends ArcheBot> implements Comparable<Command<B>> {

    private final String name;
    private final String[] ids;
    private boolean enabled = true;
    private boolean requireLogin = false;
    private Permission permission = Permission.DEFAULT;
    private String syntax = "";
    private String[] description = {};

    public Command(String name, String... ids) {
        this.name = name;
        this.ids = ids;
    }

    public abstract void execute(B bot, Channel channel, User sender, String[] args);

    public void execute(B bot, User sender, String[] args) {
        execute(bot, bot.getChannelMap().getChannel(sender.getNick()), sender, args);
    }

    public String[] getDescription() {
        return description;
    }

    public String[] getIds() {
        return ids;
    }

    public String getName() {
        return name;
    }

    public String getSyntax() {
        return syntax;
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean requiresNickservLogin() {
        return requireLogin;
    }

    public void setDescription(String... description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
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
