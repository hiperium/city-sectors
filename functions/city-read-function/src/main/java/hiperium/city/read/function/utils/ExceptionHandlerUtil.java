package hiperium.city.read.function.utils;

import hiperium.city.functions.common.enums.ErrorCode;
import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.functions.common.exceptions.ResourceNotFoundException;
import hiperium.city.functions.common.exceptions.ValidationException;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.responses.FunctionResponse;
import reactor.core.publisher.Mono;

/**
 * Utility class for handling exceptions in a consistent way across the application.
 * This class provides methods to map exceptions to appropriate response messages and HTTP status codes.
 * It includes predefined handlers for specific custom exceptions and a default handler
 * for unexpected errors, ensuring structured and meaningful error messages are returned.
 * <p>
 * The class is designed to be used within reactive streams and does not allow instantiation.
 */
//TODO: move this class to the common module
public final class ExceptionHandlerUtil {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(ExceptionHandlerUtil.class);

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
            case ValidationException exception ->       handleException(exception);
            case InactiveCityException exception ->     handleException(exception);
            case ResourceNotFoundException exception -> handleException(exception);
            case CityException exception ->             handleException(exception);
            default -> Mono.just(FunctionResponse
                .error(new CityException(
                    "An unexpected error occurred.",
                    ErrorCode.INTERNAL_001,
                    throwable)));
        };
    }

    private static Mono<FunctionResponse> handleException(ValidationException validationException) {
        LOGGER.error(validationException.getMessage(), validationException);
        return Mono.just(FunctionResponse.error(validationException));
    }

    private static Mono<FunctionResponse> handleException(ResourceNotFoundException resourceNotFoundException) {
        LOGGER.error(resourceNotFoundException.getMessage(), resourceNotFoundException);
        return Mono.just(FunctionResponse.error(resourceNotFoundException));
    }

    private static Mono<FunctionResponse> handleException(InactiveCityException inactiveCityException) {
        LOGGER.error(inactiveCityException.getMessage(), inactiveCityException);
        return Mono.just(FunctionResponse.error(inactiveCityException));
    }

    private static Mono<FunctionResponse> handleException(CityException cityException) {
        LOGGER.error(cityException.getMessage(), cityException);
        return Mono.just(FunctionResponse.error(cityException));
    }
}
