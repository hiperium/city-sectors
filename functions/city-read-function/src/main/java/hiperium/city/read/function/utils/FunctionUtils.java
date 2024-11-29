package hiperium.city.read.function.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.requests.FunctionRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Utility class for function operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    /**
     * Deserializes a request message to a {@link FunctionRequest} object.
     *
     * @param requestMessage The request message to be deserialized.
     * @return The deserialized {@link FunctionRequest} object.
     * @throws ParsingException If the deserialization fails.
     */
    public static FunctionRequest deserializeRequest(final Message<byte[]> requestMessage) {
        LOGGER.debug("Deserializing request: {}", requestMessage);
        try {
            return OBJECT_MAPPER.readValue(requestMessage.getPayload(), FunctionRequest.class);
        } catch (IOException exception) {
            String messageContent = new String(requestMessage.getPayload(), StandardCharsets.UTF_8);
            throw new ParsingException("Couldn't deserialize request: " + messageContent, exception);
        }
    }

    /**
     * Validates a FunctionRequest object using bean validation.
     *
     * @param dataRequest The FunctionRequest object to be validated.
     * @throws ValidationException if the DeviceDataRequest object is invalid.
     */
    public static void validateRequest(final FunctionRequest dataRequest) {
        LOGGER.debug("Validating request: {}", dataRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<FunctionRequest>> violations = validator.validate(dataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<FunctionRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
