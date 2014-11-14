/*
 * JmsTestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.services.devtest;

import com.rhythm.louie.DAO;

/**
 *
 * @author cjohnson
 */
@DAO
public class DevTestDAO implements DevTestService {

    @Override
    public String messageTest(String message) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean sendEmail(String sender, String receiver, String subject, String body) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
