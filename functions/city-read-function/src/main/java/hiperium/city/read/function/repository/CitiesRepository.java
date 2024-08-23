package hiperium.city.read.function.repository;

import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.ReadCityRequest;
import hiperium.city.read.function.entities.City;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The CitiesRepository class is responsible for retrieving City objects from the DynamoDB table.
 *
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbAsyncClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class CitiesRepository {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(CitiesRepository.class);

    private final DynamoDbAsyncClient dynamoDbAsyncClient;

    @Value("${hiperium.cities.table.name}")
    private String tableName;

    /**
     * The CitiesRepository class is responsible for retrieving City objects from the DynamoDB table.
     */
    public CitiesRepository(DynamoDbAsyncClient dynamoDbAsyncClient) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    /**
     * Retrieves a City record from the DynamoDB table based on the provided ReadCityRequest asynchronously.
     *
     * @param readCityRequest The ReadCityRequest object containing the unique identifier of the city to retrieve.
     * @return A CompletableFuture that completes with a Map representing the retrieved City record.
     *         The keys in the Map represent the column names of the City table,
     *         and the values represent the corresponding attribute values.
     */
    public CompletableFuture<Map<String, AttributeValue>> findByIdAsync(final ReadCityRequest readCityRequest) {
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(City.ID_COLUMN_NAME, AttributeValue.builder().s(readCityRequest.cityId()).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
            .key(keyToGet)
            .tableName(this.tableName)
            .build();

        return this.dynamoDbAsyncClient.getItem(itemRequest)
            .thenApply(GetItemResponse::item)
            .exceptionally(exception -> {
                LOGGER.error("Error when trying to find a City by ID.", exception.getMessage(), readCityRequest);
                throw new CityException("Error when trying to find a City by ID.");
            });
    }
}
