package hiperium.city.read.function;

import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.configurations.FunctionsConfig;
import hiperium.city.read.function.dto.CityDataResponse;
import hiperium.city.read.function.utils.TestsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
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
@FunctionalSpringBootTest(classes = CityReadApplication.class)
class CityReadApplicationTest extends TestContainersBase {

    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @BeforeEach
    void init() {
        TestsUtils.waitForDynamoDbToBeReady(this.dynamoDbAsyncClient);
    }

    @ParameterizedTest
    @DisplayName("Valid requests")
    @ValueSource(strings = {
        "requests/valid/lambda-valid-id-request.json"
    })
    void givenValidRequest_whenInvokeLambdaFunction_thenExecuteSuccessfully(String jsonFilePath) throws IOException {
        Function<Message<byte[]>, Mono<CityDataResponse>> function = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestsUtils.createMessage(inputStream.readAllBytes());

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
        Function<Message<byte[]>, Mono<CityDataResponse>> createEventFunction = this.getFunctionUnderTest();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath)) {
            assert inputStream != null;
            Message<byte[]> requestMessage = TestsUtils.createMessage(inputStream.readAllBytes());

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

    private Function<Message<byte[]>, Mono<CityDataResponse>> getFunctionUnderTest() {
        Function<Message<byte[]>, Mono<CityDataResponse>> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
