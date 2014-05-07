/*
 * ConnectionFactory.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.jdbc;

import java.sql.Connection;

/**
 *
 * @author cjohnson
 */
public interface ConnectionFactory {
    Connection createConnection() throws Exception;
}
