package com.rbkmoney.proxy.mocketbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class ProxyMocketBankApplication extends SpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProxyMocketBankApplication.class, args);
    }
}
