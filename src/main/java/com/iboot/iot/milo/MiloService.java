package com.iboot.iot.milo;


import com.iboot.iot.milo.configuration.MiloProperties;
import com.iboot.iot.milo.core.manage.OpcUaClientManager;
import com.iboot.iot.milo.core.model.OpcUaReadWriter;
import com.iboot.iot.milo.core.model.OpcUaWriter;
import com.iboot.iot.milo.core.runner.ReadValuesRunner;
import com.iboot.iot.milo.core.runner.WriteValuesRunner;
import com.iboot.iot.milo.core.runner.subscription.SubscriptionCallback;
import com.iboot.iot.milo.core.runner.subscription.SubscriptionRunner;
import com.iboot.iot.milo.utils.MiloPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MiloService {
    private final OpcUaClientManager connectPool;
    private final MiloProperties properties;

    public MiloService(OpcUaClientManager connectPool, MiloProperties properties) {
        this.connectPool = connectPool;
        this.properties = properties;
    }

    public void writeSpecifyType(OpcUaWriter writer) throws Exception {
        writeSpecifyType(Collections.singletonList(writer));
    }

    public void writeSpecifyType(OpcUaWriter writer, String clientName) throws Exception {
        writeSpecifyType(Collections.singletonList(writer), clientName);
    }

    public void writeSpecifyType(List<OpcUaWriter> writers) throws Exception {
        writeSpecifyType(writers, null);
    }

    public void writeSpecifyType(List<OpcUaWriter> writers, String clientName) {
        MiloProperties.Config config = MiloPropertiesUtil.getConfig(properties, clientName);
        WriteValuesRunner runner = new WriteValuesRunner(writers);
        OpcUaClient client = connectPool.borrowClient(config);
        if (client != null) {
            try {
                runner.run(client);
            } finally {
                connectPool.returnClient(config, client);
            }
        }
    }

    public OpcUaReadWriter readFromOpcUa(String identifiers) {
        return readFromOpcUa(identifiers, null);
    }

    public OpcUaReadWriter readFromOpcUa(String identifier, String clientName) {
        List<OpcUaReadWriter> entityList = readFromOpcUa(Collections.singletonList(identifier), clientName);
        if (!entityList.isEmpty()) {
            return entityList.get(0);
        }
        return null;
    }

    public List<OpcUaReadWriter> readFromOpcUa(List<String> identifiers) {
        return readFromOpcUa(identifiers, null);
    }

    public List<OpcUaReadWriter> readFromOpcUa(List<String> identifiers, String clientName) {
        MiloProperties.Config config = MiloPropertiesUtil.getConfig(properties, clientName);
        ReadValuesRunner runner = new ReadValuesRunner(identifiers);
        OpcUaClient client = connectPool.borrowClient(config);
        if (client != null) {
            try {
                return runner.run(client);
            } finally {
                connectPool.returnClient(config, client);
            }
        }
        return new ArrayList<>();
    }

    public void subscriptionFromOpcUa(List<String> identifiers, SubscriptionCallback callback) {
        subscriptionFromOpcUa(identifiers, 1000.0, callback);
    }

    public void subscriptionFromOpcUa(List<String> identifiers, String clientName, SubscriptionCallback callback) {
        subscriptionFromOpcUa(identifiers, 1000.0, clientName, callback);
    }

    public void subscriptionFromOpcUa(List<String> identifiers, double samplingInterval, SubscriptionCallback callback) {
        subscriptionFromOpcUa(identifiers, samplingInterval, null, callback);
    }

    public void subscriptionFromOpcUa(List<String> identifiers, double samplingInterval, String clientName, SubscriptionCallback callback) {
        MiloProperties.Config config = MiloPropertiesUtil.getConfig(properties, clientName);
        SubscriptionRunner runner = new SubscriptionRunner(identifiers, samplingInterval);
        OpcUaClient client = connectPool.borrowClient(config);
        if (client != null) {
            try {
                runner.run(client, callback);
            } finally {
                connectPool.returnClient(config, client);
            }
        }
    }
}
