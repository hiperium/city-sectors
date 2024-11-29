package hiperium.city.read.function.services;

import hiperium.cities.commons.enums.RecordStatus;
import hiperium.cities.commons.exceptions.InactiveCityException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.entities.SectorEntity;
import hiperium.city.read.function.mappers.CityMapper;
import hiperium.city.read.function.repositories.CityRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing city and sector data, including retrieval and mapping operations.
 * This service interacts with a repository for data persistence and a mapper for data transformation.
 */
@Service
public class CityService {

    private final CityMapper cityMapper;
    private final CityRepository cityRepository;

    /**
     * Constructs a new CityService with the specified city mapper and city repository.
     *
     * @param cityMapper the CityMapper to be used for mapping city data responses
     * @param cityRepository the CityRepository to be used for accessing city data
     */
    public CityService(CityMapper cityMapper, CityRepository cityRepository) {
        this.cityMapper = cityMapper;
        this.cityRepository = cityRepository;
    }

    /**
     * Retrieves an active city based on the provided city ID. This method queries the city repository
     * and applies a mapping function to convert the response into a CityEntity.
     *
     * @param cityId the ID of the city to be retrieved
     * @return a {@link Mono} emitting a {@link CityEntity} representing the active city,
     *         or completing with an error if the city cannot be found or mapped
     */
    public Mono<CityEntity> findActiveCityById(String cityId) {
        return this.cityRepository.findByCityId(cityId)
            .flatMap(queryResponse ->
                retrieveCityData(cityId, queryResponse))
            .map(this.cityMapper::mapCityDataResponse)
            .flatMap(CityService::validateCityStatus);
    }

    /**
     * Finds the list of active {@code SectorEntity} objects associated with a specified city ID.
     * This method ensures that only sectors belonging to an active city are processed.
     *
     * @param cityId the unique identifier of the city for which active sectors are to be found.
     * @return a {@code Mono} containing a list of active {@code SectorEntity} instances if the city is active,
     *         or an error if the city record is inactive or the search fails.
     */
    public Mono<List<SectorEntity>> findActiveSectorsByCityId(final String cityId) {
        return this.findActiveCityById(cityId)
            .flatMap(cityEntity -> {
                if (RecordStatus.ACTIVE.equals(cityEntity.commonAttributes().status())) {
                    return this.cityRepository.findSectorsByCityAndStatus(cityId, RecordStatus.ACTIVE)
                        .map(response -> response.items().stream()
                            .map(this.cityMapper::mapSectorsDataResponse)
                            .collect(Collectors.toList()));
                } else {
                    return Mono.error(new InactiveCityException("Cannot perform operations on an Inactive city."));
                }
            });
    }

    private static Mono<Map<String, AttributeValue>> retrieveCityData(String cityId, QueryResponse queryResponse) {
        if (queryResponse.items().isEmpty()) {
            return Mono.error(new ResourceNotFoundException("City not found with ID: " + cityId));
        } else {
            return Mono.just(queryResponse.items().getFirst());
        }
    }

    private static Mono<CityEntity> validateCityStatus(CityEntity cityEntity) {
        if (RecordStatus.INACTIVE.equals(cityEntity.commonAttributes().status())) {
            return Mono.error(new InactiveCityException("The city record is inactive."));
        } else {
            return Mono.just(cityEntity);
        }
    }
}
