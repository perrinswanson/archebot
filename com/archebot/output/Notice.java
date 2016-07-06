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

public class Notice extends Output {

    public Notice(Channel channel, String notice, Object... objects) {
        this(channel.getName(), notice, objects);
    }

    public Notice(User user, String notice, Object... objects) {
        this(user.getNick(), notice, objects);
    }

    public Notice(String target, String notice, Object... objects) {
        super("NOTICE");
        setTarget(target);
        setNotice(notice, objects);
    }

    public Notice setChannel(Channel channel) {
        return setTarget(channel.getName());
    }

    public Notice setUser(User user) {
        return setTarget(user.getNick());
    }

    public Notice setTarget(String target) {
        setArgs(target);
        return this;
    }

    public Notice setNotice(String notice, Object... objects) {
        if (objects.length > 0)
            notice = String.format(notice, objects);
        setTail(notice);
        return this;
    }
}
