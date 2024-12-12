package hiperium.city.read.function.utils;

import hiperium.city.functions.common.exceptions.ValidationException;
import hiperium.city.read.function.requests.CityDataRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

/**
 * Utility class for function operations.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility classes should not be instantiated.");
    }

    /**
     * Validates the given CityDataRequest object to ensure it meets all
     * specified constraints. If the request is invalid, a ValidationException
     * is thrown with details about the first encountered violation.
     *
     * @param dataRequest the CityDataRequest object to be validated
     * @param requestId the unique identifier for the current request, used for logging or error handling
     * @throws ValidationException if the dataRequest fails validation
     */
    public static void validateRequest(final CityDataRequest dataRequest, final String requestId) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CityDataRequest>> violations = validator.validate(dataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<CityDataRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage(), requestId);
            }
        }
    }
}
