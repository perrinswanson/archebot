/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.exceptions;

public class UnknownUserException extends RuntimeException {

    private final String user;

    public UnknownUserException(String user) {
        super("Unknown user: " + user);
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
