package com.iboot.iot.milo.configuration;

import lombok.Data;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.iboot.iot.milo.configuration.Constants.PREFIX;


@Data
@ConfigurationProperties(prefix = PREFIX)
public class MiloProperties {
    /**
     * whether to enable the component
     */
    private Boolean enabled;

    /**
     * OPC-UA Server list
     */
    private Map<String, Config> config = new ConcurrentHashMap<>();

    @Data
    public static class Config {
        /**
         * OPC-UA Server endpoint
         */
        private String endpoint;

        /**
         * OPC-UA connection SecurityPolicy
         */
        private SecurityPolicy securityPolicy = SecurityPolicy.None;

        private String username;

        private String password;
    }
}
