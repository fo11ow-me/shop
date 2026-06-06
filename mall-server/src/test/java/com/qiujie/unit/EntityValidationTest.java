package com.qiujie.unit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jakarta Bean Validation framework tests")
class EntityValidationTest {

    private static Validator validator;

    @Data
    static class TestDTO {
        @NotBlank(message = "name must not be blank")
        private String name;

        @Positive(message = "price must be positive")
        private Integer price;
    }

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("@NotBlank — empty string should cause violation")
    void shouldViolateNotBlank() {
        TestDTO dto = new TestDTO();
        dto.setName("");
        dto.setPrice(10);

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("name"));
    }

    @Test
    @DisplayName("@NotBlank — null should cause violation")
    void shouldViolateNotBlankOnNull() {
        TestDTO dto = new TestDTO();
        dto.setName(null);
        dto.setPrice(10);

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("@Positive — negative value should cause violation")
    void shouldViolatePositive() {
        TestDTO dto = new TestDTO();
        dto.setName("valid");
        dto.setPrice(-1);

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("price"));
    }

    @Test
    @DisplayName("valid DTO — no violations")
    void shouldHaveNoViolations() {
        TestDTO dto = new TestDTO();
        dto.setName("Product A");
        dto.setPrice(99);

        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
