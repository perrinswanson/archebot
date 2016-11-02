/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Group implements Comparable<Group>, Iterable<User> {

    private final TreeSet<User> users = new TreeSet<>();
    private final HashSet<Permission> permissions = new HashSet<>();
    private String name;
    private int maxSize;

    public Group() {
        this("default");
    }

    public Group(String name) {
        this(name, 0);
    }

    public Group(String name, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
    }

    public void add(User user) {
        if (maxSize <= 0 || users.size() < maxSize) {
            users.add(user);
            permissions.forEach(user::givePermission);
        }
    }

    public void addPermission(String permission) {
        addPermission(Permission.get(permission));
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
        users.forEach(u -> u.givePermission(permission));
    }

    public void clear() {
        users.clear();
    }

    public boolean contains(User user) {
        return users.contains(user);
    }

    public boolean containsPermission(String permission) {
        return containsPermission(Permission.get(permission));
    }

    public boolean containsPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getName() {
        return name;
    }

    public HashSet<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    public TreeSet<User> getUsers() {
        return new TreeSet<>(users);
    }

    public TreeSet<User> getUsers(Predicate<User> predicate) {
        return new TreeSet<>(users.stream().filter(predicate).collect(Collectors.toSet()));
    }

    public void remove(User user) {
        users.remove(user);
        permissions.forEach(user::removePermission);
    }

    public void removePermission(String permission) {
        removePermission(Permission.get(permission));
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        users.forEach(u -> u.removePermission(permission));
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return users.size();
    }

    public int size(Predicate<User> predicate) {
        return (int) users.stream().filter(predicate).count();
    }

    @Override
    public int compareTo(Group group) {
        return name.compareToIgnoreCase(group.name);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Group))
            return false;
        Group group = (Group) object;
        if (!name.equals(group.name))
            return false;
        if (maxSize != group.maxSize)
            return false;
        for (User user : users)
            if (!group.contains(user))
                return false;
        for (User user : group)
            if (!contains(user))
                return false;
        for (Permission permission : permissions)
            if (!group.containsPermission(permission))
                return false;
        for (Permission permission : group.permissions)
            if (!containsPermission(permission))
                return false;
        return true;
    }

    @Override
    public Iterator<User> iterator() {
        return users.iterator();
    }

    @Override
    public String toString() {
        return name;
    }
}
