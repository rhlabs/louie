/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
