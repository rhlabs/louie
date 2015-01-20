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
package com.rhythm.louie.service.layer;

import java.lang.annotation.Annotation;
import java.util.List;

import com.rhythm.louie.CacheDelegate;
import com.rhythm.louie.Classes;
import com.rhythm.louie.DAO;
import com.rhythm.louie.Router;

/**
 *
 * @author cjohnson
 */
public class AnnotatedServiceLayer implements ServiceLayer {
    
    public static final AnnotatedServiceLayer DAO = new AnnotatedServiceLayer(DAO.class);
    public static final AnnotatedServiceLayer ROUTER = new AnnotatedServiceLayer(Router.class);
    public static final AnnotatedServiceLayer CACHE = new AnnotatedServiceLayer(CacheDelegate.class);
    
    private final Class<? extends Annotation> annotation;
    public AnnotatedServiceLayer(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }
            
    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadLayer(Class<T> service) throws Exception {
        // search for the class tagged with the annotation
        List<Class<?>> classes = Classes.getTypesAnnotatedWith(service.getPackage().getName(), annotation);
        if (classes.size() > 1) {
            throw new Exception("Multiple @" + annotation.getSimpleName() + " classes found");
        } else if (classes.isEmpty()) {
            throw new Exception("Could not find a @" + annotation.getSimpleName() + " class");
        }
        
        Class<?> layer  = classes.iterator().next();
        return (T) layer.newInstance();
    }
}
