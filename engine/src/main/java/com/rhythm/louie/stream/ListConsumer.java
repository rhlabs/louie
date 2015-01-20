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
package com.rhythm.louie.stream;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eyasukoc
 * @param <T> Type of the returned object(s)
 */
public class ListConsumer<T> extends Consumer<T>{

    private final List<T> list = new ArrayList<T>();
    
    public ListConsumer() {}
    
    @Override
    public void informMessageCount(int count) {}

    @Override
    public void consume(T item) {
        list.add(item);
    }
    
    public List<T> get() {
        return list;
    }
    
}
