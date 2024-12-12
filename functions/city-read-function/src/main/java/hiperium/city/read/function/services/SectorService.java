package hiperium.city.read.function.services;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.read.function.entities.SectorEntity;
import hiperium.city.read.function.mappers.FunctionMapper;
import hiperium.city.read.function.repositories.SectorRepository;
import hiperium.city.read.function.requests.CityDataRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing sector data, including retrieval and mapping operations.
 * This service interacts with a repository for data persistence and a mapper for data transformation.
 */
@Service
public class SectorService {

    private final FunctionMapper functionMapper;
    private final CityService cityService;
    private final SectorRepository sectorRepository;

    public SectorService(FunctionMapper functionMapper, CityService cityService, SectorRepository sectorRepository) {
        this.functionMapper = functionMapper;
        this.cityService = cityService;
        this.sectorRepository = sectorRepository;
    }

    /**
     * Finds the list of active {@code SectorEntity} objects associated with a specified city ID.
     * This method ensures that only sectors belonging to an active city are processed.
     *
     * @param cityDataRequest   the unique identifier of the city for which active sectors are to be found.
     * @param requestId         the unique identifier of the cityDataRequest for tracking purposes.
     * @return a {@code Mono} containing a list of active {@code SectorEntity} instances if the city is active,
     *         or an error if the city record is inactive or the search fails.
     */
    public Mono<List<SectorEntity>> findActiveSectorsByCityId(final CityDataRequest cityDataRequest,
                                                              final String requestId) {
        return this.cityService.findActiveCityById(cityDataRequest, requestId)
            .flatMap(cityEntity -> {
                if (RecordStatus.ACTIVE.equals(cityEntity.entityCommon().status())) {
                    return this.sectorRepository.findSectorsByCityAndStatus(
                            cityDataRequest.cityId(),
                            RecordStatus.ACTIVE,
                            requestId)
                        .map(response -> response.items().stream()
                            .map(this.functionMapper::mapSectorsDataResponse)
                            .collect(Collectors.toList()));
                } else {
                    return Mono.error(
                        new InactiveCityException("Cannot perform operations on an Inactive city: " + cityDataRequest.cityId(),
                            requestId));
                }
            });
    }
}
