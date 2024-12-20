package hiperium.city.read.function.repositories;

import hiperium.city.functions.common.enums.ErrorCode;
import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.read.function.entities.CityEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * The CityRepository class is responsible for retrieving City objects from the DynamoDB table.
 * <p>
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbAsyncClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class CityRepository {

    private final String tableName;
    private final DynamoDbClient dynamoDbClient;

    public CityRepository(@Value("${city.table}") String tableName, DynamoDbClient dynamoDbClient) {
        this.tableName = tableName;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Retrieves a city's data from the DynamoDB table using the specified city ID. The method uses
     * a key condition expression to query the database with the provided city ID.
     *
     * @param cityId    the ID of the city to be retrieved. It is used to form the partition key
     *                  for querying the database.
     * @param requestId the unique identifier of the request for tracking purposes.
     * @return a {@link Mono} that emits the {@link QueryResponse} containing items related to the specified city ID,
     * or an error if the data retrieval fails.
     */
    public Mono<QueryResponse> findByCityId(final String cityId, final String requestId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":pkValue", AttributeValue.builder()
            .s(CityEntity.CITY_PK_PREFIX + cityId)
            .build());
        expressionAttributeValues.put(":skValue", AttributeValue.builder()
            .s(CityEntity.CITY_PK_PREFIX + cityId)
            .build());

        QueryRequest request = QueryRequest.builder()
            .tableName(this.tableName)
            .keyConditionExpression("pk = :pkValue and sk = :skValue")
            .expressionAttributeValues(expressionAttributeValues)
            .build();

        return Mono.fromCallable(() -> this.dynamoDbClient.query(request))
            .onErrorMap(DynamoDbException.class, exception ->
                new CityException("Error when retrieving city data with ID: " + cityId,
                    ErrorCode.INTERNAL_001, requestId, exception))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
