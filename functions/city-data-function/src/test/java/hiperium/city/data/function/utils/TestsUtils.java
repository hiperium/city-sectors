package hiperium.city.data.function.utils;

import hiperium.city.data.function.dto.CityIdRequest;
import hiperium.city.data.function.entities.City;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.time.Duration;

public final class TestsUtils {

    private TestsUtils() {
    }

    public static void waitForDynamoDbToBeReady(final DynamoDbClient dynamoDbClient) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))         // maximum wait time
            .pollInterval(Duration.ofSeconds(3))    // check every 3 seconds
            .until(() -> {
                DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(City.CITY_TABLE_NAME)
                    .build();
                try {
                    TableStatus tableStatus = dynamoDbClient.describeTable(request).table().tableStatus();
                    return TableStatus.ACTIVE.equals(tableStatus);
                } catch (ResourceNotFoundException e) {
                    return false;
                }
            });
    }
}
