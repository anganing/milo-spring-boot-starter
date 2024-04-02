package com.iboot.iot.milo.core.runner.subscription;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class SubscriptionRunner {
    /**
     * OPC-UA NodeIds
     */
    private final List<String> identifiers;

    private final double samplingInterval;

    public SubscriptionRunner(List<String> identifiers) {
        this.identifiers = identifiers;
        this.samplingInterval = 1000.0D;
    }

    public SubscriptionRunner(List<String> identifiers, double samplingInterval) {
        this.identifiers = identifiers;
        this.samplingInterval = samplingInterval;
    }

    public void run(OpcUaClient opcUaClient, SubscriptionCallback callback) {
        final CountDownLatch downLatch = new CountDownLatch(1);

        // Add a subscription listener to handle subscription issues after disconnection and reconnection
        opcUaClient.getSubscriptionManager().addSubscriptionListener(new CustomSubscriptionListener(opcUaClient, callback));

        // Handle the subscription logic
        handle(opcUaClient, callback);

        try {
            // Continuous monitoring
            downLatch.await();
        } catch (Exception e) {
            log.error("An exception occurred while subscribing：{}", e.getMessage(), e);
        }
    }

    private void handle(OpcUaClient opcUaClient, SubscriptionCallback callback) {
        try {
            // Create subscription
            ManagedSubscription subscription = ManagedSubscription.create(opcUaClient, samplingInterval);
            subscription.setDefaultSamplingInterval(samplingInterval);
            subscription.setDefaultQueueSize(UInteger.valueOf(10));

            List<NodeId> nodeIdList = new ArrayList<>();
            for (String identifier : identifiers) {
                nodeIdList.add(NodeId.parseOrNull(identifier));
            }
            List<ManagedDataItem> dataItemList = subscription.createDataItems(nodeIdList);
            for (ManagedDataItem dataItem : dataItemList) {
                dataItem.addDataValueListener((item) -> callback.onSubscribe(dataItem.getNodeId().getIdentifier().toString(), item.getValue().getValue()));
            }
        } catch (Exception e) {
            log.error("An exception occurred while subscribing：{}", e.getMessage(), e);
        }
    }

    private class CustomSubscriptionListener implements UaSubscriptionManager.SubscriptionListener {
        private final OpcUaClient client;
        private final SubscriptionCallback callback;

        public CustomSubscriptionListener(OpcUaClient client, SubscriptionCallback callback) {
            this.client = client;
            this.callback = callback;
        }

        /**
         * When reconnecting, this method will be called when trying to restore the previous subscription fails.
         *
         * @param uaSubscription subscription
         * @param statusCode     statusCode
         */
        @Override
        public void onSubscriptionTransferFailed(UaSubscription uaSubscription, StatusCode statusCode) {
            log.info("Failed to restore subscription, need to resubscribe");
            // Resubscribe in callback method
            handle(client, callback);
        }

        @Override
        public void onKeepAlive(UaSubscription subscription, DateTime publishTime) {
            log.info("Subscription status is healthy...");
        }
    }
}
