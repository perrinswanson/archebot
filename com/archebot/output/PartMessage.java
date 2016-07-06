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

public class PartMessage extends Output {

    public PartMessage(Channel channel) {
        this(channel.getName());
    }

    public PartMessage(String channel) {
        super("PART");
        setChannel(channel);
    }

    public PartMessage(Channel channel, String reason, Object... objects) {
        this(channel.getName(), reason, objects);
    }

    public PartMessage(String channel, String reason, Object... objects) {
        super("PART");
        setChannel(channel);
        setReason(reason, objects);
    }

    public PartMessage setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public PartMessage setChannel(String channel) {
        setArgs(channel);
        return this;
    }

    public PartMessage setReason(String reason, Object... objects) {
        if (objects.length > 0)
            reason = String.format(reason, objects);
        setTail(reason);
        return this;
    }
}
