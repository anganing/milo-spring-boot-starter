package com.iboot.iot.milo.configuration;

import com.iboot.iot.milo.MiloService;
import com.iboot.iot.milo.core.manage.OpcUaClientFactory;
import com.iboot.iot.milo.core.manage.OpcUaClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static org.yaml.snakeyaml.nodes.Tag.PREFIX;

@Slf4j
@Configuration
@EnableConfigurationProperties(MiloProperties.class)
@ConditionalOnClass({MiloService.class, OpcUaClientManager.class})
@ConditionalOnProperty(prefix = PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
public class MiloAutoConfiguration {
    private final MiloProperties properties;

    public MiloAutoConfiguration(MiloProperties properties) {
        this.properties = properties;
        printBanner(properties.getBanner());
    }

    /**
     * core auto-configuration
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({OpcUaClientManager.class})
    public OpcUaClientManager opcUaClientManager() {
        // OpcUaClientFactory can create a instance of OpcUaClient
        OpcUaClientFactory opcUaClientFactory = new OpcUaClientFactory(properties);

        // OpcUaClientManager will manage the instance of OpcUaClient, which created by the OpcUaClientFactory
        return new OpcUaClientManager(opcUaClientFactory, properties);
    }

    /**
     * register the bean miloService to Spring container
     * @param opcUaClientManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(MiloService.class)
    @DependsOn("opcUaClientManager")
    public MiloService miloService(OpcUaClientManager opcUaClientManager) {
        return new MiloService(opcUaClientManager, properties);
    }

    private void printBanner(Boolean banner) {
        if (!banner) {
            return;
        }
        System.out.print("  _ _                 _                        \n" +
                " (_) |__   ___   ___ | |_   ___ ___  _ __ ___  \n" +
                " | | '_ \\ / _ \\ / _ \\| __| / __/ _ \\| '_ ` _ \\ \n" +
                " | | |_) | (_) | (_) | |_ | (_| (_) | | | | | |\n" +
                " |_|_.__/ \\___/ \\___/ \\__(_)___\\___/|_| |_| |_|\n");
        System.out.println(":: milo-spring-boot-starter ::        (v1.0.0)\n");
    }
}
