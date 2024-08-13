package hiperium.city.data.function.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.data.function.dto.CityDataRequest;
import hiperium.city.data.function.dto.CityDataResponse;
import hiperium.city.data.function.entities.City;
import hiperium.city.data.function.entities.CityStatus;
import hiperium.city.data.function.exceptions.DisabledCityException;
import hiperium.city.data.function.exceptions.ResourceNotFoundException;
import hiperium.city.data.function.mappers.CityMapper;
import hiperium.city.data.function.repository.CitiesRepository;
import hiperium.city.data.function.validations.BeanValidations;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

/**
 * Represents a function that finds a city by its identifier.
 */
public class CityDataFunction implements Function<Message<byte[]>, Mono<CityDataResponse>> {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(CityDataFunction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CityMapper cityMapper;
    private final CitiesRepository citiesRepository;

    /**
     * Represents a function that finds a city by its identifier.
     */
    public CityDataFunction(CityMapper cityMapper, CitiesRepository citiesRepository) {
        this.cityMapper = cityMapper;
        this.citiesRepository = citiesRepository;
    }

    /**
     * Find a city by its identifier.
     *
     * @param cityIdRequestMessage The request message containing the city ID.
     * @return A Mono containing the CityDataResponse object.
     */
    @Override
    public Mono<CityDataResponse> apply(Message<byte[]> cityIdRequestMessage) {
        CityDataRequest cityDataRequest;
        try {
            cityDataRequest = OBJECT_MAPPER.readValue(cityIdRequestMessage.getPayload(), CityDataRequest.class);
        } catch (IOException exception) {
            LOGGER.error("Couldn't deserialize payload request", exception.getMessage());
            return Mono.just(new CityDataResponse.Builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .errorMessage("Invalid request payload")
                .build());
        }
        return Mono.just(cityDataRequest)
            .doOnNext(BeanValidations::validateBean)
            .map(this.citiesRepository::findCityById)
            .doOnNext(this::validateCityStatus)
            .map(city -> this.cityMapper.toCityResponse(city, HttpStatus.OK.value(), null))
            .onErrorResume(CityDataFunction::handleException);
    }

    private void validateCityStatus(City city) {
        LOGGER.debug("Validating City Status", city);
        if (city.status().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + city.id());
        }
    }

    private static Mono<CityDataResponse> handleException(Throwable throwable) {
        LOGGER.error("Couldn't find city data", throwable.getMessage());
        CityDataResponse deviceUpdateResponse = createDeviceUpdateResponse(throwable);
        return Mono.just(deviceUpdateResponse);
    }

    private static CityDataResponse createDeviceUpdateResponse(Throwable throwable) {
        int statusCode;
        String message = throwable.getMessage();

        switch (throwable) {
            case ValidationException ignored -> statusCode = HttpStatus.BAD_REQUEST.value();
            case ResourceNotFoundException ignored -> statusCode = HttpStatus.NOT_FOUND.value();
            case DisabledCityException ignored -> statusCode = HttpStatus.NOT_ACCEPTABLE.value();
            default -> statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return new CityDataResponse.Builder()
            .httpStatus(statusCode)
            .errorMessage(message)
            .build();
    }
}
