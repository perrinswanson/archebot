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

public class Action extends Output {

    public Action(Channel channel, String action, Object... objects) {
        this(channel.getName(), action, objects);
    }

    public Action(User user, String action, Object... objects) {
        this(user.getNick(), action, objects);
    }

    public Action(String target, String action, Object... objects) {
        super("PRIVMSG");
        setTarget(target);
        setAction(action, objects);
    }

    public Action setChannel(Channel channel) {
        return setTarget(channel.getName());
    }

    public Action setUser(User user) {
        return setTarget(user.getNick());
    }

    public Action setTarget(String target) {
        setArgs(target);
        return this;
    }

    public Action setAction(String action, Object... objects) {
        if (objects.length > 0)
            action = String.format(action, objects);
        setTail("\1ACTION " + action + "\1");
        return this;
    }
}