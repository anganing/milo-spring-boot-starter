package com.iboot.iot.milo.core.runner;

import com.iboot.iot.milo.core.model.OpcUaWriter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class WriteValuesRunner {
    private final List<OpcUaWriter> opcUaWriters;

    public WriteValuesRunner(List<OpcUaWriter> opcUaWriters) {
        this.opcUaWriters = opcUaWriters;
    }

    public void run(OpcUaClient opcUaClient) {
        try {
            if (!opcUaWriters.isEmpty()) {
                List<NodeId> nodeIds = new LinkedList<>();
                List<DataValue> dataValues = new LinkedList<>();
                for (OpcUaWriter writer : opcUaWriters) {
                    nodeIds.add(NodeId.parseOrNull(writer.getIdentifier()));
                    dataValues.add(new DataValue(writer.getVariant(), null, null));
                }

                List<StatusCode> statusCodeList = opcUaClient.writeValues(nodeIds, dataValues).join();
                for (int i = 0; i < statusCodeList.size(); i++) {
                    if (statusCodeList.get(i).isGood()) {
                        log.info("Writing value {} to node '{}' successfully", dataValues.get(i).getValue(), nodeIds.get(i));
                    } else {
                        log.error("An error occurred while writing the value {}, to node '{}': {}", dataValues.get(i).getValue(), nodeIds.get(i), statusCodeList.get(i));
                    }
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while reading values in batches:{}", e.getMessage(), e);
        }
    }
}
