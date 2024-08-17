package hiperium.city.read.function.functions;

import hiperium.city.read.function.dto.CityDataResponse;
import hiperium.city.read.function.entities.City;
import hiperium.city.read.function.mappers.CityMapper;
import hiperium.city.read.function.services.CitiesService;
import hiperium.city.read.function.utils.FunctionUtils;
import hiperium.city.read.function.validations.BeanValidations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Represents a function that finds a city by its identifier.
 */
public class CityReadFunction implements Function<Message<byte[]>, Mono<CityDataResponse>> {

    private final CityMapper cityMapper;
    private final CitiesService citiesService;

    /**
     * Represents a function that finds a city by its identifier.
     * This function is used to retrieve city information based on the provided city ID request message.
     */
    public CityReadFunction(CityMapper cityMapper, CitiesService citiesService) {
        this.cityMapper = cityMapper;
        this.citiesService = citiesService;
    }

    /**
     * Applies the CityReadFunction to a given city ID request message and returns the CityDataResponse.
     *
     * @param requestMessage The city ID request message to be processed.
     * @return A Mono containing the CityDataResponse.
     */
    @Override
    public Mono<CityDataResponse> apply(Message<byte[]> requestMessage) {
        return Mono.fromCallable(() -> FunctionUtils.deserializeRequest(requestMessage))
            .doOnNext(BeanValidations::validateRequest)
            .flatMap(this.citiesService::findById)
            .flatMap(this::mapResponse)
            .onErrorResume(FunctionUtils::handleRuntimeException);
    }

    private Mono<CityDataResponse> mapResponse(City city) {
        return Mono.fromSupplier(() -> this.cityMapper.mapToCityResponse(city));
    }
}
