package com.core.framework.dataLoadView;

/**
 * Created by paulz.
 */
public interface IConsumer {

    public void onDataResponse(String data);

    public void onDataError(String msg, Throwable throwable);

}
