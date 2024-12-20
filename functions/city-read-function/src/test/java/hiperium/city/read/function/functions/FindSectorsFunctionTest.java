package hiperium.city.read.function.functions;

import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.DeserializerUtil;
import hiperium.city.functions.common.utils.ResponseUtil;
import hiperium.city.functions.tests.utils.DynamoDbTableUtil;
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
import org.springframework.messaging.support.MessageBuilder;
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
class FindSectorsFunctionTest extends TestContainersBase {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @Value("${city.table}")
    private String tableName;

    @BeforeEach
    void init() {
        DynamoDbTableUtil.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Valid requests")
    void givenValidRequest_whenInvokeLambdaFunction_thenExecuteSuccessfully() throws IOException {
        String jsonContent = this.getJsonFromFilePath("requests/city-sectors/valid/find-sectors-by-city-id-request.json");
        assertThat(jsonContent).isNotNull();

        FunctionRequest functionRequest = DeserializerUtil.fromJson(jsonContent);
        assertThat(functionRequest).isNotNull();

        // Find the corresponding function by name.
        Function<Message<FunctionRequest>, Mono<Message<String>>> function = this.findFunctionUnderTest();
        assertThat(function).isNotNull();

        // Create a message with the request payload.
        Message<FunctionRequest> message = MessageBuilder
            .withPayload(functionRequest)
            .build();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(message))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getHeaders().get(ResponseUtil.LAMBDA_STATUS_CODE)).isEqualTo(HttpStatus.OK.value());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @DisplayName("Non-valid requests")
    @ValueSource(strings = {
        "requests/city-sectors/non-valid/empty-city-id.json",
        "requests/city-sectors/non-valid/wrong-city-id.json",
        "requests/city-sectors/non-valid/non-existing-city.json"
    })
    void givenNonValidRequests_whenInvokeLambdaFunction_thenReturnErrors(String jsonFilePath) throws IOException {
        String jsonContent = this.getJsonFromFilePath(jsonFilePath);
        assertThat(jsonContent).isNotNull();

        FunctionRequest functionRequest = DeserializerUtil.fromJson(jsonContent);
        assertThat(functionRequest).isNotNull();

        // Find the corresponding function by name.
        Function<Message<FunctionRequest>, Mono<Message<String>>> function = this.findFunctionUnderTest();
        assertThat(function).isNotNull();

        // Create a message with the request payload.
        Message<FunctionRequest> message = MessageBuilder
            .withPayload(functionRequest)
            .build();

        // Execute the function and verify the response.
        StepVerifier.create(function.apply(message))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getHeaders().get(ResponseUtil.LAMBDA_STATUS_CODE)).isNotEqualTo(HttpStatus.OK.value());
                int errorCode = (int) Objects.requireNonNull(response.getHeaders().get(ResponseUtil.LAMBDA_STATUS_CODE));
                assertThat(errorCode >= HttpStatus.OK.value() && errorCode <= HttpStatus.IM_USED.value()).isFalse();
            })
            .verifyComplete();
    }

    private Function<Message<FunctionRequest>, Mono<Message<String>>> findFunctionUnderTest() {
        return this.functionCatalog.lookup(Function.class, FindSectorsFunction.FUNCTION_NAME);
    }

    private String getJsonFromFilePath(String pathOfJsonDataFile) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathOfJsonDataFile);
        if (Objects.isNull(inputStream)) {
            throw new IOException("File not found: " + pathOfJsonDataFile);
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}