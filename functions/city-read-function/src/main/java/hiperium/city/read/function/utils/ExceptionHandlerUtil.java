package hiperium.city.read.function.utils;

import hiperium.cities.common.exceptions.CityException;
import hiperium.cities.common.exceptions.InactiveCityException;
import hiperium.cities.common.exceptions.ParsingException;
import hiperium.cities.common.exceptions.ResourceNotFoundException;
import hiperium.cities.common.loggers.HiperiumLogger;
import hiperium.cities.common.responses.FunctionResponse;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 * Utility class for handling exceptions in a consistent way across the application.
 * This class provides methods to map exceptions to appropriate response messages and HTTP status codes.
 * It includes predefined handlers for specific custom exceptions and a default handler
 * for unexpected errors, ensuring structured and meaningful error messages are returned.
 * <p>
 * The class is designed to be used within reactive streams and does not allow instantiation.
 */
public final class ExceptionHandlerUtil {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    private ExceptionHandlerUtil() {
        throw new UnsupportedOperationException("Utility classes should not be instantiated.");
    }

    /**
     * Handles exceptions by mapping known exception types to appropriate response messages
     * and status codes. For unrecognized exceptions, it returns a generic internal server error response.
     *
     * @param throwable the exception to be handled
     * @return a {@link Mono} emitting a {@link FunctionResponse} with the error details and corresponding HTTP status code
     */
    public static Mono<FunctionResponse> handleException(Throwable throwable) {
        LOGGER.debug("Handling exception: {}", throwable.getClass().getSimpleName());
        return switch (throwable) {
            case ParsingException exception ->          handleException(exception);
            case ValidationException exception ->       handleException(exception);
            case InactiveCityException exception ->     handleException(exception);
            case ResourceNotFoundException exception -> handleException(exception);
            case CityException exception ->             handleException(exception);
            default -> Mono.just(FunctionResponse
                .error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error occurred"));
        };
    }

    private static Mono<FunctionResponse> handleException(ParsingException exception) {
        LOGGER.error(exception.getMessage(), exception);
        // TODO: update the error method to accept a map instead of a string
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            exception.getMessage()
        ));
    }

    private static Mono<FunctionResponse> handleException(ValidationException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            exception.getMessage()
        ));
    }

    private static Mono<FunctionResponse> handleException(ResourceNotFoundException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            exception.getMessage()
        ));
    }

    private static Mono<FunctionResponse> handleException(InactiveCityException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            exception.getMessage()
        ));
    }

    private static Mono<FunctionResponse> handleException(CityException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            exception.getMessage()
        ));
    }
}
