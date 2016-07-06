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

public class KickMessage extends Output {

    public KickMessage(Channel channel, User user) {
        this(channel.getName(), user.getNick());
    }

    public KickMessage(String channel, String user) {
        this(channel, user, "");
    }

    public KickMessage(Channel channel, User user, String reason, Object... objects) {
        this(channel.getName(), user.getNick(), reason, objects);
    }

    public KickMessage(String channel, String user, String reason, Object... objects) {
        super("KICK");
        setArgs(channel, user);
        setReason(reason, objects);
    }

    public KickMessage setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public KickMessage setChannel(String channel) {
        setArg(0, channel);
        return this;
    }

    public KickMessage setUser(User user) {
        return setUser(user.getNick());
    }

    public KickMessage setUser(String user) {
        setArg(1, user);
        return this;
    }

    public KickMessage setReason(String reason, Object... objects) {
        if (objects.length > 0)
            reason = String.format(reason, objects);
        setTail(reason);
        return this;
    }
}
