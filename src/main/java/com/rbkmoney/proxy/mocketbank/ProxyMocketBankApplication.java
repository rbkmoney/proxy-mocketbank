package com.rbkmoney.proxy.mocketbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.proxy.mocketbank"})
public class ProxyMocketBankApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ProxyMocketBankApplication.class, args);
    }
}
