package com.epes.util;

import com.epes.service.ScoreInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationFormParserTest {

    @Test
    @DisplayName("Case 1: All criteria rated successfully returns complete ScoreInput map")
    void testAllRated() {
        Map<String, String> params = new HashMap<>();
        params.put("rating_101", "4");
        params.put("comment_101", "Strong execution");
        params.put("rating_102", "5");
        params.put("comment_102", "Exceptional leadership");

        List<String> errors = new ArrayList<>();
        Map<Long, ScoreInput> result = EvaluationFormParser.parse(
                params::get,
                List.of(101L, 102L),
                errors
        );

        assertTrue(errors.isEmpty(), "No errors expected when all valid");
        assertEquals(2, result.size());
        assertEquals(4, result.get(101L).getRating());
        assertEquals("Strong execution", result.get(101L).getComment());
        assertEquals(5, result.get(102L).getRating());
        assertEquals("Exceptional leadership", result.get(102L).getComment());
    }

    @Test
    @DisplayName("Case 2: Some criteria unrated - unrated criteria are simply omitted without error")
    void testSomeUnrated() {
        Map<String, String> params = new HashMap<>();
        params.put("rating_101", "3");
        params.put("comment_101", "Met targets");
        // criterion 102 is unrated (missing from params)
        params.put("rating_103", ""); // empty rating string

        List<String> errors = new ArrayList<>();
        Map<Long, ScoreInput> result = EvaluationFormParser.parse(
                params::get,
                List.of(101L, 102L, 103L),
                errors
        );

        assertTrue(errors.isEmpty(), "Unrated criteria should not produce parse errors");
        assertEquals(1, result.size());
        assertTrue(result.containsKey(101L));
        assertEquals(3, result.get(101L).getRating());
        assertFalse(result.containsKey(102L));
        assertFalse(result.containsKey(103L));
    }

    @Test
    @DisplayName("Case 3: Invalid rating text produces friendly error message")
    void testInvalidRatingText() {
        Map<String, String> params = new HashMap<>();
        params.put("rating_101", "high"); // non-numeric
        params.put("rating_102", "7"); // out of 1..5 range
        params.put("rating_103", "0"); // out of 1..5 range

        List<String> errors = new ArrayList<>();
        Map<Long, ScoreInput> result = EvaluationFormParser.parse(
                params::get,
                List.of(101L, 102L, 103L),
                errors
        );

        assertEquals(0, result.size());
        assertEquals(3, errors.size());
        assertTrue(errors.get(0).contains("Invalid rating text for criterion 101"));
        assertTrue(errors.get(1).contains("between 1 and 5"));
        assertTrue(errors.get(2).contains("between 1 and 5"));
    }

    @Test
    @DisplayName("Case 4: Missing criterion ID in parameter map adds friendly error without throwing")
    void testMissingCriterionId() {
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put("rating_", new String[]{"4"}); // missing id
        paramMap.put("rating_abc", new String[]{"5"}); // invalid non-numeric id
        paramMap.put("rating_200", new String[]{"3"}); // valid id

        List<String> errors = new ArrayList<>();
        Map<Long, ScoreInput> result = EvaluationFormParser.parseFromMap(
                paramMap,
                List.of(200L),
                errors
        );

        assertEquals(1, result.size());
        assertEquals(3, result.get(200L).getRating());
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Missing criterion ID")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Invalid criterion ID")));
    }
}
