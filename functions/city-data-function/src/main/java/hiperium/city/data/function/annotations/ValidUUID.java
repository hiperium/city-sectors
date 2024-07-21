package hiperium.city.data.function.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a custom annotation for validating UUID values.
 *
 * <p>
 * The {@code ValidUUID} annotation can be used to annotate fields or parameters that should be validated
 * to ensure they contain valid UUID values. It is applied at runtime and can be used with other validation
 * annotations to perform multi-field validation.
 * </p>
 *
 * <p>
 * The annotation is marked as a constraint, and it uses the {@code Pattern} constraint to specify the regular
 * expression for validating the UUID value. The regular expression used is "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".
 * </p>
 *
 * <p>
 * The {@code ValidUUID} annotation supports customizing the error message by using the {@code message} attribute.
 * Other attributes such as {@code groups} and {@code payload} can be used to specify groupings and payloads for
 * the validation process, but they are not used by the annotation implementation itself.
 * </p>
 *
 * <p>
 * Here is an example usage of the {@code ValidUUID} annotation:
 * </p>
 *
 * <pre>{@code
 * public class User {
 *
 *     &#64;ValidUUID
 *     private String id;
 *
 *     // ...
 *
 * }
 * }</pre>
 *
 * @see Constraint
 * @see Pattern
 * @see ReportAsSingleViolation
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = {}) // no additional validator needed
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
public @interface ValidUUID {

    /**
     * Retrieves the error message to be displayed when a UUID validation fails.
     *
     * @return The error message for UUID validation failure.
     */
    String message() default "Invalid UUID";

    /**
     * Returns an array of classes representing the groups targeted for validation.
     *
     * <p>
     * This method returns an array of classes that represents the groups targeted for validation. The validation
     * groups are used to define a subset of constraints that should be evaluated during validation. By using
     * groups, you can selectively validate certain constraints based on different scenarios or business rules.
     * </p>
     *
     * <p>
     * The default behavior of this method is to return an empty array, indicating that no specific groups are
     * targeted for validation. However, subclasses or implementing classes may override this behavior and provide
     * custom logic to determine the targeted groups.
     * </p>
     *
     * @return an array of classes representing the groups targeted for validation
     *
     * @see ValidUUID
     * @since 1.0.0
     */
    Class<?>[] groups() default {};

    /**
     * Returns an array of classes that represent the custom payload types associated with this method.
     * The default value is an empty array.
     *
     * @return an array of classes representing the custom payload types
     */
    Class<? extends Payload>[] payload() default {};
}
