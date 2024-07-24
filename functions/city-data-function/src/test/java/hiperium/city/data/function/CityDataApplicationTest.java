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
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@FunctionalSpringBootTest(classes = CityDataApplication.class)
class CityDataApplicationTest extends TestContainersBase {

    private static final String ENABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328528";
    private static final String DISABLED_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328529";
    private static final String NON_EXISTING_CITY_ID = "a0ecb466-7ef5-47bf-a1ca-12f9f9328530";

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private FunctionCatalog functionCatalog;

    @BeforeEach
    void init() {
        TestsUtils.waitForDynamoDbToBeReady(this.dynamoDbClient);
    }

    @Test
    @DisplayName("Existing city")
    void givenExistingCity_whenInvokeLambdaFunction_thenReturnCityData() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest(ENABLED_CITY_ID));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ENABLED_CITY_ID);
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Non-existing city")
    void givenNonExistingCity_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest(NON_EXISTING_CITY_ID));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Disabled city")
    void givenDisabledCity_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest(DISABLED_CITY_ID));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Null parameter")
    void givenNullRequestParam_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest(null));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Blank parameter")
    void givenBlankRequestParam_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest(StringUtils.SPACE));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.errorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Invalid UUID parameter")
    void givenInvalidUUID_whenInvokeLambdaFunction_thenReturnError() {
        Function<Message<CityIdRequest>, CityResponse> function = this.getFunctionUnderTest();
        Message<CityIdRequest> requestMessage = TestsUtils.createMessage(new CityIdRequest("a0ecb466-7ef5-47bf"));
        CityResponse response = function.apply(requestMessage);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNull();
        assertThat(response.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.errorMessage()).isEqualTo("Invalid UUID");
    }

    private Function<Message<CityIdRequest>, CityResponse> getFunctionUnderTest() {
        Function<Message<CityIdRequest>, CityResponse> function = this.functionCatalog.lookup(Function.class,
            FunctionsConfig.FIND_BY_ID_BEAN_NAME);
        assertThat(function).isNotNull();
        return function;
    }
}
