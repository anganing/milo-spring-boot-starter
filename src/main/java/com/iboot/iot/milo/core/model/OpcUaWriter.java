package com.iboot.iot.milo.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OpcUaWriter {
    /**
     * the alias of identifier
     */
    private String tag;

    /**
     * OPC-UA NodeId
     */
    private String identifier;

    private Variant variant;
}
