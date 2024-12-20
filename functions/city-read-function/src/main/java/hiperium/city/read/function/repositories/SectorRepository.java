package hiperium.city.read.function.repositories;

import hiperium.city.functions.common.enums.ErrorCode;
import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.entities.SectorEntity;
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
 * The SectorRepository class is responsible for retrieving City Sector objects from the DynamoDB table.
 * <p>
 * @apiNote The Enhanced Client has problems at runtime when used with Spring Native.
 * This is because the Enhanced Client uses reflection to create the DynamoDbAsyncClient.
 * The solution is to use the low-level client instead.
 */
@Repository
public class SectorRepository {

    private final String tableName;
    private final DynamoDbClient dynamoDbClient;

    public SectorRepository(@Value("${city.table}") String tableName, DynamoDbClient dynamoDbClient) {
        this.tableName = tableName;
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Finds sectors by city ID and status from the DynamoDB table.
     * The method queries the sectors associated with the specified city ID
     * and filters them based on the given status.
     *
     * @param cityId    the ID of the city used to match sectors.
     * @param status    the status used to filter the sectors.
     * @param requestId the unique identifier of the request for tracking purposes.
     * @return a {@link Mono} that emits a {@link QueryResponse} containing the sectors
     * that match the city ID and status, or an error if the query fails.
     */
    public Mono<QueryResponse> findSectorsByCityAndStatus(final String cityId,
                                                          final RecordStatus status,
                                                          final String requestId) {

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#status", "status"); // status is a reserved word in DynamoDB.

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":pkValue", AttributeValue.builder()
            .s(CityEntity.CITY_PK_PREFIX + cityId)
            .build());
        expressionAttributeValues.put(":sectorPrefixValue", AttributeValue.builder()
            .s(SectorEntity.SECTOR_SK_PREFIX)
            .build());
        expressionAttributeValues.put(":statusValue", AttributeValue.builder()
            .s(status.getValue())
            .build());

        QueryRequest request = QueryRequest.builder()
            .tableName(this.tableName)
            .keyConditionExpression("pk = :pkValue AND begins_with(sk, :sectorPrefixValue)")
            .filterExpression("#status = :statusValue")
            .expressionAttributeNames(expressionAttributeNames)
            .expressionAttributeValues(expressionAttributeValues)
            .build();

        return Mono.fromCallable(() -> this.dynamoDbClient.query(request))
            .onErrorMap(DynamoDbException.class, exception ->
                new CityException("Error when querying sectors by city ID: " + cityId,
                    ErrorCode.INTERNAL_001, requestId, exception))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
