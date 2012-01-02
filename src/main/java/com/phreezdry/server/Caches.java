package com.phreezdry.server;

/**
 * @author petrovic May 23, 2010 8:57:07 AM
 */
public enum Caches {
    DOCUMENT("phreez.document.cache"),
    RATELIMIT("phreez.ratelimit.cache"),
    USER("phreez.user.cache");

    // key values must match cache names in ehcache.xml
    public final String key;

    Caches(String key) {
        this.key = key;
    }
}
