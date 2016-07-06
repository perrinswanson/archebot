/* Copyright (C) 2014-2016 Perrin Swanson | http://perrinswanson.com
 * This file is part of the ArcheBot Project Library.
 *
 * Distribution, implementation, and modification of this library and its contents
 * is free provided this copyright notice is not modified or removed.
 * All documentation referencing this library must acknowledge the original owner,
 * and any modifications made to the files must be fully documented.
 */
package com.archebot.utilities;

import com.archebot.exceptions.ReadonlyException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * PML (Property Markup Language) is a file format designed to be easily manageable through both text-based programs and
 * code-based applications. Each PML element consists of the tag, placed between < and > at the start of the line, and
 * the text content following it on that line. Child elements are indicated by indentation from the previous element.
 * All ArcheBot configurations are saved using PML files, and developers are encouraged to use it when making their
 * own bots.
 * Note: Prior to ArcheBot 1.18, this file and other PML source files were included in an external utilities library.
 *
 * @author Perrin Swanson
 * @version PML 1.4.1
 * @since ArcheBot 1.18
 */
public class Element implements Comparable<Element>, Iterable<Element> {

    public static final String VERSION = "1.4.1";
    private final String tag;
    private final TreeSet<Element> children = new TreeSet<>();
    private String content;
    private boolean readonly = false;
    private int indent = 2;
    private int index = -1;

    public Element(String tag) {
        this(tag, "");
    }

    public Element(String tag, String content) {
        if (!tag.matches("#|[\\w.-]+"))
            throw new IllegalArgumentException("Error creating '" + tag + "': Illegal characters in tag.");
        this.tag = tag;
        this.content = content;
    }

    public void addChild(Element element) {
        if (readonly)
            throw new ReadonlyException(this);
        if (element.index != -1)
            throw new IllegalArgumentException("Error modifying '" + tag + "': Element '" + element.tag + "' already has parent.");
        children.add(element);
        element.index = size(element.tag) - 1;
    }

    public Element getChild(String tag) {
        return getChild(tag, tag.equals("#") ? -1 : 0);
    }

    public Element getChild(String tag, int index) {
        if (tag.contains("/")) {
            String[] split = tag.split("/", 2);
            return getChild(split[0], index).getChild(split[1], index);
        }
        if (tag.contains(":")) {
            String[] parts = tag.split(":", 2);
            tag = parts[0];
            if (parts[1].matches("-?\\d+"))
                index = Integer.parseInt(parts[1]);
        }
        if (index >= 0) {
            while (!isChild(tag, index))
                addChild(new Element(tag));
            for (Element child : children)
                if (tag.equalsIgnoreCase(child.tag) && index == child.index)
                    return child;
        }
        Element child = new Element(tag);
        addChild(child);
        return child;
    }

    public TreeSet<Element> getChildren() {
        return new TreeSet<>(children);
    }

    public TreeSet<Element> getChildren(String tag) {
        return getChildren(e -> e.getTag().equalsIgnoreCase(tag));
    }

    public TreeSet<Element> getChildren(Predicate<Element> predicate) {
        return new TreeSet<>(children.stream().filter(predicate).collect(Collectors.toSet()));
    }

    public String getContent() {
        return content;
    }

    public int getIndex() {
        return index;
    }

    public int getIndent() {
        return indent;
    }

