/*
 * CacheManager.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 */
public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private static net.sf.ehcache.CacheManager defaultEhcacheManager;
    
    private static final Map<String,CacheManager> cacheManagers =
            Collections.synchronizedMap(new TreeMap<String,CacheManager>());
    
    private net.sf.ehcache.CacheManager ehCacheManager;
    private final Map<String,Cache<?,?>> caches;
    private String name;
    
    private CacheManager(String name) {
        caches = new TreeMap<String,Cache<?,?>>();
        this.name = name;
    }
    
    public synchronized static void shutdown() {
        if (defaultEhcacheManager!=null) {
            defaultEhcacheManager.shutdown();
        }
        for (CacheManager manager : cacheManagers.values()) {
            if (manager.ehCacheManager!=null && manager.ehCacheManager!=defaultEhcacheManager) {
                manager.ehCacheManager.shutdown();
            }
        }
        cacheManagers.clear();
    }
    
    public static synchronized CacheManager createEhCacheManager(String name, URL ehConfigURL) {
        CacheManager cm = createCacheManager(name);
        cm.ehCacheManager =  new net.sf.ehcache.CacheManager(ehConfigURL);
        return cm;
    }
    
    public static synchronized CacheManager createCacheManager(String name) {
        if (cacheManagers.containsKey(name)) {
            logger.warn("CacheManager \""+name+"\" already exists!");
            return cacheManagers.get(name);
        }
        
        CacheManager cm = new CacheManager(name);
        cacheManagers.put(name, cm);
        return cm;
    }
    
    public static CacheManager createIfNeeded(String name) {
        CacheManager cm = cacheManagers.get(name);
        if (cm != null) {
            return cm;
        } else {
            return createCacheManager(name);
        }
    }
    
    
    public static CacheManager getCacheManager(String name) {
        return cacheManagers.get(name);
    }
    
    public static Collection<CacheManager> getCacheManagers() {
        return cacheManagers.values();
    }
    
    synchronized public static net.sf.ehcache.CacheManager getDefaultEhcacheManager() {
        if (defaultEhcacheManager == null) {
            defaultEhcacheManager = new net.sf.ehcache.CacheManager(CacheManager.class.getResource("ehcache.xml"));
        }
        return defaultEhcacheManager;
    }
    
    public String getName() {
        return name;
    }
    
    // Unsafe, must cleanup all managers
//    public void setEhcacheManager(net.sf.ehcache.CacheManager cacheManager) {
//        ehCacheManager = cacheManager;
//    }
//    public void loadEhcacheManager(URL configUrl) {
//        ehCacheManager = new net.sf.ehcache.CacheManager(configUrl);
//    }
//   
    public <K, V> NoCache<K, V> noCache(String cacheName) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            NoCache<K, V> cache = new NoCache<K, V>(cacheName);
            caches.put(cacheName, cache);
            return cache;
        }
    }

    public <K, V> SimpleCache<K, V> simpleCache(String cacheName) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            SimpleCache<K, V> cache = new SimpleCache<K, V>(cacheName);
            caches.put(cacheName, cache);
            return cache;
        }
    }

    public <K, V> EhCache<K, V> createEHCache(String cacheName) {
        synchronized (caches) {
            if (ehCacheManager == null) {
                ehCacheManager = getDefaultEhcacheManager();
            }
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            EhCache<K, V> cache = new EhCache<K, V>(cacheName, ehCacheManager);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <V> SingletonCache<V> singletonCache(String cacheName) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            SingletonCache<V> cache = new SingletonCache<V>(cacheName);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <V> SingletonNoCache<V> singletonNoCache(String cacheName) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            SingletonNoCache<V> cache = new SingletonNoCache<V>(cacheName);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <V> SingletonEhCache<V> singletonEhCache(String cacheName) {
        synchronized (caches) {
            if (ehCacheManager == null) {
                ehCacheManager = getDefaultEhcacheManager();
            }
            
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            SingletonEhCache<V> cache = new SingletonEhCache<V>(cacheName,ehCacheManager);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <K, V> GuavaCache<K,V> createGuavaCache(String cacheName, CacheBuilder bldr) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }
            
            GuavaCache<K,V> cache = new GuavaBasicCache<K,V>(cacheName, bldr);
            caches.put(cacheName, cache);
            return cache;
        }
    }

    public <K, V> GuavaCache<K,V> createGuavaLoadingCache(String cacheName, CacheBuilder bldr, CacheLoader loader) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }
            
            GuavaCache<K,V> cache = new GuavaLoadingCache<K,V>(name, bldr, loader);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <V> GuavaSingletonLoadingCache<V> createGuavaSingletonLoadingCache(String cacheName, CacheBuilder bldr, CacheLoader loader) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            GuavaSingletonLoadingCache<V> cache = new GuavaSingletonLoadingCache<V>(cacheName,bldr,loader);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public <V> GuavaSingletonCache<V> createGuavaSingletonCache(String cacheName, CacheBuilder bldr) {
        synchronized (caches) {
            if (caches.containsKey(cacheName)) {
                logger.warn("Cache \"" + name + ":" + cacheName + "\" already exists!");
            }

            GuavaSingletonCache<V> cache = new GuavaSingletonCache<V>(cacheName,bldr);
            caches.put(cacheName, cache);
            return cache;
        }
    }
    
    public Collection<Cache<?,?>> getCaches() {
        return caches.values();
    }
    
    public Cache<?,?> getCache(String cacheName) {
        return caches.get(cacheName);
    }
    
    public void clearAllCaches() throws Exception {
        for (Cache<?,?> cache : caches.values()) {
            cache.clear();
        }
    }
}
