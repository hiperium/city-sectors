package hiperium.city.data.function.utils;

import hiperium.city.data.function.dto.CityIdRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
public final class BeanValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanValidationUtils.class);

    private BeanValidationUtils() {
    }

    /**
     * Validates a CityIdRequest object using bean validation.
     *
     * @param cityIdRequest The CityIdRequest object to be validated.
     */
    public static void validateBean(CityIdRequest cityIdRequest) {
        LOGGER.debug("Validating request: {}", cityIdRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CityIdRequest>> violations = validator.validate(cityIdRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<CityIdRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
