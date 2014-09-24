/*
 * JmsTestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.testservice.jms;

import com.rhythm.louie.DAO;

/**
 *
 * @author cjohnson
 */
@DAO
public class JmsTestDAO implements JmsTestService {

    @Override
    public String messageTest(String message) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
