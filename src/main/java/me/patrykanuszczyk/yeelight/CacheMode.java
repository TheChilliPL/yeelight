package me.patrykanuszczyk.yeelight;

public enum CacheMode {
    ANY,
    CACHE_ONLY,
    FORCE_CACHE_ONLY,
    FETCH_NEW;

    public boolean forcesCache() {
        return this == FORCE_CACHE_ONLY;
    }

    public boolean allowsCache() {
        return this != FETCH_NEW;
    }

    public boolean allowsCache(boolean cacheValid) {
        return forcesCache() || (cacheValid && allowsCache());
    }

    public boolean allowsFetchNew() {
        return this == ANY || this == FETCH_NEW;
    }
}
