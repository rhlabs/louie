/*
 * TestDAO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.testservice;

import java.util.ArrayList;
import java.util.List;

import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.request.RequestContext;

/**
 *
 * @author cjohnson
 */
public class TestDAO implements TestClient {
    public TestDAO() {
    }
 
    @Override
    public String echoTest(String value, Integer sleep) throws Exception {
        String key = RequestContext.getRequest().getSessionKey();
        
        Thread.sleep(sleep);
        
        if (!RequestContext.getRequest().getSessionKey().equals(key)) {
            throw new Exception("Context does not work!!!");
        }
        return value;
    }

    @Override
    public String loopTest(List<String> hosts) throws Exception {
        if (hosts.isEmpty()) {
            return "Done";
        }
        List<String> args = new ArrayList<String>(hosts);
        String host = args.remove(0);
        TestClient client = TestClientFactory.getClient(
                LouieConnectionFactory.getConnection(host));
        return client.loopTest(args);
    }
}
