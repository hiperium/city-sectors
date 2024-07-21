package hiperium.city.data.function;

import hiperium.city.data.function.common.TestContainersBase;
import hiperium.city.data.function.configurations.FunctionsConfig;
import hiperium.city.data.function.dto.CityIdRequest;
import hiperium.city.data.function.dto.CityResponse;
import hiperium.city.data.function.utils.TestsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = FunctionalApplication.class)
class FunctionalApplicationTest extends TestContainersBase {

    private static final String ENABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328528";
    private static final String DISABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328529";

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @BeforeEach
    void init() {
        TestsUtils.waitForDynamoDbToBeReady(this.dynamoDbClient);
    }

    @Test
    @DisplayName("City found")
    void givenEnabledCityId_whenInvokeLambdaFunction_thenReturnCityData() {
        Function<Mono<CityIdRequest>, Mono<CityResponse>> cityDataFunction = this.getFunctionUnderTest();
        Mono<CityResponse> cityResponseMono = cityDataFunction.apply(Mono.just(new CityIdRequest(ENABLED_CITY_ID)));

        StepVerifier.create(cityResponseMono)
            .assertNext(cityResponse -> {
                assertThat(cityResponse).isNotNull();
                assertThat(cityResponse.id()).isEqualTo(ENABLED_CITY_ID);
                assertThat(cityResponse.httpStatus()).isEqualTo(HttpStatus.OK.value());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("City not found")
    void givenNonExistingCityId_whenInvokeLambdaFunction_thenReturnError() {
        Function<Mono<CityIdRequest>, Mono<CityResponse>> cityDataFunction = this.getFunctionUnderTest();
        Mono<CityResponse> cityResponseMono = cityDataFunction.apply(Mono.just(new CityIdRequest("non-existing-id")));

        StepVerifier.create(cityResponseMono)
            .assertNext(cityResponse -> {
                assertThat(cityResponse).isNotNull();
                assertThat(cityResponse.id()).isNull();
                assertThat(cityResponse.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("City disabled")
    void givenDisabledCityId_whenInvokeLambdaFunction_thenReturnError() {
        Function<Mono<CityIdRequest>, Mono<CityResponse>> cityDataFunction = this.getFunctionUnderTest();
        Mono<CityResponse> cityResponseMono = cityDataFunction.apply(Mono.just(new CityIdRequest(DISABLED_CITY_ID)));

        StepVerifier.create(cityResponseMono)
            .assertNext(cityResponse -> {
                assertThat(cityResponse).isNotNull();
                assertThat(cityResponse.id()).isNull();
                assertThat(cityResponse.httpStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
            })
            .verifyComplete();
    }

    private Function<Mono<CityIdRequest>, Mono<CityResponse>> getFunctionUnderTest() {
        Function<Mono<CityIdRequest>, Mono<CityResponse>> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
