package com.iboot.iot.milo.core.manage;

import com.iboot.iot.milo.configuration.MiloProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class OpcUaClientManager {
    private final Map<MiloProperties.Config, OpcUaClient> connectionPool = new ConcurrentHashMap<>();

    private final OpcUaClientFactory factory;

    private final MiloProperties properties;

    public OpcUaClientManager(OpcUaClientFactory factory, MiloProperties properties) {
        this.factory = factory;
        this.properties = properties;
        initConnectionPool();
    }

    private void initConnectionPool() {
        log.info("init OpcUaClientConnectionPool...");
        properties.getConfig().forEach((key, config) -> {
            try {
                OpcUaClient opcUaClient = factory.make(config);
                connectionPool.put(config, opcUaClient);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        log.info("init OpcUaClientConnectionPool successfully");
    }

    @PreDestroy
    public void closeAllClients() {
        connectionPool.forEach((clientName, client) -> {
            if (client != null) {
                try {
                    log.info("Disconnect OPC-UA client: {}", clientName);
                    client.disconnect().get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public OpcUaClient borrowClient(MiloProperties.Config config) {
        return connectionPool.get(config);
    }

    public void returnClient(MiloProperties.Config config, OpcUaClient client) {
        // todo I haven't figured out how to deal with the client yet, nothing is OK
    }
}
