package com.iboot.iot.milo.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OpcUaReadWriter {
    /**
     * the alias of identifier
     */
    private String tag;

    /**
     * OPC-UA NodeId
     */
    private String identifier;

    private Object value;
}
