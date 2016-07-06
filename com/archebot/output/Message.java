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

public class Message extends Output {

    public Message(Channel channel, String message, Object... objects) {
        this(channel.getName(), message, objects);
    }

    public Message(User user, String message, Object... objects) {
        this(user.getNick(), message, objects);
    }

    public Message(String target, String message, Object... objects) {
        super("PRIVMSG");
        setTarget(target);
        setMessage(message, objects);
    }

    public Message setChannel(Channel channel) {
        return setTarget(channel.getName());
    }

    public Message setUser(User user) {
        return setTarget(user.getNick());
    }

    public Message setTarget(String target) {
        setArgs(target);
        return this;
    }

    public Message setMessage(String message, Object... objects) {
        if (objects.length > 0)
            message = String.format(message, objects);
        setTail(message);
        return this;
    }
}