    public String getTag() {
        return tag;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean isChild(String tag) {
        return isChild(tag, 0);
    }

    public boolean isChild(String tag, int index) {
        if (tag.contains("/")) {
            String[] split = tag.split("/", 2);
            return isChild(split[0], index) && getChild(split[0], index).isChild(split[1], index);
        }
        if (tag.contains(":")) {
            String[] parts = tag.split(":", 2);
            tag = parts[0];
            if (parts[1].matches("\\d+"))
                index = Integer.parseInt(parts[1]);
        }
        for (Element child : children)
            if (tag.equalsIgnoreCase(child.tag) && index == child.index)
                return true;
        return false;
    }

    public boolean isChild(Element element) {
        return children.contains(element);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void removeChild(String tag) {
        removeChild(tag, 0);
    }

    public void removeChild(String tag, int index) {
        if (tag.contains("/")) {
            String[] split = tag.split("/", 2);
            if (isChild(split[0], index))
                getChild(split[0], index).removeChild(split[1], index);
        } else {
            if (tag.contains(":")) {
                String[] parts = tag.split(":", 2);
                tag = parts[0];
                if (parts[1].matches("\\d+"))
                    index = Integer.parseInt(parts[1]);
            }
            for (Element child : getChildren(tag))
                if (child.index == index) {
                    child.index = -1;
                    children.remove(child);
                } else if (child.index > index)
                    child.index--;
        }
    }

    public void removeChild(Element element) {
        if (!isChild(element))
            throw new IllegalArgumentException("Error modifying '" + tag + "': Not parent of element '" + element.tag + "'.");
        getChildren(e -> e.tag.equalsIgnoreCase(element.tag) && e.index > element.index).forEach(e -> e.index--);
        element.index = -1;
        children.remove(element);
    }

    public void removeChildren() {
        for (Element child : children)
            child.index = -1;
        children.clear();
    }

    public void removeChildren(String tag) {
        for (Element child : getChildren(tag)) {
            child.index = -1;
            children.remove(child);
        }
    }

    public void setContent(String content) {
        if (readonly)
            throw new ReadonlyException(this);
        this.content = content;
    }

    public void setIndent(int indent) {
        if (readonly)
            throw new ReadonlyException(this);
        if (indent < 1)
            throw new IllegalArgumentException("Error modifying '" + tag + "': Indent must be greater than 0." );
        this.indent = indent;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public int size() {
        return children.size();
    }

    public int size(String tag) {
        return size(e -> e.getTag().equalsIgnoreCase(tag));
    }

    public int size(Predicate<Element> predicate) {
        return (int) children.stream().filter(predicate).count();
    }

    public void write() throws IOException {
        write(tag);
    }

    public void write(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.endsWith(".pml") ? file : file + ".pml"));
        for (Element child : children)
            write(writer, child, 0);
        writer.close();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Element element) {
        if (tag.equalsIgnoreCase(element.tag))
            return Integer.compare(index, element.index);
        if (tag.matches("^-?\\d+$") && element.tag.matches("^-?\\d+$"))
            return Integer.compare(Integer.parseInt(tag), Integer.parseInt(element.tag));
        if (tag.matches("^[\\w.-]+-?\\d+$") && element.tag.matches("^[\\w.-]+-?\\d+$")
                && tag.replaceAll("-?\\d+$", "").equalsIgnoreCase(element.tag.replaceAll("-?\\d+$", "")))
            return Integer.compare(Integer.parseInt(tag.replaceAll("[\\w.-]+", "")), Integer.parseInt(element.tag.replaceAll("[\\w.-]+", "")));
        return tag.compareToIgnoreCase(element.tag);
    }

    @Override
    public Iterator<Element> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return (readonly ? "(" : "<") + tag + (readonly ? ") " : "> ") + content;
    }

    public static Element read(String file) throws IOException {
        String tag = file.contains(File.separator) ? file.substring(file.lastIndexOf(File.separatorChar)) : file;
        return read(file, tag.replaceAll("\\W", ""));
    }

    public static Element read(String filename, String tag) throws IOException {
        Element element = new Element(tag);
        File file = new File(filename.endsWith(".pml") ? filename : filename + ".pml");
        if (!file.exists())
            return element;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<Element> parents = new ArrayList<>();
        parents.add(element);
        while ((line = reader.readLine()) != null) {
            int indent = 0;
            while (line.startsWith(" ")) {
                indent++;
                line = line.substring(1);
            }
            int i = 0, k = 1;
            while (i < indent && k < parents.size() - 1)
                i += parents.get(k++).indent;
            if (indent - i <= 0 || parents.size() == 1)
                k--;
            Element parent = parents.get(k);
            while (parents.size() - 1 > k)
                parents.remove(k + 1);
            String[] parts = line.split(" ", 2);
            String t = parts[0];
            if (!t.matches("[<\\(](#|&|[\\w.-]+)[>\\)]"))
                continue;
            String content = parts.length > 1 ? parts[1].replaceAll("^ ", "") : "";
            if (t.matches("[<\\(]&[>\\)]"))
                parent.content += "\n" + content;
            else {
                Element e = new Element(t.substring(1, t.length() - 1), content);
                e.readonly = t.matches("\\((#|[\\w.-]+)\\)");
                parent.addChild(e);
                if (indent - i > 0)
                    parent.indent = indent - i;
                parents.add(e);
            }
        }
        return element;
    }

    private static void write(BufferedWriter writer, Element element, int indent) throws IOException {
        String[] parts = element.content.split("\\n");
        writer.write(StringUtils.repeat(" ", indent));
        writer.write(element.readonly ? "(" : "<");
        writer.write(element.tag);
        writer.write(element.readonly ? ") " : "> ");
        writer.write(parts[0]);
        writer.newLine();
        for (int i = 1; i < parts.length; i++) {
            writer.write(StringUtils.repeat(" ", indent + element.indent));
            writer.write(element.readonly ? "(&) " : "<&> ");
            writer.write(parts[i]);
            writer.newLine();
        }
        for (Element child : element)
            write(writer, child, indent + element.indent);
    }
}
