/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import com.archebot.utilities.Element;
import com.archebot.utilities.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Configuration {

    private final HashMap<Property, Object> values = new HashMap<>();
    private final HashMap<String, Set<String>> permissions = new HashMap<>();
    private final HashSet<String> channels = new HashSet<>();
    private String name;
    private String directory = null;

    public Configuration() {
        this("default");
    }

    public Configuration(String name) {
        this.name = name;
    }

    public void addChannel(String channel) {
        channels.add(channel);
    }

    public void addPermission(String user, Permission permission) {
        addPermission(user, permission.getName());
    }

    public void addPermission(String user, String permission) {
        if (!hasPermissions(user))
            permissions.put(user.toLowerCase(), new HashSet<>());
        permissions.get(user.toLowerCase()).add(permission);
    }

    public void clearPermissions(String user) {
        permissions.remove(user.toLowerCase());
    }

    public boolean getBoolean(Property property) {
        Object value = getValue(property);
        if (value instanceof Boolean)
            return (boolean) value;
        value = property.getDefaultValue();
        return value instanceof Boolean && (boolean) value;
    }

    public TreeSet<String> getChannels() {
        return new TreeSet<>(channels);
    }

    public String getDirectory() {
        return directory;
    }

    public int getInteger(Property property) {
        Object value = getValue(property);
        if (value instanceof Integer)
            return (int) value;
        if (property.getDefaultValue() instanceof Integer)
            return (int) property.getDefaultValue();
        return 0;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return getString(Property.nick);
    }

    public int getPort() {
        return getInteger(Property.port);
    }

    public String getServer() {
        return getString(Property.server);
    }

    public String getString(Property property) {
        return getValue(property).toString();
    }

    public Object getValue(Property property) {
        if (isSet(property))
            return values.get(property);
        return property.getDefaultValue();
    }

    public boolean hasPermissions(User user) {
        return hasPermissions(user.getNick());
    }

    public boolean hasPermissions(String user) {
        return permissions.containsKey(user.toLowerCase());
    }

    public boolean includesChannel(String channel) {
        return channels.contains(channel);
    }

    public boolean isSavable() {
        return directory != null;
    }

    public boolean isSet(Property property) {
        return values.containsKey(property);
    }

    public void loadPermissions(User user) {
        if (hasPermissions(user))
            permissions.get(user.getNick().toLowerCase()).forEach(user::givePermission);
    }

    public int permissionsSize() {
        return permissions.size();
    }

    public void removeChannel(String channel) {
        channels.remove(channel);
    }

    public void removePermission(String user, Permission permission) {
        removePermission(user, permission.getName());
    }

    public void removePermission(String user, String permission) {
        if (hasPermissions(user)) {
            Set<String> perms = permissions.get(user.toLowerCase());
            perms.remove(permission);
            if (perms.size() == 0)
                permissions.remove(user.toLowerCase());
        }
    }

    public void removeValue(Property property) {
        values.remove(property);
    }

    public void save() throws IOException {
        if (isSavable()) {
            Element element = new Element(directory + name);
            Element props = element.getChild("properties");
            values.keySet().forEach(p -> props.getChild(p.name()).setContent(getString(p)));
            Element perms = element.getChild("permissions");
            permissions.keySet().forEach(u -> permissions.get(u).forEach(p -> perms.addChild(new Element(u, p))));
            channels.forEach(c -> element.addChild(new Element("channel", c)));
            element.write();
        }
    }

    public void setDirectory(String directory) {
        if (directory != null && !directory.isEmpty() && !directory.endsWith(File.separator))
            directory += File.separator;
        this.directory = directory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNick(String nick) {
        setValue(Property.nick, nick);
    }

    public void setPort(int port) {
        setValue(Property.port, port);
    }

    public void setServer(String server) {
        setValue(Property.server, server);
    }

    public Object setValue(Property property, Object value) {
        values.put(property, value);
        return value;
    }

    public int size() {
        return values.size();
    }

    public void storePermissions(User user) {
        Set<String> perms = user.getPermissions(p -> p.isSavable() && user.isIncluded(p)).stream().map(Permission::getName).collect(Collectors.toSet());
        if (perms.size() > 0)
            permissions.put(user.getNick().toLowerCase(), perms);
        else if (hasPermissions(user))
            permissions.remove(user.getNick().toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }

    private void updateValueFromElement(Element element) {
        setValue(Property.valueOf(element.getTag()), toObject(element.getContent()));
    }

    public static Configuration load(String name) throws IOException {
        return load("", name);
    }

    public static Configuration load(String directory, String name) throws IOException {
        Configuration configuration = new Configuration(name);
        configuration.setDirectory(directory);
        Element element = Element.read(configuration.getDirectory() + name);
        element.getChild("properties").getChildren(e -> Property.isValue(e.getTag())).forEach(configuration::updateValueFromElement);
        element.getChild("permissions").forEach(e -> configuration.addPermission(e.getTag(), e.getContent()));
        element.getChildren("channel").stream().map(Element::getContent).forEach(configuration::addChannel);
        return configuration;
    }

    private static Object toObject(String value) {
        if (value.toLowerCase().matches("true|false"))
            return StringUtils.toBoolean(value);
        else if (value.matches("\\d+"))
            return Integer.parseInt(value);
        else
            return value;
    }
}
