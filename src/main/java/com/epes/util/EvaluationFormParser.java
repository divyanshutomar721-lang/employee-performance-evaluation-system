package com.epes.util;

import com.epes.service.ScoreInput;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Pure parser for converting HTTP evaluation form parameters (rating_&lt;id&gt; and comment_&lt;id&gt;)
 * into a typed map of criterion IDs to {@link ScoreInput} instances.
 */
public final class EvaluationFormParser {

    private static final String RATING_PREFIX = "rating_";
    private static final String COMMENT_PREFIX = "comment_";

    private EvaluationFormParser() {}

    /**
     * Parses ratings and comments for a given set of active criterion IDs using a parameter getter function.
     *
     * @param paramGetter function returning parameter value by name
     * @param activeCriterionIds collection of valid active criterion IDs
     * @param errors collector for parsing or validation error messages
     * @return map of criterion ID to ScoreInput for all rated criteria
     */
    public static Map<Long, ScoreInput> parse(Function<String, String> paramGetter,
                                              Collection<Long> activeCriterionIds,
                                              List<String> errors) {
        if (paramGetter == null || activeCriterionIds == null) {
            return Collections.emptyMap();
        }

        Map<Long, ScoreInput> result = new HashMap<>();
        for (Long criterionId : activeCriterionIds) {
            if (criterionId == null) {
                errors.add("Missing criterion ID");
                continue;
            }
            String ratingStr = paramGetter.apply(RATING_PREFIX + criterionId);
            String comment = paramGetter.apply(COMMENT_PREFIX + criterionId);

            if (ratingStr == null || ratingStr.isBlank()) {
                // Unrated criterion - simply omitted
                continue;
            }

            try {
                int rating = Integer.parseInt(ratingStr.trim());
                if (rating < 1 || rating > 5) {
                    errors.add("Rating for criterion " + criterionId + " must be between 1 and 5 (got: " + rating + ")");
                } else {
                    String cleanComment = (comment != null && !comment.isBlank()) ? comment.trim() : null;
                    result.put(criterionId, new ScoreInput(rating, cleanComment));
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid rating text for criterion " + criterionId + ": '" + ratingStr + "'");
            }
        }
        return result;
    }

    /**
     * Parses parameter map from request keys, validating criterion IDs and ratings.
     *
     * @param parameterMap map of parameter names to string values or arrays
     * @param activeCriterionIds collection of allowed active criterion IDs (if null, allows any valid long ID)
     * @param errors collector for error messages
     * @return map of criterion ID to ScoreInput
     */
    public static Map<Long, ScoreInput> parseFromMap(Map<String, String[]> parameterMap,
                                                     Collection<Long> activeCriterionIds,
                                                     List<String> errors) {
        if (parameterMap == null) {
            return Collections.emptyMap();
        }

        Set<Long> allowedIds = (activeCriterionIds != null) ? Set.copyOf(activeCriterionIds) : null;
        Map<Long, ScoreInput> result = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            if (paramName != null && paramName.startsWith(RATING_PREFIX)) {
                String idPart = paramName.substring(RATING_PREFIX.length()).trim();
                if (idPart.isEmpty()) {
                    errors.add("Missing criterion ID in parameter '" + paramName + "'");
                    continue;
                }

                Long criterionId;
                try {
                    criterionId = Long.parseLong(idPart);
                } catch (NumberFormatException e) {
                    errors.add("Invalid criterion ID in parameter: " + idPart);
                    continue;
                }

                if (allowedIds != null && !allowedIds.contains(criterionId)) {
                    errors.add("Criterion ID " + criterionId + " is not an active criterion");
                    continue;
                }

                String[] values = entry.getValue();
                String ratingStr = (values != null && values.length > 0) ? values[0] : null;
                if (ratingStr == null || ratingStr.isBlank()) {
                    continue;
                }

                try {
                    int rating = Integer.parseInt(ratingStr.trim());
                    if (rating < 1 || rating > 5) {
                        errors.add("Rating for criterion " + criterionId + " must be between 1 and 5 (got: " + rating + ")");
                    } else {
                        String[] commentVals = parameterMap.get(COMMENT_PREFIX + criterionId);
                        String comment = (commentVals != null && commentVals.length > 0 && !commentVals[0].isBlank())
                                ? commentVals[0].trim() : null;
                        result.put(criterionId, new ScoreInput(rating, comment));
                    }
                } catch (NumberFormatException e) {
                    errors.add("Invalid rating text for criterion " + criterionId + ": '" + ratingStr + "'");
                }
            }
        }
        return result;
    }
}
