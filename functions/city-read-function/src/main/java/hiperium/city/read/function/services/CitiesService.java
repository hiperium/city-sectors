package hiperium.city.read.function.services;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.ReadCityRequest;
import hiperium.city.read.function.entities.City;
import hiperium.city.read.function.mappers.CityMapper;
import hiperium.city.read.function.repository.CitiesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The CitiesService class represents a service that interacts with the CityMapper and CitiesRepository
 * to retrieve City objects.
 */
@Service
public class CitiesService {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(CitiesService.class);

    private final CityMapper cityMapper;
    private final CitiesRepository citiesRepository;

    /**
     * This class represents a service that interacts with the CityMapper and CitiesRepository
     * to retrieve City objects.
     */
    public CitiesService(CityMapper cityMapper, CitiesRepository citiesRepository) {
        this.cityMapper = cityMapper;
        this.citiesRepository = citiesRepository;
    }

    /**
     * Retrieves the City object based on the provided ReadCityRequest.
     *
     * @param readCityRequest The ReadCityRequest object containing the unique identifier of the city.
     * @return A Mono that emits the City object if it is found and meets certain conditions.
     *         Otherwise, an error is emitted: ResourceNotFoundException if the city is not found in the repository,
     *         or DisabledCityException if the city is disabled.
     */
    public Mono<City> findById(final ReadCityRequest readCityRequest) {
        return Mono.fromCompletionStage(() -> this.citiesRepository.findByIdAsync(readCityRequest))
            .handle((returnedItem, sink) -> {
                if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                    LOGGER.error("No city found with the provided ID.", readCityRequest);
                    sink.error(new ResourceNotFoundException("No city found with the provided ID."));
                    return;
                }
                sink.next(this.cityMapper.mapToCity(returnedItem));
            });
    }
}
