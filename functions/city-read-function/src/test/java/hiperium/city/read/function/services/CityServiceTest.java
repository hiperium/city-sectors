package hiperium.city.read.function.services;

import hiperium.cities.commons.enums.RecordStatus;
import hiperium.cities.commons.exceptions.InactiveCityException;
import hiperium.cities.commons.exceptions.ResourceNotFoundException;
import hiperium.cities.commons.utils.TestUtils;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
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
        StepVerifier.create(this.cityService.findActiveCityById(FunctionTestUtils.ACTIVE_CITY_ID))
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.commonAttributes());
                assertNotNull(response.countryCode());
                assertNotNull(response.languageCode());
                assertNotNull(response.timezone());
                assertNotNull(response.metadataAttributes());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find City by ID - Inactive")
    void givenCityId_whenFindInactiveCity_mustReturnCityData() {
        StepVerifier.create(this.cityService.findActiveCityById(FunctionTestUtils.INACTIVE_CITY_ID))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }

    @Test
    @DisplayName("Find sectors by City ID - Active")
    void givenActiveCityId_whenFindSectors_mustReturnActiveSectorsData() {
        StepVerifier.create(this.cityService.findActiveSectorsByCityId(FunctionTestUtils.ACTIVE_CITY_ID))
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.isEmpty());
                assertEquals(FunctionTestUtils.ACTIVE_CITY_ACTIVE_SECTORS_COUNT, response.size());

                // All returned sectors should be active.
                response.forEach(sector ->
                    assertEquals(RecordStatus.ACTIVE, sector.commonAttributes().status()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find sectors by City ID - Inactive")
    void givenInactiveCityId_whenFindSectors_mustReturnError() {
        StepVerifier.create(this.cityService.findActiveSectorsByCityId(FunctionTestUtils.INACTIVE_CITY_ID))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }

    @Test
    @DisplayName("Find sectors by City ID - Non-existing")
    void givenNonExistingCityId_whenFindSectors_mustReturnError() {
        String cityId = "non-existing-city-id";
        StepVerifier.create(this.cityService.findActiveSectorsByCityId(cityId))
            .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
            .verify();
    }
}
