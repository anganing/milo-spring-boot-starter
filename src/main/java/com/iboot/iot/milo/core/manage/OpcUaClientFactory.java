package com.iboot.iot.milo.core.manage;

import com.iboot.iot.milo.configuration.MiloProperties;
import com.iboot.iot.milo.core.exception.MiloPropertiesNullPointerException;
import com.iboot.iot.milo.core.exception.IdentityProviderException;
import com.iboot.iot.milo.utils.MiloPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class OpcUaClientFactory {
    public OpcUaClientFactory(MiloProperties properties) {
        MiloPropertiesUtil.verifyProperties(properties);
    }

    /**
     * create OPC-UA client instance
     *
     * @return OpcUaClient
     * @throws Exception
     */
    public OpcUaClient make(MiloProperties.Config config) throws Exception {
        OpcUaClient client = null;
        try {
            client = createClient(config);
            client.connect().get();
            return client;
        } catch (Exception e) {
            if (client != null) {
                client.disconnect().get();
            }
            throw new InterruptedException(e.getMessage());
        }
    }

    private OpcUaClient createClient(MiloProperties.Config config) throws Exception {
        // verify the config again is necessary for create OPC-UA client with DB or other way dynamically
        verifyConfig(config);

        KeyStoreLoader loader = new KeyStoreLoader().load();

        return OpcUaClient.create(
                config.getEndpoint(),
                endpoints -> {
                    EndpointDescription description = endpoints.stream()
                            .findFirst().orElseThrow(() -> new MiloPropertiesNullPointerException("no desired endpoints returned"));
                    if (!description.getEndpointUrl().equals(config.getEndpoint())) {
                        URI uri = parseURI(config.getEndpoint());
                        description = EndpointUtil.updateUrl(description, uri.getHost(), uri.getPort());
                    }
                    return Optional.of(description);
                },
                configBuilder -> configBuilder
                        .setApplicationName(LocalizedText.english("milo opc-ua client"))
                        .setApplicationUri("urn:iboot-" + config.getEndpoint() + "-:milo:client")
                        .setKeyPair(loader.getClientKeyPair())
                        .setCertificate(loader.getClientCertificate())
                        .setCertificateChain(loader.getClientCertificateChain())
                        .setCertificateValidator(loader.getCertificateValidator())
                        .setIdentityProvider(identityProvider(config))
                        .setRequestTimeout(Unsigned.uint(5000))
                        .build()
        );
    }

    private void verifyConfig(MiloProperties.Config config) {
        if (Objects.isNull(config)) {
            throw new RuntimeException("the config cannot be null");
        }
        if (Objects.isNull(config.getSecurityPolicy())) {
            throw new RuntimeException("the config's security policy cannot be null");
        }
        if (SecurityPolicy.None.equals(config.getSecurityPolicy())) {
            // don't need to verify other properties
            return;
        }
        if (!StringUtils.hasText(config.getUsername())) {
            throw new RuntimeException("the config's username cannot be null or empty string, because the config's security policy is not None");
        }
        if (!StringUtils.hasText(config.getPassword())) {
            throw new RuntimeException("the config's password cannot be null or empty string, because the config's security policy is not None");
        }
    }

    private URI parseURI(String endpoint) {
        try {
            return new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new MiloPropertiesNullPointerException("the milo config's endpoint URI syntax error");
        }
    }

    private IdentityProvider identityProvider(MiloProperties.Config config) {
        if (SecurityPolicy.None.equals(config.getSecurityPolicy())) {
            return new AnonymousProvider();
        }
        if (StringUtils.hasText(config.getUsername()) && StringUtils.hasText(config.getPassword())) {
            return new UsernameProvider(config.getUsername(), config.getPassword());
        }
        throw new IdentityProviderException("the milo config's username or password is required:" + config);
    }
}
