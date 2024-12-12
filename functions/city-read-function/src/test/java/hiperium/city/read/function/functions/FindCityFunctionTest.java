package hiperium.city.read.function.functions;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import hiperium.city.functions.common.responses.FunctionResponse;
import hiperium.city.functions.tests.utils.DynamoDbTableTest;
import hiperium.city.functions.tests.utils.EventDeserializerTest;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionApplication.class)
class FindCityFunctionTest extends TestContainersBase {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private FunctionCatalog functionCatalog;

    @Value("${cities.table.name}")
    private String tableName;

    @BeforeEach
    void init() {
        DynamoDbTableTest.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Valid requests")
    void givenValidRequest_whenInvokeLambdaFunction_thenExecuteSuccessfully() throws IOException {
        String jsonContent = this.getJsonFromFilePath("requests/city/valid/find-city-by-id-request.json");
        assertThat(jsonContent).isNotNull();

        Message<APIGatewayProxyRequestEvent> eventMessage = EventDeserializerTest
            .deserializeRequestEvent(jsonContent, this.messageConverter);
        assertThat(eventMessage).isNotNull();

        // Find the corresponding function by name.
        Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> function = this.findFunctionUnderTest();
        assertThat(function).isNotNull();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(eventMessage))
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
        "requests/city/non-valid/non-existing-city.json"
    })
    void givenNonValidRequests_whenInvokeLambdaFunction_thenReturnErrors(String jsonFilePath) throws IOException {
        String jsonContent = this.getJsonFromFilePath(jsonFilePath);
        assertThat(jsonContent).isNotNull();

        Message<APIGatewayProxyRequestEvent> eventMessage = EventDeserializerTest
            .deserializeRequestEvent(jsonContent, this.messageConverter);
        assertThat(eventMessage).isNotNull();

        // Find the corresponding function by name.
        Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> function =
            this.findFunctionUnderTest();
        assertThat(function).isNotNull();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(eventMessage))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.statusCode()).isNotEqualTo(HttpStatus.OK.value());
                int errorCode = response.statusCode();
                assertThat(errorCode >= HttpStatus.OK.value() && errorCode <= HttpStatus.IM_USED.value()).isFalse();
            })
            .verifyComplete();
    }

    private Function<Message<APIGatewayProxyRequestEvent>, Mono<FunctionResponse>> findFunctionUnderTest() {
        return this.functionCatalog.lookup(Function.class, FindCityFunction.FUNCTION_NAME);
    }

    private String getJsonFromFilePath(String pathOfJsonDataFile) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathOfJsonDataFile);
        if (Objects.isNull(inputStream)) {
            throw new IOException("File not found: " + pathOfJsonDataFile);
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
