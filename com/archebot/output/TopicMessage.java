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

public class TopicMessage extends Output {

    public TopicMessage(Channel channel, String topic, Object... objects) {
        this(channel.getName(), topic, objects);
    }

    public TopicMessage(String channel, String topic, Object... objects) {
        super(String.format("TOPIC %s :%s", channel, objects.length > 0 ? String.format(topic, objects) : topic));
    }

    public TopicMessage setChannel(Channel channel) {
        return setChannel(channel.getName());
    }

    public TopicMessage setChannel(String channel) {
        setArgs(channel);
        return this;
    }

    public TopicMessage setTopic(String topic, Object... objects) {
        if (objects.length > 0)
            topic = String.format(topic, objects);
        setTail(topic);
        return this;
    }
}
