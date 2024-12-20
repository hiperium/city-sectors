package hiperium.city.read.function.repositories;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.tests.utils.DynamoDbTableUtil;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = FunctionApplication.class)
public class SectorRepositoryTest extends TestContainersBase {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Value("${city.table}")
    private String tableName;

    @BeforeEach
    void setup() {
        DynamoDbTableUtil.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Find active sectors by city ID")
    void givenCityId_whenFindSectorsByCityId_thenReturnAllSectors() {
        StepVerifier.create(this.sectorRepository.findSectorsByCityAndStatus(FunctionTestUtils.ACTIVE_CITY_ID,
                RecordStatus.ACTIVE, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.items()).isNotEmpty();
                assertThat(response.items().size()).isGreaterThan(1);

                // Verify all items belong to the same city
                response.items().forEach(item -> {
                    assertThat(item).containsKey("status");
                    assertThat(item.get("status").s()).isEqualTo(RecordStatus.ACTIVE.name());
                });
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find active sectors for non-existent city")
    void givenNonExistentCityId_whenFindSectorsByCity_thenReturnEmptyResponse() {
        String nonExistentCityId = "non-existent-city";

        StepVerifier.create(this.sectorRepository.findSectorsByCityAndStatus(nonExistentCityId,
                RecordStatus.ACTIVE, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.items()).isEmpty();
            })
            .verifyComplete();
    }
}
