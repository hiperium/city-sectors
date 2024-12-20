package hiperium.city.read.function.functions;

import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.ResponseUtil;
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
public class FindSectorsFunction implements Function<Message<FunctionRequest>, Mono<Message<String>>> {

    public static final String FUNCTION_NAME = "findSectorsByCityId";

    private final SectorService sectorService;

    public FindSectorsFunction(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @Override
    public Mono<Message<String>> apply(Message<FunctionRequest> requestMessage) {
        return Mono.just(UnmarshallUtils.deserializeRequest(requestMessage.getPayload()))
            .doOnNext(ValidationUtils::validateRequest)
            .flatMap(this.sectorService::findActiveSectorsByCityId)
            .map(ResponseUtil::success)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }
}
