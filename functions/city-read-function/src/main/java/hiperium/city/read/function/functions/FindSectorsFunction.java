package hiperium.city.read.function.functions;

import hiperium.cities.common.loggers.HiperiumLogger;
import hiperium.cities.common.responses.FunctionResponse;
import hiperium.city.read.function.entities.SectorEntity;
import hiperium.city.read.function.utils.ExceptionHandlerUtil;
import hiperium.city.read.function.requests.FunctionRequest;
import hiperium.city.read.function.services.CityService;
import hiperium.city.read.function.utils.FunctionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * The FindSectorsFunction class is responsible for executing a function that locates
 * active sectors within a specified city by using a city ID. This class implements
 * the Function interface, which allows it to process messages containing city ID data
 * and produce a response encapsulating the result.
 */
public class FindSectorsFunction implements Function<Message<FunctionRequest>, Mono<FunctionResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FindSectorsFunction.class);

    private final CityService cityService;

    /**
     * Constructs a FindSectorsFunction instance with the given CityService.
     *
     * @param cityService the CityService used for retrieving sector data related to specific cities
     */
    public FindSectorsFunction(CityService cityService) {
        this.cityService = cityService;
    }

    /**
     * Applies the function to find active sectors by city ID from a given request message.
     *
     * @param requestMessage the message containing city ID information in a byte array format
     * @return a {@link Mono} emitting a {@link FunctionResponse} containing the list of active sectors associated
     *         with the specified city and an HTTP status code, or an error if the process fails
     */
    @Override
    public Mono<FunctionResponse> apply(Message<FunctionRequest> requestMessage) {
        LOGGER.debug("Finding active sectors by city ID: {}", requestMessage.getPayload().cityId());
        return FunctionUtils.validateRequest(requestMessage.getPayload())
            .then(this.cityService.findActiveSectorsByCityId(requestMessage.getPayload()))
            .map(this::generateResponse)
            .onErrorResume(ExceptionHandlerUtil::handleException);
    }

    private FunctionResponse generateResponse(List<SectorEntity> sectorEntity) {
        return new FunctionResponse.Builder()
            .withStatusCode(HttpStatus.OK.value())
            .withBody(sectorEntity)
            .build();
    }
}
