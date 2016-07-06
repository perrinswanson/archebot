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

public class JoinMessage extends Output {

    public JoinMessage(Channel channel) {
        this(channel.getName());
    }

    public JoinMessage(String channel) {
        super("JOIN");
        setChannel(channel);
    }

    public JoinMessage(Channel channel, String key) {
        this(channel.getName(), key);
    }

    public JoinMessage(String channel, String key) {
        super("JOIN");
        setChannel(channel, key);
    }

    public JoinMessage setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public JoinMessage setChannel(Channel channel, String key) {
        return setChannel(channel.getName(), key);
    }

    public JoinMessage setChannel(String channel) {
        setArgs(channel);
        return this;
    }

    public JoinMessage setChannel(String channel, String key) {
        setArgs(channel, key);
        return this;
    }
}
