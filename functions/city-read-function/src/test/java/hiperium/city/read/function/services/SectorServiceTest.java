package hiperium.city.read.function.services;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.InactiveCityException;
import hiperium.city.functions.common.exceptions.ResourceNotFoundException;
import hiperium.city.functions.tests.utils.DynamoDbTableTest;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.requests.CityDataRequest;
import hiperium.city.read.function.utils.FunctionTestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ActiveProfiles("test")
@SpringBootTest(classes = FunctionApplication.class)
public class SectorServiceTest extends TestContainersBase {

    @Autowired
    private SectorService sectorService;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${cities.table.name}")
    private String tableName;

    @BeforeEach
    void setup() {
        DynamoDbTableTest.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Find sectors by City ID - Active")
    void givenActiveCityId_whenFindSectors_mustReturnActiveSectorsData() {
        CityDataRequest cityDataRequest = new CityDataRequest(FunctionTestUtils.ACTIVE_CITY_ID);

        StepVerifier.create(this.sectorService.findActiveSectorsByCityId(cityDataRequest, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                Assertions.assertThat(response).isNotNull();
                Assertions.assertThat(response).isNotEmpty();
                Assertions.assertThat(response.size()).isGreaterThan(0);

                // All returned sectors should be active.
                response.forEach(sector ->
                    Assertions.assertThat(sector.entityCommon().status()).isEqualTo(RecordStatus.ACTIVE));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find sectors by City ID - Inactive")
    void givenInactiveCityId_whenFindSectors_mustReturnError() {
        CityDataRequest cityDataRequest = new CityDataRequest(FunctionTestUtils.INACTIVE_CITY_ID);

        StepVerifier.create(this.sectorService.findActiveSectorsByCityId(cityDataRequest, FunctionTestUtils.REQUEST_ID))
            .expectErrorMatches(throwable -> throwable instanceof InactiveCityException)
            .verify();
    }

    @Test
    @DisplayName("Find sectors by City ID - Non-existing")
    void givenNonExistingCityId_whenFindSectors_mustReturnError() {
        CityDataRequest cityDataRequest = new CityDataRequest("non-existing-city-id");

        StepVerifier.create(this.sectorService.findActiveSectorsByCityId(cityDataRequest, FunctionTestUtils.REQUEST_ID))
            .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
            .verify();
    }
}
