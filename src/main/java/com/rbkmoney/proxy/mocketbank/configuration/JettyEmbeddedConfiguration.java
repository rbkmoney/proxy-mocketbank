package com.rbkmoney.proxy.mocketbank.configuration;

import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JettyEmbeddedConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(JettyEmbeddedConfiguration.class);

    @Value("${server.port}")
    private String mainPort;

    @Value("#{'${server.secondary.ports}'.split(',')}")
    private List<String> secondaryPorts;

    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory() {

        final JettyEmbeddedServletContainerFactory factory =  new JettyEmbeddedServletContainerFactory(Integer.valueOf(mainPort));

        // Add customized Jetty configuration with non blocking connection handler
        factory.addServerCustomizers((JettyServerCustomizer) server -> {
            // Register an additional connector for each secondary port.
            for(final String secondaryPort : secondaryPorts) {
                final NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
                connector.setPort(Integer.valueOf(secondaryPort));
                try {
                    connector.join();
                } catch (InterruptedException e) {
                    LOGGER.error("Exception in JettyConfiguration", e);
                }
                server.addConnector(connector);
            }

            // Additional configuration
        });
        return factory;
    }

}
