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

public class Invite extends Output {

    public Invite(Channel channel, User user) {
        this(channel.getName(), user.getNick());
    }

    public Invite(String channel, String user) {
        super("INVITE");
        setArgs(channel, user);
    }

    public Invite setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public Invite setChannel(String channel) {
        setArg(0, channel);
        return this;
    }

    public Invite setUser(User user) {
        return setUser(user.getNick());
    }

    public Invite setUser(String user) {
        setArg(1, user);
        return this;
    }
}
