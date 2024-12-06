package hiperium.city.read.function.functions;

import hiperium.cities.common.loggers.HiperiumLogger;
import hiperium.cities.common.responses.FunctionResponse;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.utils.ExceptionHandlerUtil;
import hiperium.city.read.function.requests.FunctionRequest;
import hiperium.city.read.function.services.CityService;
import hiperium.city.read.function.utils.FunctionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * The FindCityFunction class implements the Function interface, allowing it to process a message
 * to find an active city by ID using the CityService. It handles deserialization and validation
 * of the request, invokes a service call to retrieve the city data, and returns a response wrapped
 * in a reactive {@link Mono} stream.
 */
public class FindCityFunction implements Function<Message<FunctionRequest>, Mono<FunctionResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FindCityFunction.class);

    private final CityService cityService;

    /**
     * Creates an instance of FindCityFunction using the specified CityService.
     *
     * @param cityService the service responsible for city-related operations, particularly retrieving
     *                    city data required by the function
     */
    public FindCityFunction(CityService cityService) {
        this.cityService = cityService;
    }

    /**
     * Applies the function to find an active city by its ID from a given request message.
     *
     * @param requestMessage the message containing the city ID to be processed
     * @return a {@link Mono} emitting a {@link FunctionResponse} containing the city data and an HTTP status code,
     *         or completing with an error if the process fails
     */
    @Override
    public Mono<FunctionResponse> apply(Message<FunctionRequest> requestMessage) {
        LOGGER.debug("Finding city by ID: {}", requestMessage.getPayload().cityId());
        return FunctionUtils.validateRequest(requestMessage.getPayload())
            .then(this.cityService.findActiveCityById(requestMessage.getPayload()))
            .map(this::buildCityResponse)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }

    private FunctionResponse buildCityResponse(CityEntity cityEntity) {
        return new FunctionResponse.Builder()
            .withStatusCode(HttpStatus.OK.value())
            .withBody(cityEntity)
            .build();
    }
}
