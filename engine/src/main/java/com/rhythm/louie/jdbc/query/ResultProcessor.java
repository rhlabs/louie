/*
 * ResultProcessor.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc.query;

import java.sql.ResultSet;
import java.util.Collection;

/**
 *
 * @author cjohnson
 * @param <T> Type result that will be returned
 */
public interface ResultProcessor<T> {
    public void processResults(ResultSet rst, Collection<T> results) throws Exception;
}
