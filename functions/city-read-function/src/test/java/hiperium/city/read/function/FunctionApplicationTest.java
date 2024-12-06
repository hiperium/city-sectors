package hiperium.city.read.function;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hiperium.cities.common.responses.FunctionResponse;
import hiperium.cities.common.utils.TestUtils;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.configurations.NonRoutableMessageHandler;
import hiperium.city.read.function.requests.FunctionRequest;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionApplication.class)
class FunctionApplicationTest extends TestContainersBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @Value("${cities.table.name}")
    private String tableName;

    @BeforeEach
    void init() {
        TestUtils.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/city/valid/find-city-by-id-request.json",
        "requests/city-sectors/valid/find-sectors-by-city-id-request.json"
    })
    void givenValidRequest_whenInvokeLambdaFunction_thenExecuteSuccessfully(String jsonFilePath) throws IOException {
        Message<FunctionRequest> requestMessage;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assertThat(inputStream).isNotNull();

            // Create a message object with the content of the JSON file.
            JsonNode requestNode = OBJECT_MAPPER.readTree(inputStream);
            requestMessage = MessageBuilder
                .withPayload(OBJECT_MAPPER.readValue(requestNode.toString(), FunctionRequest.class))
                .build();
        }

        // Find the corresponding function by name.
        Function<Message<FunctionRequest>, Mono<FunctionResponse>> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(requestMessage))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @DisplayName("Non-valid requests")
    @ValueSource(strings = {
        "requests/city/non-valid/empty-city-id.json",
        "requests/city/non-valid/wrong-city-id.json",
        "requests/city/non-valid/non-existing-city.json",
        "requests/city-sectors/non-valid/empty-city-id.json",
        "requests/city-sectors/non-valid/wrong-city-id.json",
        "requests/city-sectors/non-valid/non-existing-city.json"
    })
    void givenNonValidRequests_whenInvokeLambdaFunction_thenReturnErrors(String jsonFilePath) throws IOException {
        Message<FunctionRequest> requestMessage;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assertThat(inputStream).isNotNull();

            // Create a message object with the content of the JSON file.
            JsonNode requestNode = OBJECT_MAPPER.readTree(inputStream);
            requestMessage = MessageBuilder
                .withPayload(OBJECT_MAPPER.readValue(requestNode.toString(), FunctionRequest.class))
                .build();
        }

        // Find the corresponding function by name.
        Function<Message<FunctionRequest>, Mono<FunctionResponse>> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(requestMessage))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.statusCode()).isNotEqualTo(HttpStatus.OK.value());
                int errorCode = response.statusCode();
                assertThat(errorCode >= HttpStatus.OK.value() && errorCode <= HttpStatus.IM_USED.value()).isFalse();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Wrong function name")
    void givenRequestWithWrongFunctionName_whenInvokeLambdaFunction_thenMustLogMessage() throws IOException {
        Message<FunctionRequest> requestMessage;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("requests/common/non-valid/wrong-function-name.json")) {
            assertThat(inputStream).isNotNull();

            // Create a message object with the content of the JSON file.
            JsonNode requestNode = OBJECT_MAPPER.readTree(inputStream);
            requestMessage = MessageBuilder
                .withPayload(OBJECT_MAPPER.readValue(requestNode.toString(), FunctionRequest.class))
                .build();
        }

        // Capture log messages.
        LogCaptor logCaptor = LogCaptor.forClass(NonRoutableMessageHandler.class);

        // Find the corresponding function by name.
        Function<Message<FunctionRequest>, Mono<FunctionResponse>> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Execute the function.
        Mono<FunctionResponse> response = function.apply(requestMessage);
        assertThat(response).isNull();

        // Validate log message.
        assertThat(logCaptor.getWarnLogs()).isNotEmpty();
        assertThat(logCaptor.getWarnLogs())
            .anyMatch(log -> log.contains("The following message couldn't be routed"));
    }

    private Function<Message<FunctionRequest>, Mono<FunctionResponse>> findRoutingFunction() {
        return this.functionCatalog.lookup(Function.class, RoutingFunction.FUNCTION_NAME);
    }
}
