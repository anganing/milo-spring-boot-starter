# Introduce

`milo-spring-boot-starter` is a Spring Boot starter of milo（https://github.com/eclipse/milo）

## SpringBoot usage

SpringBoot2.x or SpringBoot3.x is all OK.

your pom.xml:

```xml
<dependency>
    <groupId>com.iboot</groupId>
    <artifactId>milo-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

your appliaction.propeties (or appliaction.yml):
```properties
iboot.milo.enabled=true
# config is a map, the 'rgv' is a string key of client name, you can define it to what you need, such as 'line', 'agv' etc.
iboot.milo.config.rgv.endpoint=opc.tcp://milo.digitalpetri.com:62541/milo
iboot.milo.config.rgv.security-policy=none
#iboot.milo.config.rgv.username=admin
#iboot.milo.config.rgv.password=123456

```

## Use MiloService to operate OPC-UA Node
**when you read/write/subscribe OPC-UA data with MiloService, 
you can use specific OPC-UA Client or use default OPC-UA Client (the server config map's first client)**
1. Inject MiloService Service

```java
import com.iboot.iot.milo.MiloService;

import javax.annotation.Resource;

@Resource
private MiloService miloService;
```

2. See `com.iboot.iot.milo.MiloServiceTests` test cases how to read/write/subscribe Data
```java
package com.iboot.iot.milo;

import com.google.common.collect.Lists;
import com.iboot.iot.milo.configuration.MiloAutoConfiguration;
import com.iboot.iot.milo.core.model.OpcUaReadWriter;
import com.iboot.iot.milo.core.model.OpcUaWriter;
import com.iboot.iot.milo.utils.VariantUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Slf4j
@TestPropertySource("classpath:test.properties")
@SpringBootTest(classes = {MiloAutoConfiguration.class})
class MiloServiceTests {
    @Resource
    MiloService miloService;

    @Test
    void miloAutoConfigurationTest() {
        Assert.notNull(miloService, "the instance of MiloService shouldn't be null");
    }

    @Test
    void miloServiceReadTest() {
        OpcUaReadWriter readWriter = miloService.readFromOpcUa("ns=2;s=Dynamic/RandomDouble", "rgv");
        Object value = readWriter.getValue();
        Assert.isInstanceOf(Double.class, value);
    }

    @Test
    void miloServiceWriteTest() throws Exception {
        OpcUaWriter writer = new OpcUaWriter()
                .setIdentifier("ns=2;i=2003")
                .setVariant(VariantUtil.buildVariant(6, "256"));
        miloService.writeSpecifyType(writer, "rgv");

        OpcUaReadWriter readWriter = miloService.readFromOpcUa("ns=2;i=2003", "rgv");

        Assert.isTrue(Objects.equals(readWriter.getValue(), writer.getVariant().getValue()),"write value not equals read value from same identifier");
    }

    @Test
    void miloServiceWrite2Test() throws Exception {
        OpcUaWriter writer = new OpcUaWriter()
                .setIdentifier("ns=2;i=2003")
                .setVariant(VariantUtil.buildVariant(6, "185"));
        miloService.writeSpecifyType(writer);

        OpcUaReadWriter readWriter = miloService.readFromOpcUa("ns=2;i=2003");

        Assert.isTrue(Objects.equals(readWriter.getValue(), writer.getVariant().getValue()),"write value not equals read value from same identifier");
    }

    @Test
    void Test() {
        List<String> ids = Lists.newArrayList("ns=2;s=Dynamic/RandomDouble");
        miloService.subscriptionFromOpcUa(ids, "rgv", ((identifier, value) -> {
            log.info("identifier:'{}' , value:{}", identifier, value);
        }));
    }
}

```