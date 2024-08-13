package hiperium.city.data.function.utils;

import hiperium.city.data.function.entities.City;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.time.Duration;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestsUtils {

    public static void waitForDynamoDbToBeReady(final DynamoDbClient dynamoDbClient) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))         // maximum wait time
            .pollInterval(Duration.ofSeconds(3))    // check every 3 seconds
            .until(() -> {
                DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(City.TABLE_NAME)
                    .build();
                try {
                    TableStatus tableStatus = dynamoDbClient.describeTable(request).table().tableStatus();
                    return TableStatus.ACTIVE.equals(tableStatus);
                } catch (ResourceNotFoundException e) {
                    return false;
                }
            });
    }

    public static Message<byte[]> createMessage(byte[] bytes) {
        return new Message<>() {
            @NonNull
            @Override
            public byte[] getPayload() {
                return bytes;
            }

            @NonNull
            @Override
            public MessageHeaders getHeaders() {
                return new MessageHeaders(Map.of());
            }
        };
    }
}
