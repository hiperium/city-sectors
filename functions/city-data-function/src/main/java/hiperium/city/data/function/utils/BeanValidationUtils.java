package hiperium.city.data.function.utils;

import hiperium.city.data.function.dto.CityIdRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Utility class for performing bean validations on objects.
 */
public final class BeanValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanValidationUtils.class);

    private BeanValidationUtils() {
    }

    public static Mono<Void> validateBean(Mono<CityIdRequest> cityIdRequestMono) {
        LOGGER.debug("Validating Request - START");
        return cityIdRequestMono.flatMap(cityIdRequest -> {
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                Validator validator = factory.getValidator();
                Set<ConstraintViolation<CityIdRequest>> violations = validator.validate(cityIdRequest);
                if (!violations.isEmpty()) {
                    ConstraintViolation<CityIdRequest> firstViolation = violations.iterator().next();
                    return Mono.error(new ValidationException(firstViolation.getMessage()));
                }
            }
            return Mono.empty();
        });
    }
}
