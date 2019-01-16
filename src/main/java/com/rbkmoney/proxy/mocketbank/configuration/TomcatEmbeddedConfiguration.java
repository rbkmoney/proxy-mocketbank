package com.rbkmoney.proxy.mocketbank.configuration;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the configuration class for configuring the {@link ServletWebServerFactory}
 *
 * @author Anatoly Cherkasov
 * @see Connector
 * @see ServletWebServerFactory
 */
@Configuration
public class TomcatEmbeddedConfiguration {

    @Value("${server.port}")
    private String mainPort;

    @Value("#{'${server.secondary.ports}'.split(',')}")
    private List<String> secondaryPorts;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

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
