package hiperium.city.read.function;

import hiperium.cities.commons.utils.TestUtils;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.configurations.FunctionConfig;
import hiperium.city.read.function.dto.ReadCityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = ReadCityApplication.class)
class ReadCityApplicationTest extends TestContainersBase {

    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @Value("${hiperium.cities.table.name}")
    private String tableName;

    @BeforeEach
    void init() {
        TestUtils.waitForDynamoDbToBeReady(this.dynamoDbAsyncClient, this.tableName);
    }

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/valid/lambda-valid-id-request.json"
    })
    void givenValidRequest_whenInvokeLambdaFunction_thenExecuteSuccessfully(String jsonFilePath) throws IOException {
        Function<Message<byte[]>, Mono<ReadCityResponse>> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestUtils.createMessage(inputStream.readAllBytes());

            StepVerifier.create(function.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.error()).isNull();
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @DisplayName("Non-valid requests")
    @ValueSource(strings = {
        "requests/invalid/empty-city-id.json",
        "requests/invalid/wrong-payload.json",
        "requests/invalid/wrong-city-id.json",
        "requests/invalid/non-existing-city.json",
    })
    void givenNonValidRequests_whenInvokeLambdaFunction_thenReturnErrors(String jsonFilePath) throws IOException {
        Function<Message<byte[]>, Mono<ReadCityResponse>> createEventFunction = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestUtils.createMessage(inputStream.readAllBytes());

            StepVerifier.create(createEventFunction.apply(requestMessage))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.error()).isNotNull();
                    int errorCode = response.error().errorCode();
                    assertThat(errorCode >= HttpStatus.OK.value() && errorCode <= HttpStatus.IM_USED.value()).isFalse();
                })
                .verifyComplete();
        }
    }

    private Function<Message<byte[]>, Mono<ReadCityResponse>> getFunctionUnderTest() {
        Function<Message<byte[]>, Mono<ReadCityResponse>> function = this.functionCatalog.lookup(Function.class,
            FunctionConfig.FUNCTION_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
