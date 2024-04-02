package com.iboot.iot.milo.core.runner.subscription;

public interface SubscriptionCallback {
    void onSubscribe(String identifier, Object value);
}
