package hiperium.city.read.function.commons;

import java.time.ZonedDateTime;

public record MetadataAttributes(
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
