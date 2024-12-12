package hiperium.city.read.function.functions;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.responses.FunctionResponse;
import hiperium.city.read.function.services.SectorService;
import hiperium.city.read.function.utils.ExceptionHandlerUtil;
import hiperium.city.read.function.utils.UnmarshallUtils;
import hiperium.city.read.function.utils.ValidationUtils;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * The FindSectorsFunction class implements the Function interface, allowing it to process a message
 * to find active city sectors by ID using the CityService. It handles deserialization and validation
 * of the request, invokes a service call to retrieve the city sectors data, and returns a response wrapped
 * in a reactive {@link Mono} stream.
 */
@Component(FindSectorsFunction.FUNCTION_NAME)
public class FindSectorsFunction implements Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> {

    public static final String FUNCTION_NAME = "findSectorsByCityId";
    private static final HiperiumLogger LOGGER = new HiperiumLogger(FindSectorsFunction.class);

    private final SectorService sectorService;

    public FindSectorsFunction(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @Override
    public Mono<FunctionResponse> apply(Message<APIGatewayProxyRequestEvent> requestMessage) {
        LOGGER.debug("Processing request to find sectors by city ID: {}", requestMessage.getPayload());

        return Mono.just(UnmarshallUtils.deserializeRequest(requestMessage.getPayload()))
            .doOnNext(cityDataRequest ->
                ValidationUtils.validateRequest(
                    cityDataRequest,
                    requestMessage.getPayload().getRequestContext().getRequestId()))
            .flatMap(cityDataRequest ->
                this.sectorService.findActiveSectorsByCityId(
                    cityDataRequest,
                    requestMessage.getPayload().getRequestContext().getRequestId()))
            .map(FunctionResponse::success)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }
}
