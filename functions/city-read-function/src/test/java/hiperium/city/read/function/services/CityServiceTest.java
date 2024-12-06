package hiperium.city.read.function.services;

import hiperium.cities.common.enums.RecordStatus;
import hiperium.cities.common.exceptions.InactiveCityException;
import hiperium.cities.common.exceptions.ResourceNotFoundException;
import hiperium.cities.common.utils.TestUtils;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.requests.FunctionRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        TestUtils.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Find City by ID - Active")
    void givenCityId_whenFindActiveCity_mustReturnCityData() {
        FunctionRequest functionRequest = new FunctionRequest(null, FunctionTestUtils.ACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveCityById(functionRequest))
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.entityCommon());
                assertNotNull(response.countryCode());
                assertNotNull(response.languageCode());
                assertNotNull(response.timezone());
                assertNotNull(response.entityMetadata());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find City by ID - Inactive")
    void givenCityId_whenFindInactiveCity_mustReturnCityData() {
        FunctionRequest functionRequest = new FunctionRequest(null, FunctionTestUtils.INACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveCityById(functionRequest))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }

    @Test
    @DisplayName("Find sectors by City ID - Active")
    void givenActiveCityId_whenFindSectors_mustReturnActiveSectorsData() {
        FunctionRequest functionRequest = new FunctionRequest(null, FunctionTestUtils.ACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveSectorsByCityId(functionRequest))
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.isEmpty());
                assertEquals(FunctionTestUtils.ACTIVE_CITY_ACTIVE_SECTORS_COUNT, response.size());

                // All returned sectors should be active.
                response.forEach(sector ->
                    assertEquals(RecordStatus.ACTIVE, sector.entityCommon().status()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find sectors by City ID - Inactive")
    void givenInactiveCityId_whenFindSectors_mustReturnError() {
        FunctionRequest functionRequest = new FunctionRequest(null, FunctionTestUtils.INACTIVE_CITY_ID);

        StepVerifier.create(this.cityService.findActiveSectorsByCityId(functionRequest))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }

    @Test
    @DisplayName("Find sectors by City ID - Non-existing")
    void givenNonExistingCityId_whenFindSectors_mustReturnError() {
        FunctionRequest functionRequest = new FunctionRequest(null, "non-existing-city-id");

        StepVerifier.create(this.cityService.findActiveSectorsByCityId(functionRequest))
            .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
            .verify();
    }
}
