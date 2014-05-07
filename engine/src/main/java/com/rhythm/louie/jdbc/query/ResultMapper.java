/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
