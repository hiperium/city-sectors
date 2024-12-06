package hiperium.city.read.function.utils;

import hiperium.cities.common.loggers.HiperiumLogger;
import hiperium.city.read.function.requests.FunctionRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Utility class for function operations.
 */
public final class FunctionUtils {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    private FunctionUtils() {
        throw new UnsupportedOperationException("Utility classes should not be instantiated.");
    }

    /**
     * Validates a FunctionRequest object using bean validation.
     *
     * @param dataRequest The FunctionRequest object to be validated.
     * @throws ValidationException if the DeviceDataRequest object is common.
     */
    public static Mono<Void> validateRequest(final FunctionRequest dataRequest) {
        return Mono.fromRunnable(() -> {
            LOGGER.debug("Validating request: {}", dataRequest);
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                Validator validator = factory.getValidator();
                Set<ConstraintViolation<FunctionRequest>> violations = validator.validate(dataRequest);
                if (!violations.isEmpty()) {
                    ConstraintViolation<FunctionRequest> firstViolation = violations.iterator().next();
                    throw new ValidationException(firstViolation.getMessage());
                }
            }
        });
    }
}
