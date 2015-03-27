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
package com.rhythm.louie.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cjohnson
 */
public class JsonTest {
    // TODO needs to be flushed out
    
    public JsonTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

//    message JsonRequestPB {
//    optional string user  = 1;
//    optional string agent = 2;
//    optional string system = 3;
//    optional string method = 4;
//    repeated string types   = 5;
//    repeated JsonArgPB params  = 6;
//}
//
//message JsonArgPB {
//    repeated string arg = 1;
//}

    @Test
    public void testJsonV1() {
        try {
            URL url = new URL("http://localhost:8080/louie/json");
            
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(30*1000);
            connection.setConnectTimeout(15*1000);
            connection.connect();
            
            String jsonRequest = "{'version':'1','user':'cjohnson',"
                    + "'agent':'JUNIT_TEST','system':'status','method':'echoTest',"
                    + "'types':['louie.StringPB','louie.IntPB'],"
                    + "'params':[{'arg':['{\\'value\\':\\'JSON Echo Test v1\\'}','{\\'value\\':10}']}]}";
            System.out.println(jsonRequest);
            
            try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
                wr.write(jsonRequest);
                wr.flush();
            }
            
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            System.out.println("JSON RESPONSE: "+sb.toString());
        } catch (Exception ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void testJsonV2() {
        try {
            URL url = new URL("http://localhost:8080/louie/json");
            
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(30*1000);
            connection.setConnectTimeout(15*1000);
            connection.connect();
            
            String jsonRequest = "{'version':'2','user':'cjohnson','agent':'JUNIT_TEST',"
                    + "'system':'status','method':'echoTest',"
                    + "'params':[{'type':'louie.StringPB','value':{'value':'JSON Echo Test v2'}},"
                    + "          {'type':'louie.IntPB','value':{'value':10}}]}";
            
             
            System.out.println(jsonRequest);
            
            try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
                wr.write(jsonRequest);
                wr.flush();
            }
            
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            System.out.println("JSON RESPONSE: "+sb.toString());
        } catch (Exception ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
