package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

    @Value("${restTemplate.networkTimeout}")
    private int networkTimeout;

    @Bean
    public SSLContext sslContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return new SSLContextBuilder()
                .loadTrustMaterial((x509Certificates, s) -> true)
                .build();
    }

    @Bean
    public CloseableHttpClient httpClient(SSLContext sslContext) {
        return HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(sslContext)
                .disableAutomaticRetries()
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory requestFactory(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    @Bean
    // TODO: 14/11/2018 fix integrations test: change {@Autowired ServerHandler serverHandler} to thriftClient 
    //@RequestScope
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory requestFactory) {
        int executionTimeout = ContextUtils.getExecutionTimeout(TraceContext.getCurrentTraceData().getServiceSpan(), networkTimeout);
        return new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
                .setConnectTimeout(Duration.ofMillis(executionTimeout))
                .setReadTimeout(Duration.ofMillis(executionTimeout))
                .build();
    }

}
