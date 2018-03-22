package com.rbkmoney.proxy.mocketbank.utils.error_mapping;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.proxy.mocketbank.utils.model.Error;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.rbkmoney.geck.serializer.kit.tbase.TErrorUtil.toGeneral;


/**
 * @author Anatoly Cherkasov
 */
public class ErrorMapping {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String DEFAULT_FILE_PATH = "/opt/java/proxy-test/errors.json";

    private static final String DEFAULT_PATTERN_REASON = "'%s' - '%s'";

    private final ObjectMapper mapper;

    /**
     * Pattern for reason failure
     */
    private final String patternReason;

    private final String filePath;

    /**
     * List of errors
     */
    private final List<com.rbkmoney.proxy.mocketbank.utils.model.Error> errors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link ErrorMapping} instance.
     */
    public ErrorMapping() {
        this(DEFAULT_FILE_PATH);
    }

    public ErrorMapping(String filePath) {
        this(filePath, DEFAULT_PATTERN_REASON);
    }

    public ErrorMapping(String filePath, String patternReason) {
        this(filePath, patternReason, new ObjectMapper());
    }

    public ErrorMapping(String filePath, String patternReason, ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.patternReason = patternReason;
        this.mapper = objectMapper;
        this.errors = initErrorList();
    }

    private List<com.rbkmoney.proxy.mocketbank.utils.model.Error> initErrorList() {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            return mapper.readValue(inputStream, new TypeReference<List<Error>>() {});
        } catch (JsonParseException e) {
            throw new ErrorMappingException("Json can't parse data from file", e);
        } catch (JsonMappingException e) {
            throw new ErrorMappingException("Json can't mapping data from file", e);
        } catch (IOException e) {
            throw new ErrorMappingException("Failed to initErrorList", e);
        }
    }


    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /**
     * Get failure by code and description
     *
     * @param code        String
     * @param description String
     * @return Failure
     */
    public Failure getFailureByCodeAndDescription(String code, String description) {
        com.rbkmoney.proxy.mocketbank.utils.model.Error error = findMatchWithPattern(errors, code, description);

        Failure failure = toGeneral(error.getMapping());
        failure.setReason(prepareReason(code, description));
        return failure;
    }

    /**
     * Find match code or description by pattern
     *
     * @param errors      List<Error>
     * @param code        String
     * @param description String
     * @return com.rbkmoney.proxy.mocketbank.utils.model.Error
     */
    private com.rbkmoney.proxy.mocketbank.utils.model.Error findMatchWithPattern(
            List<Error> errors,
            String code,
            String description
    ) {
        if (code == null || description == null) {
            throw new IllegalArgumentException();
        }

        return errors.stream()
                .filter(error ->
                        (code.matches(error.getRegexp())
                                || description.matches(error.getRegexp())
                        )
                )
                .findFirst()
                .orElseThrow(() -> new WUndefinedResultException(String.format("Undefined error. code %s, description %s", code, description)));
    }

    // ------------------------------------------------------------------------
    // Private methods
    // ------------------------------------------------------------------------

    /**
     * Prepare reason for {@link Failure}
     *
     * @param code        String
     * @param description String
     * @return String
     */
    private String prepareReason(String code, String description) {
        return String.format(this.patternReason, code, description);
    }

}
