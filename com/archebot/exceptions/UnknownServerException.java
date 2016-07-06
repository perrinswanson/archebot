/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.exceptions;

public class UnknownServerException extends RuntimeException {

    private final String server;

    public UnknownServerException(String server) {
        super("Unknown server: " + server);
        this.server = server;
    }

    public String getServer() {
        return server;
    }
}
