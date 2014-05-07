/*
 * DBUtils.java
 * 
 * Copyright (c) 2011 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;


/**
 * @author cjohnson
 * Created: Sep 2, 2011 11:23:21 AM
 */
public class DBUtils {
    public static void setStringOrNull(PreparedStatement ps, int index, String s) throws SQLException {
        if (s==null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, s);
        }
    }
    
    public static void setIntOrNull(PreparedStatement ps, int index, Integer i) throws SQLException {
        if (i==null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, i);
        }
    }
    
    public static void setIntPositiveOrNull(PreparedStatement ps, int index, int i) throws SQLException {
        if (i<=0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, i);
        }
    }
}
