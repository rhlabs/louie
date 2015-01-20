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
