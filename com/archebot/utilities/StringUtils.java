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
import java.util.Collection;
import java.util.List;

/**
 * This utility class provides a selection of methods for manipulating Strings.
 * No instances of this class should exist or be used in programs.
 * Note: Prior to ArcheBot 1.18, this file was included in an external utilities library.
 * Note: Prior to ArcheBot 1.2, this file was named StringManager.
 *
 * @author Perrin Swanson
 * @see java.lang.String
 * @see java.lang.StringBuilder
 * @see java.util.Collection
 * @since ArcheBot 1.18
 */
public final class StringUtils {

    /**
     * Private, empty constructor - no class objects should be created.
     */
    private StringUtils() {}

    /**
     * Joins the elements of an array into a single string.
     * Uses a default starting index of 0 and a default String separator of " ".
     *
     * @param args the array to be compacted
     * @return a string of all the elements in the array
     * @see #compact(Object[], int, String)
     */
    public static String compact(Object[] args) {
        return compact(args, 0);
    }

    /**
     * Joins the elements of an array into a single string.
     * Uses a default String separator of " ".
     *
     * @param args the array to be compacted
     * @param start the index to start compacting from
     * @return a string of all the elements in the array
     * @see #compact(Object[], int, String)
     */
    public static String compact(Object[] args, int start) {
        return compact(args, start, " ");
    }

    /**
     * Joins the elements of an array into a single string.
     * Uses a default starting index of 0.
     *
     * @param args the array to be compacted
     * @param separator the string to be placed between each element
     * @return a string of all elements in the array
     * @see #compact(Object[], int, String)
     */
    public static String compact(Object[] args, String separator) {
        return compact(args, 0, separator);
    }

    /**
     * Joins the elements of an array into a single string.
     *
     * @param args the array to be compacted
     * @param start the index to start compacting from
     * @param separator the string to be placed between each element
     * @return a string of all elements in the array
     */
    public static String compact(Object[] args, int start, String separator) {
        StringBuilder builder = new StringBuilder(args[start++].toString());
        for (int i = start; i < args.length; i++) {
            builder.append(separator);
            builder.append(args[i]);
        }
        return builder.toString();
    }

    /**
     * Joins the elements of a collection into a single String.
     * Uses a default separator of ", ".
     *
     * @param args the collection to be compacted
     * @param <S> the type parameter of the collection
     * @return a string of all elements in the collection
     * @see #compact(Collection, String)
     */
    public static <S> String compact(Collection<S> args) {
        return compact(args, ", ");
    }

    /**
     * Joins the elements of a collection into a single String.
     *
     * @param args the collection to be compacted
     * @param separator the string to be places between each element
     * @param <S> the type parameter of the collection
     * @return a string of all elements in the collection
     */
    public static <S> String compact(Collection<S> args, String separator) {
        StringBuilder builder = new StringBuilder();
        for (S arg : args) {
            builder.append(separator);
            builder.append(arg);
        }
        if (args.size() > 0)
            return builder.substring(separator.length());
        return builder.toString();
    }

    /**
     * Formats a string based on whether a value is equal to one.
     * By default, includes value in returned string.
     *
     * @param value the value to be checked
     * @param singular the string to use if the value is one
     * @param plural the string to use if the value is not one
     * @return the resulting singular or plural string
     */
    public static String formatQuantity(int value, String singular, String plural) {
        return formatQuantity(value, singular, plural, true);
    }

    /**
     * Formats a string based on whether a value is equal to one.
     *
     * @param value the value to be checked
     * @param singular the string to use if the value is one
     * @param plural the string to use if the value is not one
     * @param includeValue whether the value should be included in the returned string
     * @return the resulting singular or plural string
     */
    public static String formatQuantity(int value, String singular, String plural, boolean includeValue) {
        if (value == 1)
            return (includeValue ? "1 " : "") + singular;
        return (includeValue ? value + " " : "") + plural;
    }

    /**
     * Repeats a string.
     *
     * @param string the string to be repeated
     * @param repeat the number of times to repeat the string
     * @return the repeated string
     */
    public static String repeat(String string, int repeat) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++)
            builder.append(string);
        return builder.toString();
    }

    /**
     * Converts a string into a String array by splitting at spaces outside of the deliminator character.
     * Uses a default delimiting character of '"' (double quotation mark).
     *
     * @param string the string to split
     * @return a String array of the split string
     * @see #splitArgs(String, char)
     */
    public static String[] splitArgs(String string) {
        return splitArgs(string, '"');
    }

    /**
     * Converts a string into a String array by splitting at spaces outside of the deliminator character.
     * Note that the split character is not the character at which the string is split. Instead, it is split at
     * space characters that do not fall between deliminators.
     * Deliminators can be escaped with the \ (backslash) character. Non-escaped deliminators are removed.
     *
     * @param string the string to split
     * @param split the character delimiting groups of words
     * @return a String array of the split string
     */
    public static String[] splitArgs(String string, char split) {
        StringBuilder builder = new StringBuilder();
        List<String> strings = new ArrayList<>();
        boolean b = false;
        for (char c : string.replace("\\" + split, "\0").toCharArray()) {
            if (!b && c == ' ') {
                strings.add(builder.toString());
                builder.setLength(0);
            } else if (c == split)
                b = !b;
            else if (c == '\0')
                builder.append(split);
            else
                builder.append(c);
        }
        if (builder.length() > 0)
            strings.add(builder.toString());
        return strings.toArray(new String[strings.size()]);
    }

    /**
     * Converts a string into a String array by splitting at spaces outside of deliminator characters.
     * In order to preserve ordering, this method keeps track of how many times it has encountered the starting
     * character, and waits to encounter the same number of closing characters before continuing to split.
     * Start and end deliminators can be escaped with the \ (backslash) character. Non-escaped deliminators are removed.
     *
     * @param string the string to split
     * @param first the character delimiting the start of a group of words
     * @param last the character delimiting the end of a group of words
     * @return a String array of the split string
     */
    public static String[] splitArgs(String string, char first, char last) {
        if (first == last)
            return splitArgs(string, first);
        StringBuilder builder = new StringBuilder();
        string = string.replace("\\" + first, "\0").replace("\\" + last, "\1");
        List<String> strings = new ArrayList<>();
        int i = 0;
        for (char c : string.toCharArray()) {
            if (i == 0 && c == ' ') {
                strings.add(builder.toString());
                builder.setLength(0);
            } else if (c == first) {
                if (i++ != 0)
                    builder.append(c);
            } else if (c == last) {
                if (--i != 0)
                    builder.append(c);
            } else if (c == '\0')
                builder.append(first);
            else if (c == '\1')
                builder.append(last);
            else
                builder.append(c);
        }
        if (builder.length() > 0)
            strings.add(builder.toString());
        return strings.toArray(new String[strings.size()]);
    }

    /**
     * Converts a string into a boolean value.
     *
     * @param string the string to convert
     * @return true if the string is equal to "true"
     */
    public static boolean toBoolean(String string) {
        return string.toLowerCase().equals("true");
    }
}
