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
