package hiperium.city.read.function.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.commons.exceptions.DisabledCityException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.CityDataRequest;
import hiperium.city.read.function.dto.CityDataResponse;
import hiperium.city.read.function.entities.City;
import hiperium.city.read.function.entities.CityStatus;
import hiperium.city.read.function.mappers.CityMapper;
import hiperium.city.read.function.repository.CitiesRepository;
import hiperium.city.read.function.validations.BeanValidations;
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
     * Applies the CityDataFunction to a given city ID request message and returns the CityDataResponse.
     *
     * @param cityIdRequestMessage The city ID request message to be processed.
     * @return A Mono containing the CityDataResponse.
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
            .map(this.citiesRepository::findById)
            .doOnNext(this::validateCityStatus)
            .map(city -> this.cityMapper.mapToCityResponse(city, HttpStatus.OK.value(), null))
            .onErrorResume(CityDataFunction::handleException);
    }

    private void validateCityStatus(City city) {
        LOGGER.debug("Validating City Status", city);
        if (city.status().equals(CityStatus.DISABLED)) {
            throw new DisabledCityException("City is disabled: " + city.id());
        }
    }

    private static Mono<CityDataResponse> handleException(Throwable throwable) {
        LOGGER.error("Couldn't find City data", throwable.getMessage());
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
