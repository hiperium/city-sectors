package hiperium.city.read.function.functions;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.responses.FunctionResponse;
import hiperium.city.read.function.services.CityService;
import hiperium.city.read.function.utils.ExceptionHandlerUtil;
import hiperium.city.read.function.utils.UnmarshallUtils;
import hiperium.city.read.function.utils.ValidationUtils;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * The FindCityFunction class implements the Function interface, allowing it to process a message
 * to find an active city by ID using the CityService. It handles deserialization and validation
 * of the request, invokes a service call to retrieve the city data, and returns a response wrapped
 * in a reactive {@link Mono} stream.
 */
@Component(FindCityFunction.FUNCTION_NAME)
public class FindCityFunction implements Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> {

    public static final String FUNCTION_NAME = "findCityById";
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FindCityFunction.class);

    private final CityService cityService;

    public FindCityFunction(CityService cityService) {
        this.cityService = cityService;
    }

    @Override
    public Mono<FunctionResponse> apply(Message<APIGatewayProxyRequestEvent> requestMessage) {
        LOGGER.debug("Processing request to find city by ID: {}", requestMessage.getPayload());

        return Mono.just(UnmarshallUtils.deserializeRequest(requestMessage.getPayload()))
            .doOnNext(cityDataRequest ->
                ValidationUtils.validateRequest(
                    cityDataRequest,
                    requestMessage.getPayload().getRequestContext().getRequestId()))
            .flatMap(cityDataRequest ->
                this.cityService.findActiveCityById(
                    cityDataRequest,
                    requestMessage.getPayload().getRequestContext().getRequestId()))
            .map(FunctionResponse::success)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }
}
