package hiperium.city.read.function.utils;

import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.functions.common.exceptions.ResourceNotFoundException;
import hiperium.city.functions.common.exceptions.ValidationException;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.utils.ResponseUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;

/**
 * Utility class for handling exceptions in a consistent way across the application.
 * This class provides methods to map exceptions to appropriate response messages and HTTP status codes.
 * It includes predefined handlers for specific custom exceptions and a default handler
 * for unexpected errors, ensuring structured and meaningful error messages are returned.
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
     * @return a {@link Mono} emitting a {@link Message} with the error details and corresponding HTTP status code
     */
    public static Mono<Message<String>> handleException(Throwable throwable) {
        return switch (throwable) {
            case ValidationException exception -> handleException(exception);
            case InactiveCityException exception -> handleException(exception);
            case ResourceNotFoundException exception -> handleException(exception);
            case CityException exception -> handleException(exception);
            default -> Mono.just(
                MessageBuilder
                    .withPayload("Internal server error.")
                    .setHeader(ResponseUtil.LAMBDA_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN_VALUE)
                    .build()
            );
        };
    }

    private static Mono<Message<String>> handleException(ValidationException validationException) {
        LOGGER.error(validationException.getMessage(), validationException);
        return Mono.just(ResponseUtil.error(validationException));
    }

    private static Mono<Message<String>> handleException(ResourceNotFoundException resourceNotFoundException) {
        LOGGER.error(resourceNotFoundException.getMessage(), resourceNotFoundException);
        return Mono.just(ResponseUtil.error(resourceNotFoundException));
    }

    private static Mono<Message<String>> handleException(InactiveCityException inactiveCityException) {
        LOGGER.error(inactiveCityException.getMessage(), inactiveCityException);
        return Mono.just(ResponseUtil.error(inactiveCityException));
    }

    private static Mono<Message<String>> handleException(CityException cityException) {
        LOGGER.error(cityException.getMessage(), cityException);
        return Mono.just(ResponseUtil.error(cityException));
    }
}
