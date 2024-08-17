package hiperium.city.read.function.validations;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.CityDataRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanValidations {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(BeanValidations.class);

    /**
     * Validates a CityDataRequest object using bean validation.
     *
     * @param cityDataRequest The CityDataRequest object to be validated.
     */
    public static void validateRequest(CityDataRequest cityDataRequest) {
        LOGGER.debug("Validating request message", cityDataRequest);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CityDataRequest>> violations = validator.validate(cityDataRequest);
            if (!violations.isEmpty()) {
                ConstraintViolation<CityDataRequest> firstViolation = violations.iterator().next();
                throw new ValidationException(firstViolation.getMessage());
            }
        }
    }
}
