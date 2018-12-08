package com.qh.common.utils;

import org.springframework.cache.ehcache.EhCacheCacheManager;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CacheUtils {
    
	private static String cache_loginSalt = "loginSalt";
	
	
    private static CacheManager cacheManager;


    public static String getLoginSalt(String key){
    	return (String) get(cache_loginSalt, key);
    }
    
    public static void setLoginSalt(String key,String value){
    	put(cache_loginSalt, key, value);
    }
    
    
    public static void setCacheManager(EhCacheCacheManager ehCacheCacheManager){
    	cacheManager = ehCacheCacheManager.getCacheManager();
    }
    /**
     * 获取缓存
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, String key) {
        Element element = getCache(cacheName).get(key);
        return element==null?null:element.getObjectValue();
    }
    /**
     * 获取缓存
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, Integer key) {
        Element element = getCache(cacheName).get(key);
        return element==null?null:element.getObjectValue();
    }

    /**
     * 获取缓存
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, Long key) {
        Element element = getCache(cacheName).get(key);
        return element==null?null:element.getObjectValue();
    }
    /**
     * 写入缓存
     * @param cacheName
     * @param key
     * @param value
     */
    public static void put(String cacheName, String key, Object value) {
        Element element = new Element(key, value);
        getCache(cacheName).put(element);
    }

    /**
     * 写入缓存
     * @param cacheName
     * @param key
     * @param value
     */
    public static void put(String cacheName, Long key, Object value) {
        Element element = new Element(key, value);
        getCache(cacheName).put(element);
    }
    /**
     * 写入缓存
     * @param cacheName
     * @param key
     * @param value
     */
    public static void put(String cacheName, Integer key, Object value) {
        Element element = new Element(key, value);
        getCache(cacheName).put(element);
    }
    /**
     * 从缓存中移除
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, String key) {
        getCache(cacheName).remove(key);
    }
    
    /**
     * 从缓存中移除
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, Integer key) {
        getCache(cacheName).remove(key);
    }
    /**
     * 获得一个Cache，没有则创建一个。
     * @param cacheName
     * @return
     */
    public static Cache getCache(String cacheName){
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null){
            cacheManager.addCache(cacheName);
            cache = cacheManager.getCache(cacheName);
            cache.getCacheConfiguration().setEternal(true);
        }
        return cache;
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }
    
}


