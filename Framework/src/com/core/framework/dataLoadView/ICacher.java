package com.core.framework.dataLoadView;

/**
 * Created by paulz.
 */
public interface ICacher {

    public void cache(String key, String data);

    public String getCache(String key);

    public String getCachedData(String key);

}
