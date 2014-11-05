/*
 * Lists.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.util;

import java.util.List;

/**
 *
 * @author cjohnson
 */
public class Lists {
    private Lists() {}
    
    /**
     * Merges 2 lists as efficiently as possible.  If either argument is null or empty,
     * the other is returned, favoring the first if both are.
     * 
     * If both are non-empty, then the second list is added to the first and the first is returned.
     * 
     * @param <E>
     * @param first
     * @param second
     * @return returns either the first list or the second list, possibly modifying the first
     */
    public static <E> List<E> mutableMerge(List<E> first, List<E> second) {
        if (first == null || first.isEmpty()) {
            if (second != null && !second.isEmpty()) {
                return second;
            } else {
                return first;
            }
        } else if (second == null || second.isEmpty()) {
            return first;
        } else {
            first.addAll(second);
            return first;
        }
    }
}
