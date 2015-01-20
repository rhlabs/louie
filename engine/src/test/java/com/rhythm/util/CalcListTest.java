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
package com.rhythm.util;

import com.rhythm.louie.util.CalcList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class CalcListTest {
    
    public CalcListTest() {
    }

    @Test
    public void testIter() {
        List<Future<String>> results = new ArrayList<Future<String>>();
        
        List<String> args = Lists.newArrayList("cat","dog","blah","hello");
        List<String> calcList = new CalcList<String,String>(new TestFunction(), args);
        
        for (String s : calcList) {
            System.out.println("Item : "+s);
        }
    }

    private class TestFunction implements Function<String,String> {
        
        @Override
        public String apply(String s) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FutureListTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            char[] chars =  s.toCharArray();
            char[] rchars = new char[chars.length];
            for (int i=0;i<chars.length;i++) {
                rchars[chars.length-1-i]=chars[i];
            }
            String r = new String(rchars);
            System.out.println("Producing : "+r);
            return r;
        }
    }

}
