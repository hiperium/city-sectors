package hiperium.city.read.function.services;

import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.functions.tests.utils.DynamoDbTableTest;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.requests.CityDataRequest;
import hiperium.city.read.function.utils.FunctionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = FunctionApplication.class)
public class CityServiceTest extends TestContainersBase {

    @Autowired
    private CityService cityService;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${cities.table.name}")
    private String tableName;

    @BeforeEach
    void setup() {
        DynamoDbTableTest.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Find City by ID - Active")
    void givenCityId_whenFindActiveCity_mustReturnCityData() {
        CityDataRequest cityDataRequest = new CityDataRequest(FunctionTestUtils.ACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveCityById(cityDataRequest, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.entityCommon()).isNotNull();
                assertThat(response.countryCode()).isNotNull();
                assertThat(response.languageCode()).isNotNull();
                assertThat(response.timezone()).isNotNull();
                assertThat(response.entityMetadata()).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find City by ID - Inactive")
    void givenCityId_whenFindInactiveCity_mustReturnCityData() {
        CityDataRequest cityDataRequest = new CityDataRequest(FunctionTestUtils.INACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveCityById(cityDataRequest, FunctionTestUtils.REQUEST_ID))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }
}
