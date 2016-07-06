/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.exceptions;

import com.archebot.utilities.Element;

public class ReadonlyException extends RuntimeException {

    private final Element element;

    public ReadonlyException(Element element) {
        super("Error modifying '" + element.getTag() + "': Element is set to readonly.");
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
