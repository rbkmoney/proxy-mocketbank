package com.rbkmoney.proxy.mocketbank.configuration;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the configuration class for configuring the {@link EmbeddedServletContainerFactory}
 *
 * @author Anatoly Cherkasov
 * @see Connector
 * @see EmbeddedServletContainerFactory
 */
@Configuration
public class TomcatEmbeddedConfiguration {

    @Value("${server.port}")
    private String mainPort;

    @Value("#{'${server.secondary.ports}'.split(',')}")
    private List<String> secondaryPorts;

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();

        Connector[] additionalConnectors = this.additionalConnector();
        if (additionalConnectors.length > 0) {
            tomcat.addAdditionalTomcatConnectors(additionalConnectors);
        }
        return tomcat;
    }

    private Connector[] additionalConnector() {
        List<Connector> result = new ArrayList<>();
        for (String port : secondaryPorts) {
            Connector connector = new Connector();
            connector.setPort(Integer.valueOf(port));
            result.add(connector);
        }
        return result.toArray(new Connector[]{});
    }

}
