package org.axonframework.metrics;

public interface ResultCallBack {

    void onSuccess();

    void onFailure(String reason);
}
