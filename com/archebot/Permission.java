/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Permission implements Comparable<Permission>, Iterable<Permission> {

    private static final HashMap<String, Permission> permissions = new HashMap<>();
    public static final Permission OPERATOR = new Permission("operator", true);
    public static final Permission DEFAULT = new Permission("default", false);
    public static final Permission IGNORE = new Permission("ignore", true);
    private final String name;
    private final TreeSet<Permission> subPermissions = new TreeSet<>();
    private boolean savable;

    private Permission(String name, boolean savable) {
        this.name = name;
        this.savable = savable;
        permissions.put(name.toLowerCase(), this);
    }

    public TreeSet<Permission> getSubPermissions() {
        return new TreeSet<>(subPermissions);
    }

    public TreeSet<Permission> getSubPermissions(Predicate<Permission> predicate) {
        return new TreeSet<>(subPermissions.stream().filter(predicate).collect(Collectors.toSet()));
    }

    public String getName() {
        return name;
    }

    public void include(Permission permission, Permission... permissions) {
        subPermissions.add(permission);
        if (permissions.length > 0)
            subPermissions.addAll(Arrays.asList(permissions));
    }

    public void include(String permission, String... permissions) {
        include(get(permission));
        for (String p : permissions)
            include(get(p));
    }

    public boolean includes(String permission) {
        return includes(get(permission));
    }

    public boolean includes(Permission permission) {
        return subPermissions.contains(permission);
    }

    public boolean isSavable() {
        return savable;
    }

    public void setSavable(boolean savable) {
        this.savable = savable;
    }

    @Override
    public int compareTo(Permission permission) {
        return name.compareToIgnoreCase(permission.name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission && obj.toString().equals(toString());
    }

    @Override
    public Iterator<Permission> iterator() {
        return subPermissions.iterator();
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean exists(String name) {
        return permissions.containsKey(name.replaceAll("^permission\\.", "").toLowerCase());
    }

    public static Permission get(String name) {
        name = name.replaceAll("^permission\\.", "");
        if (permissions.containsKey(name.toLowerCase()))
            return permissions.get(name.toLowerCase());
        Permission permission = new Permission(name, true);
        OPERATOR.include(permission);
        return permission;
    }

    public static TreeSet<Permission> getAll() {
        return new TreeSet<>(permissions.values());
    }

    public static TreeSet<Permission> getAll(Predicate<Permission> predicate) {
        return new TreeSet<>(permissions.values().stream().filter(predicate).collect(Collectors.toSet()));
    }
}
