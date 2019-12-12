package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;
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
        ErrorMapping errorMapping = new ErrorMapping(filePath.getInputStream(), patternReason);
        errorMapping.validateMappingFormat();
        return errorMapping;
    }

}
