package hiperium.city.data.function.functions;

import hiperium.city.data.function.dto.CityIdRequest;
import hiperium.city.data.function.dto.CityResponse;
import hiperium.city.data.function.dto.RecordStatus;
import hiperium.city.data.function.entities.City;
import hiperium.city.data.function.mappers.CityMapper;
import hiperium.city.data.function.utils.BeanValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that finds a city by its identifier.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbClient.
 * The solution is to use the low-level client instead.
 */
public class CityDataFunction implements Function<Mono<CityIdRequest>, Mono<CityResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityDataFunction.class);

    private final CityMapper cityMapper;
    private final DynamoDbClient dynamoDbClient;

    public CityDataFunction(CityMapper cityMapper, DynamoDbClient dynamoDbClient) {
        this.cityMapper = cityMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Finds a city by its identifier.
     *
     * @return The city with the matching identifier, or null if not found.
     */
    @Override
    public Mono<CityResponse> apply(Mono<CityIdRequest> cityIdRequestMessage) {
        LOGGER.debug("Finding city with ID - START");
        return cityIdRequestMessage
            .flatMap(cityIdRequest -> BeanValidationUtils.validateBean(Mono.just(cityIdRequest))
                .thenReturn(cityIdRequest))
            .flatMap(cityIdRequest -> {
                HashMap<String, AttributeValue> keyToGet = new HashMap<>();
                keyToGet.put(City.ID_COLUMN_NAME, AttributeValue.builder().s(cityIdRequest.id()).build());
                GetItemRequest request = GetItemRequest.builder()
                    .key(keyToGet)
                    .tableName(City.CITY_TABLE_NAME)
                    .build();

                return Mono.fromCallable(() -> this.dynamoDbClient.getItem(request).item())
                    .flatMap(returnedItem -> {
                        Mono<CityResponse> response;
                        if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                            response = Mono.just(new CityResponse.Builder()
                                .httpStatus(HttpStatus.NOT_FOUND.value())
                                .errorMessage("City not found.")
                                .build());
                        } else {
                            City city = this.cityMapper.toCity(returnedItem);
                            if (city.status().equals(RecordStatus.DIS)) {
                                response = Mono.just(new CityResponse.Builder()
                                    .httpStatus(HttpStatus.NOT_ACCEPTABLE.value())
                                    .errorMessage("City is disabled.")
                                    .build());
                            } else {
                                response = Mono.just(this.cityMapper.toCityResponse(city, HttpStatus.OK.value(), null));
                            }
                        }
                        return response;
                    })
                    .onErrorResume(DynamoDbException.class, exception -> {
                        LOGGER.error("ERROR: When trying to find a City with ID '{}' >>> {}", cityIdRequest.id(), exception.getMessage());
                        return Mono.just(new CityResponse(null, null, null, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal server error when trying to find City data."));
                    });
            });
    }
}
