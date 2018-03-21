package com.rbkmoney.proxy.mocketbank.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.proxy.mocketbank.utils.model.Error;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class ErrorMappingConfiguration {

    @Value("${error-mapping.file}")
    private org.springframework.core.io.Resource fileWithErrors;

    @Bean
    public List<Error> getListErrors() throws IOException {
        InputStream inputStream = fileWithErrors.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, new TypeReference<List<Error>>() {});
    }

}
