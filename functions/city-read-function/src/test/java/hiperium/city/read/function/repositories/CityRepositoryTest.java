package hiperium.city.read.function.repositories;

import hiperium.cities.common.enums.RecordStatus;
import hiperium.cities.common.utils.TestUtils;
import hiperium.city.read.function.FunctionApplication;
import hiperium.city.read.function.common.TestContainersBase;
import hiperium.city.read.function.entities.CityEntity;
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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = FunctionApplication.class)
public class CityRepositoryTest extends TestContainersBase {

    @Autowired
    private CityRepository cityRepository;

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
        StepVerifier.create(this.cityRepository.findByCityId(FunctionTestUtils.ACTIVE_CITY_ID))
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.items().isEmpty());
                assertEquals(1, response.items().size());

                // Verify the returned item has the expected attributes
                Map<String, AttributeValue> item = response.items().getFirst();
                assertEquals(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.ACTIVE_CITY_ID, item.get("pk").s());
                assertEquals(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.ACTIVE_CITY_ID, item.get("sk").s());
                assertEquals(RecordStatus.ACTIVE.getValue(), item.get("status").s());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find City by ID - Inactive")
    void givenCityId_whenFindInactiveCity_mustReturnCityData() {
        StepVerifier.create(this.cityRepository.findByCityId(FunctionTestUtils.INACTIVE_CITY_ID))
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.items().isEmpty());
                assertEquals(1, response.items().size());

                // Verify the returned item has the expected attributes
                Map<String, AttributeValue> item = response.items().getFirst();
                assertEquals(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.INACTIVE_CITY_ID, item.get("pk").s());
                assertEquals(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.INACTIVE_CITY_ID, item.get("sk").s());
                assertEquals(RecordStatus.INACTIVE.getValue(), item.get("status").s());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find active sectors by city ID")
    void givenCityId_whenFindSectorsByCityId_thenReturnAllSectors() {
        StepVerifier.create(this.cityRepository.findSectorsByCityAndStatus(FunctionTestUtils.ACTIVE_CITY_ID, RecordStatus.ACTIVE))
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.items());
                assertFalse(response.items().isEmpty());

                // Verify all items belong to the same city
                response.items()
                    .forEach(item ->
                        assertEquals(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.ACTIVE_CITY_ID, item.get("pk").s()));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find active sectors for non-existent city")
    void givenNonExistentCityId_whenFindSectorsByCity_thenReturnEmptyResponse() {
        String nonExistentCityId = "non-existent-city";

        StepVerifier.create(this.cityRepository.findSectorsByCityAndStatus(nonExistentCityId, RecordStatus.ACTIVE))
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.items().isEmpty());
            })
            .verifyComplete();
    }
}
