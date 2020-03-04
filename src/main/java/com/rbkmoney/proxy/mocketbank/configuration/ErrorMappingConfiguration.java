package com.rbkmoney.proxy.mocketbank.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.error.mapping.ErrorMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ErrorMappingConfiguration {

    @Value("${error-mapping.file}")
    private Resource filePath;

    @Value("${error-mapping.patternReason:\"'%s' - '%s'\"}")
    private String patternReason;

    @Bean
    ErrorMapping errorMapping() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        ErrorMapping errorMapping = new ErrorMapping(filePath.getInputStream(), patternReason, mapper);
        errorMapping.validateMapping();
        return errorMapping;
    }

}
