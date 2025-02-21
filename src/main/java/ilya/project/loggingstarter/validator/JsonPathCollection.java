package ilya.project.loggingstarter.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = JsonPathCollectionValidator.class)
@Target({FIELD, RECORD_COMPONENT})
public @interface JsonPathCollection {

    String message() default "Invalid json path";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
