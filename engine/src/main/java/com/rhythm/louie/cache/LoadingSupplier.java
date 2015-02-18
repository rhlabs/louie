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

package com.rhythm.louie.cache;

import com.google.common.base.Supplier;

import org.slf4j.LoggerFactory;

/**
 * A supplier that is able to deal with loading operations that throw exceptions.
 * In the event that load() throws an exception, the previous value retrieved is returned.
 * 
 * @param <T> 
 */
public abstract class LoadingSupplier<T> implements Supplier<T> {

    private T previous = null;

    @Override
    public final T get() {
        try {
            previous = load();
        } catch (Exception e) {
            LoggerFactory.getLogger(LoadingSupplier.class).error("Error looking up supplier value!", e);
        }
        return previous;
    }
    
    /**
     * Load an instance of the appropriate type. May throw an exception that will be 
     * swallowed in the get().
     * 
     * @return the loaded value
     * @throws Exception if there was a problem loading the type.
     */
    abstract public T load() throws Exception;
}
