package hiperium.city.read.function.functions;

import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.ResponseUtil;
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
public class FindCityFunction implements Function<Message<FunctionRequest>, Mono<Message<String>>> {

    public static final String FUNCTION_NAME = "findCityById";

    private final CityService cityService;

    public FindCityFunction(CityService cityService) {
        this.cityService = cityService;
    }

    @Override
    public Mono<Message<String>> apply(Message<FunctionRequest> requestMessage) {
        return Mono.just(UnmarshallUtils.deserializeRequest(requestMessage.getPayload()))
            .doOnNext(ValidationUtils::validateRequest)
            .flatMap(this.cityService::findActiveCityById)
            .map(ResponseUtil::success)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }
}
