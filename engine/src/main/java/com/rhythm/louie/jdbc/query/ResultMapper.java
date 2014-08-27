/*
 * ResultMapper.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc.query;

import java.sql.ResultSet;
import java.util.Map;

/**
 *
 * @author cjohnson
 * 
 * @param <K>
 * @param <V>
 */
public interface ResultMapper<K,V> {
    public void processResults(ResultSet rst, Map<K,V> results) throws Exception;
}
