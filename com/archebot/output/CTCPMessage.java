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

public class CTCPMessage extends Output {

    public CTCPMessage(Channel channel, String command, String args, Object... objects) {
        this(channel.getName(), command, args, objects);
    }

    public CTCPMessage(User user, String command, String args, Object... objects) {
        this(user.getNick(), command, args, objects);
    }

    public CTCPMessage(String target, String command, String args, Object... objects) {
        super("PRIVMSG");
        setTarget(target);
        setMessage(command, args, objects);
    }

    public CTCPMessage setChannel(Channel channel) {
        return setTarget(channel.getName());
    }

    public CTCPMessage setUser(User user) {
        return setTarget(user.getNick());
    }

    public CTCPMessage setTarget(String target) {
        setArgs(target);
        return this;
    }

    public CTCPMessage setMessage(String command, String args, Object... objects) {
        if (objects.length > 0)
            args = String.format(args, objects);
        setTail("\1" + command.toUpperCase() + " " + args + "\1");
        return this;
    }
}