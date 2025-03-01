package ilya.project.loggingstarter.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.regex.Pattern;

public class JsonPathCollectionValidator implements ConstraintValidator<JsonPathCollection, Collection<String>> {
    private final static Pattern JSON_PATH = Pattern.compile("([\\w$]+)(\\.[\\w$]+)*");

    @Override
    public boolean isValid(Collection<String> jsonPath, ConstraintValidatorContext context) {
        for (var value : jsonPath) {
            if (!JSON_PATH.matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }
}
