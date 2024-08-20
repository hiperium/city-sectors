package hiperium.city.read.function.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.utils.ExceptionHandlerUtil;
import hiperium.city.read.function.dto.CityDataRequest;
import hiperium.city.read.function.dto.CityDataResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Utility class for common function operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    /**
     * Deserializes a request message to a {@link CityDataRequest} object.
     *
     * @param requestMessage The request message to be deserialized.
     * @return The deserialized {@link CityDataRequest} object.
     * @throws ParsingException If the deserialization fails.
     */
    public static CityDataRequest deserializeRequest(Message<byte[]> requestMessage) {
        try {
            return OBJECT_MAPPER.readValue(requestMessage.getPayload(), CityDataRequest.class);
        } catch (IOException exception) {
            String messageContent = new String(requestMessage.getPayload(), StandardCharsets.UTF_8);
            LOGGER.error("Couldn't deserialize request message.", exception.getMessage(), messageContent);
            throw new ParsingException("Couldn't deserialize request message.");
        }
    }

    /**
     * Validates a CityDataRequest object using bean validation.
     *
     * @param dataRequest The CityDataRequest object to be validated.
     * @throws ValidationException if the DeviceDataRequest object is invalid.
     */
    public static void validateRequest(CityDataRequest dataRequest) {
        LOGGER.debug("Validating request message", dataRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CityDataRequest>> violations = validator.validate(dataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<CityDataRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }

    /**
     * Handles a runtime exception by generating an error response and encapsulating it in a CityDataResponse object.
     *
     * @param throwable The runtime exception to handle.
     * @return A Mono that emits a CityDataResponse object containing the generated error response.
     */
    public static Mono<CityDataResponse> handleRuntimeException(Throwable throwable) {
        return Mono.just(throwable)
            .map(ExceptionHandlerUtil::generateErrorResponse)
            .map(errorResponse -> new CityDataResponse(null, null, null, errorResponse))
            .doOnNext(deviceUpdateResponse -> LOGGER.debug("Mapped response", deviceUpdateResponse));
    }
}
