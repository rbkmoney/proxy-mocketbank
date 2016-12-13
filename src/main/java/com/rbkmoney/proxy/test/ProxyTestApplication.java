package com.rbkmoney.proxy.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.proxy.test"})
public class ProxyTestApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ProxyTestApplication.class, args);
    }
}
