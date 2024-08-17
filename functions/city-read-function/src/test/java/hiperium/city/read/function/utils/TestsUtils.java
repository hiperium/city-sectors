package hiperium.city.read.function.utils;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.entities.City;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestsUtils {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(TestsUtils.class);

    public static void waitForDynamoDbToBeReady(final DynamoDbAsyncClient dynamoDbAsyncClient) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(15))         // maximum wait time
            .pollInterval(Duration.ofSeconds(3))    // check every 3 seconds
            .until(() -> {
                DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(City.TABLE_NAME)
                    .build();
                CompletableFuture<DescribeTableResponse> future = dynamoDbAsyncClient.describeTable(request);
                try {
                    TableStatus tableStatus = future.get(500, TimeUnit.MILLISECONDS).table().tableStatus();
                    return TableStatus.ACTIVE.equals(tableStatus);
                } catch (ResourceNotFoundException e) {
                    LOGGER.error("Cities table was not found", e.getMessage());
                } catch (Exception e) {
                    LOGGER.error("Error when trying to describe the Cities table", e.getMessage());
                }
                return false;
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
