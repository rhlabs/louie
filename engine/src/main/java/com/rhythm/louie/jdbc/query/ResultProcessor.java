/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
