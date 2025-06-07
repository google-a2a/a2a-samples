package com.google.a2a.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Utility class for Bean Validation operations.
 */
public final class ValidationUtils {
    
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();
    
    private ValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates an object using Bean Validation and throws IllegalArgumentException if violations exist.
     * 
     * @param object the object to validate
     * @param <T> the type of object being validated
     * @throws IllegalArgumentException if validation fails
     * @return the validated object
     */
    public static <T> T validateAndThrow(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation failed: ");
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                  .append(" ")
                  .append(violation.getMessage())
                  .append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }
        
        return object;
    }
    
    /**
     * Validates an object and returns the set of constraint violations.
     * 
     * @param object the object to validate
     * @param <T> the type of object being validated
     * @return set of constraint violations (empty if valid)
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return VALIDATOR.validate(object);
    }
    
    /**
     * Checks if an object is valid (has no constraint violations).
     * 
     * @param object the object to validate
     * @param <T> the type of object being validated
     * @return true if valid, false otherwise
     */
    public static <T> boolean isValid(T object) {
        return VALIDATOR.validate(object).isEmpty();
    }
} 