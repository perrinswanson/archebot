/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.output;

import com.archebot.Channel;
import com.archebot.Output;
import com.archebot.User;

public class AddModeMessage extends Output {

    public AddModeMessage(Channel channel, char mode) {
        this(channel.getName(), mode);
    }

    public AddModeMessage(String channel, char mode) {
        super("MODE");
        setArgs(channel, "+" + mode);
    }

    public AddModeMessage(Channel channel, char mode, User user) {
        this(channel.getName(), mode, user.getNick());
    }

    public AddModeMessage(Channel channel, char mode, String value) {
        this(channel.getName(), mode, value);
    }

    public AddModeMessage(String channel, char mode, String value) {
        super("MODE");
        setArgs(channel, "+" + mode, value);
    }

    public AddModeMessage setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public AddModeMessage setChannel(String channel) {
        setArg(0, channel);
        return this;
    }

    public AddModeMessage setMode(char mode) {
        setArgs(getArg(0), "+" + mode);
        return this;
    }

    public AddModeMessage setMode(char mode, User user) {
        return setMode(mode, user.getNick());
    }

    public AddModeMessage setMode(char mode, String value) {
        setArgs(getArg(0), "+" + mode, value);
        return this;
    }
}