package hiperium.city.read.function.services;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.functions.common.exceptions.ResourceNotFoundException;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.mappers.FunctionMapper;
import hiperium.city.read.function.repositories.CityRepository;
import hiperium.city.read.function.requests.CityDataRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.Map;

/**
 * Service for managing city data, including retrieval and mapping operations.
 * This service interacts with a repository for data persistence and a mapper for data transformation.
 */
@Service
public class CityService {

    private final FunctionMapper functionMapper;
    private final CityRepository cityRepository;

    public CityService(FunctionMapper functionMapper, CityRepository cityRepository) {
        this.functionMapper = functionMapper;
        this.cityRepository = cityRepository;
    }

    /**
     * Retrieves an active city based on the provided city ID. This method queries the city repository
     * and applies a mapping function to convert the response into a CityEntity.
     *
     * @param cityDataRequest   the ID of the city to be retrieved
     * @param requestId         the unique identifier of the cityDataRequest for tracking purposes.
     * @return a {@link Mono} emitting a {@link CityEntity} representing the active city,
     *         or completing with an error if the city cannot be found or mapped
     */
    public Mono<CityEntity> findActiveCityById(final CityDataRequest cityDataRequest,
                                               final String requestId) {
        return this.cityRepository.findByCityId(cityDataRequest.cityId(), requestId)
            .flatMap(queryResponse ->
                retrieveCityData(cityDataRequest.cityId(), queryResponse, requestId))
            .map(this.functionMapper::mapCityDataResponse)
            .flatMap(cityEntity ->
                validateCityStatus(cityDataRequest.cityId(), cityEntity, requestId));
    }

    private static Mono<Map<String, AttributeValue>> retrieveCityData(final String cityId,
                                                                      final QueryResponse queryResponse,
                                                                      final String requestId) {
        if (queryResponse.items().isEmpty()) {
            return Mono.error(new ResourceNotFoundException("City not found with ID: " + cityId, requestId));
        } else {
            return Mono.just(queryResponse.items().getFirst());
        }
    }

    private static Mono<CityEntity> validateCityStatus(final String cityId,
                                                       final CityEntity cityEntity,
                                                       final String requestId) {
        if (RecordStatus.INACTIVE.equals(cityEntity.entityCommon().status())) {
            return Mono.error(
                new InactiveCityException("Cannot perform operations on an Inactive city: " + cityId, requestId));
        } else {
            return Mono.just(cityEntity);
        }
    }
}
