package hiperium.city.read.function.repositories;

import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.tests.utils.DynamoDbTableTest;
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

import static org.assertj.core.api.Assertions.assertThat;

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
        DynamoDbTableTest.waitForDynamoDbToBeReady(this.dynamoDbClient, this.tableName, 12, 3);
    }

    @Test
    @DisplayName("Find City by ID - Active")
    void givenCityId_whenFindActiveCity_mustReturnCityData() {
        StepVerifier.create(this.cityRepository.findByCityId(FunctionTestUtils.ACTIVE_CITY_ID, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.items()).isNotEmpty();
                assertThat(response.items().size()).isEqualTo(1);

                // Verify the returned item has the expected attributes
                Map<String, AttributeValue> item = response.items().getFirst();
                assertThat(item.get("pk").s()).isEqualTo(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.ACTIVE_CITY_ID);
                assertThat(item.get("sk").s()).isEqualTo(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.ACTIVE_CITY_ID);
                assertThat(item.get("status").s()).isEqualTo(RecordStatus.ACTIVE.getValue());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Find City by ID - Inactive")
    void givenCityId_whenFindInactiveCity_mustReturnCityData() {
        StepVerifier.create(this.cityRepository.findByCityId(FunctionTestUtils.INACTIVE_CITY_ID, FunctionTestUtils.REQUEST_ID))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.items()).isNotEmpty();
                assertThat(response.items().size()).isEqualTo(1);

                // Verify the returned item has the expected attributes
                Map<String, AttributeValue> item = response.items().getFirst();
                assertThat(item.get("pk").s()).isEqualTo(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.INACTIVE_CITY_ID);
                assertThat(item.get("sk").s()).isEqualTo(CityEntity.CITY_PK_PREFIX + FunctionTestUtils.INACTIVE_CITY_ID);
                assertThat(item.get("status").s()).isEqualTo(RecordStatus.INACTIVE.getValue());
            })
            .verifyComplete();
    }
}
