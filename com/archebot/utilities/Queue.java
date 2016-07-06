/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.utilities;

import java.util.ArrayList;

/**
 * A generic queue class. Objects that are removed in the order they are added.
 *
 * @param <O> the type of object found in the queue
 */
public class Queue<O> {

    private final ArrayList<O> items = new ArrayList<>();

    public Queue() {}

    public Queue(Queue<O> queue) {
        items.addAll(queue.items);
    }

    public void add(O item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public O getNext() {
        return items.remove(0);
    }

    public boolean hasNext() {
        return items.size() > 0;
    }

    public int size() {
        return items.size();
    }
}
