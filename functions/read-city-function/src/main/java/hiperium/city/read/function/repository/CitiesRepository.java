package hiperium.city.read.function.repository;

import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.CityDataRequest;
import hiperium.city.read.function.entities.City;
import hiperium.city.read.function.mappers.CityMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The CitiesRepository class is responsible for retrieving City objects from the DynamoDB table.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class CitiesRepository {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(CitiesRepository.class);

    private final CityMapper cityMapper;
    private final DynamoDbClient dynamoDbClient;

    /**
     * The CitiesRepository class is responsible for retrieving City objects from the DynamoDB table.
     */
    public CitiesRepository(CityMapper cityMapper, DynamoDbClient dynamoDbClient) {
        this.cityMapper = cityMapper;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Finds a city by its unique identifier.
     *
     * @param cityDataRequest The request object containing the city identifier.
     * @return The City object with the matching identifier.
     * @throws ResourceNotFoundException if a city with the specified identifier is not found.
     * @throws RuntimeException if there is an error finding the city in the database.
     */
    public City findById(CityDataRequest cityDataRequest) {
        LOGGER.debug("Find City by ID", cityDataRequest);

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(City.ID_COLUMN_NAME, AttributeValue.builder().s(cityDataRequest.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyToGet)
            .tableName(City.TABLE_NAME)
            .build();

        City city;
        try {
            Map<String, AttributeValue> returnedItem = this.dynamoDbClient.getItem(itemRequest).item();
            if (Objects.isNull(returnedItem) || returnedItem.isEmpty()) {
                throw new ResourceNotFoundException("City not found with ID: " + cityDataRequest.cityId());
            }
            city = this.cityMapper.mapToCity(returnedItem);
        } catch (DynamoDbException exception) {
            LOGGER.error("When trying to find a City with ID: " + cityDataRequest.cityId(), exception.getMessage());
            throw new RuntimeException("Error finding City with ID: " + cityDataRequest.cityId(), exception);
        }
        return city;
    }
}
