package com.iboot.iot.milo.core.runner;

import com.iboot.iot.milo.core.model.OpcUaReadWriter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

import java.util.ArrayList;
import java.util.List;

import static com.iboot.iot.milo.configuration.Constants.READ_VALUE_MAX_AGE;

@Slf4j
public class ReadValuesRunner {
    /**
     * OPC-UA Node Ids which you will read
     */
    private final List<String> identifiers;

    public ReadValuesRunner(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<OpcUaReadWriter> run(OpcUaClient opcUaClient) {
        List<OpcUaReadWriter> entityList = new ArrayList<>();
        try {
            List<NodeId> nodeIds = new ArrayList<>();
            identifiers.forEach(identifier -> nodeIds.add(NodeId.parseOrNull(identifier)));
            List<DataValue> dataValues = opcUaClient.readValues(READ_VALUE_MAX_AGE, TimestampsToReturn.Both, nodeIds).get();
            if (dataValues.size() == identifiers.size()) {
                for (int i = 0; i < identifiers.size(); i++) {
                    String id = identifiers.get(i);
                    Object value = dataValues.get(i).getValue().getValue();
                    StatusCode status = dataValues.get(i).getStatusCode();
                    assert status != null;
                    if (status.isGood()) {
                        log.info("Read node:'{}', value:{}", id, value);
                    }
                    entityList.add(OpcUaReadWriter.builder()
                            .identifier(id)
                            .value(value)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("An exception occurred while reading the value:{}", e.getMessage(), e);
        }
        return entityList;
    }
}
