package hiperium.city.read.function.functions;

import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.DeserializerUtil;
import hiperium.city.functions.common.utils.ResponseUtil;
import hiperium.city.functions.tests.utils.DynamoDbTableUtil;
import hiperium.city.functions.tests.utils.ResourceStreamUtil;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.configurations.NonRoutableMessageHandler;
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
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionApplication.class)
class RoutingFunctionTest extends TestContainersBase {

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

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/city/valid/find-city-by-id-request.json",
        "requests/city-sectors/valid/find-sectors-by-city-id-request.json",
    })
    void givenValidRequest_whenInvokeLambdaFunction_thenMustExecuteSuccessfully(String jsonFilePath) throws IOException {
        String jsonContent = ResourceStreamUtil.getJsonFromFilePath(jsonFilePath);
        assertThat(jsonContent).isNotNull();

        FunctionRequest functionRequest = DeserializerUtil.fromJson(jsonContent);
        assertThat(functionRequest).isNotNull();

        // Find the corresponding function by name.
        Function<Object, Object> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Create a message with the request payload.
        Message<FunctionRequest> message = MessageBuilder
            .withPayload(functionRequest)
            .copyHeaders(functionRequest.headers())
            .build();

        // Execute the function and verify the response.
        Object functionResponse = function.apply(message);

        StepVerifier.create((Mono<Message<String>>) functionResponse)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getHeaders().get(ResponseUtil.LAMBDA_STATUS_CODE)).isEqualTo(HttpStatus.OK.value());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Wrong function name")
    void givenRequestWithWrongFunctionName_whenInvokeLambdaFunction_thenMustLogMessage() throws IOException {
        String jsonContent = ResourceStreamUtil.getJsonFromFilePath("requests/non-valid/wrong-function-name.json");
        assertThat(jsonContent).isNotNull();

        FunctionRequest functionRequest = DeserializerUtil.fromJson(jsonContent);
        assertThat(functionRequest).isNotNull();

        // Capture log messages.
        LogCaptor logCaptor = LogCaptor.forClass(NonRoutableMessageHandler.class);

        // Find the corresponding function by name.
        Function<Object, Object> function = this.findRoutingFunction();
        assertThat(function).isNotNull();

        // Create a message with the request payload.
        Message<FunctionRequest> message = MessageBuilder
            .withPayload(functionRequest)
            .copyHeaders(functionRequest.headers())
            .build();
        
        // Execute the function.
        Object response = function.apply(message);
        assertThat(response).isNull();

        // Validate log message.
        assertThat(logCaptor.getWarnLogs()).isNotEmpty();
        assertThat(logCaptor.getWarnLogs())
            .anyMatch(log -> log.contains("The following message couldn't be routed"));
    }

    private Function<Object, Object> findRoutingFunction() {
        return this.functionCatalog.lookup(Function.class, RoutingFunction.FUNCTION_NAME);
    }
}
