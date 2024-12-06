package hiperium.city.read.function.commons;

import java.time.ZonedDateTime;

/**
 * Represents metadata attributes of an entity, including timestamps for when the entity was created
 * and last updated.
 * <p>
 * This record is used to encapsulate audit-related information, giving insight into the lifecycle of
 * an entity by providing the moments of its creation and most recent modification.
 *
 * @param createdAt the timestamp indicating when the entity was initially created
 * @param updatedAt the timestamp indicating the most recent update to the entity
 */
public record EntityMetadata(
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
}
