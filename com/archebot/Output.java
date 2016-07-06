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

public class Output implements Comparable<Output> {

    private final String command;
    private String prefix = "";
    private String tail = "";
    private String[] args = {};

    public Output(String command) {
        this.command = command;
    }

    public String getArg(int index) {
        if (index < args.length && index >= 0)
            return args[index];
        throw new ArrayIndexOutOfBoundsException("Argument index out of bounds (" + index + ")");
    }

    public String[] getArgs() {
        return args;
    }

    public String getCommand() {
        return command;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getTail() {
        return tail;
    }

    public void send(ArcheBot bot) throws ConnectionStateException {
        bot.send(this);
    }

    public Output setArg(int index, String value) {
        if (index < args.length && index >= 0) {
            args[index] = value;
            return this;
        }
        throw new ArrayIndexOutOfBoundsException("Argument index out of bounds (" + index + ")");
    }

    public Output setArgs(String... args) {
        this.args = args;
        return this;
    }

    public Output setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Output setTail(String tail) {
        this.tail = tail;
        return this;
    }

    @Override
    public int compareTo(Output output) {
        return toString().compareTo(output.toString());
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Output))
            return false;
        Output output = (Output) object;
        if (!command.equals(output.command))
            return false;
        if (!prefix.equals(output.prefix))
            return false;
        if (!tail.equals(output.tail))
            return false;
        if (args.length != output.args.length)
            return false;
        for (int i = 0; i < args.length; i++)
            if (!args[i].equals(output.args[i]))
                return false;
        return true;
    }

    @Override
    public final String toString() {
        return (prefix.isEmpty() ? "" : ":" + prefix + " ")
                + command
                + (args.length == 0 ? "" : " " + StringUtils.compact(args))
                + (tail.isEmpty() ? "" : " :" + tail);
    }
}
