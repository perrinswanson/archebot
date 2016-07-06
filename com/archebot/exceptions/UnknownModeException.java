/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.exceptions;

import com.archebot.ModeType;

public class UnknownModeException extends RuntimeException {

    private final char mode;
    private final ModeType type;

    public UnknownModeException(char mode, ModeType type) {
        super("Unknown mode: " + mode);
        this.mode = mode;
        this.type = type;
    }

    public char getMode() {
        return mode;
    }

    public ModeType getType() {
        return type;
    }
}
