package hiperium.city.read.function.commons;

import hiperium.cities.commons.enums.RecordStatus;

/**
 * Represents common attributes shared across different entities, such as name, description, and status.
 * <p>
 * This record encapsulates the fundamental details that are shared across various domain objects,
 * providing a consistent structure for entities that require identification and descriptive information.
 *
 * @param name the name of the entity
 * @param description a brief description of the entity
 * @param status the status of the entity indicating its current state
 */
public record CommonAttributes(
    String name,
    String description,
    RecordStatus status
) {
}
