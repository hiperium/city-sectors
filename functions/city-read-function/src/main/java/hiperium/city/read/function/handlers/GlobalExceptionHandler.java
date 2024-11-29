package hiperium.city.read.function.handlers;

import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.exceptions.InactiveCityException;
import hiperium.cities.commons.exceptions.ParsingException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.responses.FunctionResponse;
import hiperium.city.read.function.utils.FunctionUtils;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionUtils.class);

    @ExceptionHandler(ParsingException.class)
    public Mono<FunctionResponse> handleParsingException(ParsingException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            // TODO: Add error code instead of message
            exception.getMessage()
        ));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<FunctionResponse> handleParsingException(ValidationException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            // TODO: Add error code instead of message
            exception.getMessage()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<FunctionResponse> handleParsingException(ResourceNotFoundException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            // TODO: Add error code instead of message
            exception.getMessage()
        ));
    }

    @ExceptionHandler(InactiveCityException.class)
    public Mono<FunctionResponse> handleParsingException(InactiveCityException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.NOT_ACCEPTABLE.value(),
            // TODO: Add error code instead of message
            exception.getMessage()
        ));
    }

    @ExceptionHandler(CityException.class)
    public Mono<FunctionResponse> handleParsingException(CityException exception) {
        LOGGER.error(exception.getMessage(), exception);
        return Mono.just(FunctionResponse.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            // TODO: Add error code instead of message
            exception.getMessage()
        ));
    }
}
