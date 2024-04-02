package com.iboot.iot.milo.utils;

import com.iboot.iot.milo.configuration.MiloProperties;
import com.iboot.iot.milo.core.exception.MiloPropertiesEndPointNullPointerException;
import com.iboot.iot.milo.core.exception.MiloPropertiesNullPointerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author tangsc
 * @version 0.0.1
 * @desc
 * @since 2020/4/13
 */
@Slf4j
public class MiloPropertiesUtil {
    private MiloPropertiesUtil() {
    }

    public static void verifyProperties(MiloProperties properties) {
        if (properties.getConfig().isEmpty()) {
            throw new MiloPropertiesNullPointerException("the Milo Properties is Null or Empty");
        }
        properties.getConfig().forEach((clientName, config) -> {
            if (!StringUtils.hasText(config.getEndpoint())) {
                throw new MiloPropertiesEndPointNullPointerException( "the Milo Property's Endpoint is Empty: " + clientName);
            }
        });
    }

    public static MiloProperties.Config getConfig(MiloProperties properties, String clientName) {
        Map<String, MiloProperties.Config> config = properties.getConfig();
        if (StringUtils.hasText(clientName)) {
            return config.get(clientName);
        }

        Iterator<MiloProperties.Config> iterator = config.values().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("Config map is empty");
    }

    /**
     * get the default config of OPC-UA Server list
     * @param properties
     * @return
     */
    public static MiloProperties.Config getConfig(MiloProperties properties) {
        return getConfig(properties, null);
    }
}
