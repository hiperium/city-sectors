package hiperium.city.read.function.services;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.read.function.commons.TimeZoneProvider;
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

    private final CityService cityService;
    private final FunctionMapper functionMapper;
    private final SectorRepository sectorRepository;
    private final TimeZoneProvider timeZoneProvider;

    public SectorService(CityService cityService, FunctionMapper functionMapper,
                         SectorRepository sectorRepository, TimeZoneProvider timeZoneProvider) {
        this.cityService = cityService;
        this.functionMapper = functionMapper;
        this.sectorRepository = sectorRepository;
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Finds the list of active {@code SectorEntity} objects associated with a specified city ID.
     * This method ensures that only sectors belonging to an active city are processed.
     *
     * @param cityDataRequest   the unique identifier of the city for which active sectors are to be found.
     * @return a {@code Mono} containing a list of active {@code SectorEntity} instances if the city is active,
     *         or an error if the city record is inactive or the search fails.
     */
    public Mono<List<SectorEntity>> findActiveSectorsByCityId(final CityDataRequest cityDataRequest) {
        return this.cityService.findActiveCityById(cityDataRequest)
            .flatMap(cityEntity -> {
                if (RecordStatus.ACTIVE.equals(cityEntity.entityCommon().status())) {
                    return this.sectorRepository.findSectorsByCityAndStatus(
                            cityDataRequest.cityIdRequest().cityId(),
                            RecordStatus.ACTIVE,
                            cityDataRequest.requestId())
                        .map(response -> response.items().stream()
                            .map(item ->
                                this.functionMapper.mapSectorsDataResponse(item, this.timeZoneProvider))
                            .collect(Collectors.toList()));
                } else {
                    return Mono.error(
                        new InactiveCityException("Cannot perform operations on an Inactive city: " +
                            cityDataRequest.cityIdRequest().cityId(),
                            cityDataRequest.requestId()));
                }
            });
    }
}
