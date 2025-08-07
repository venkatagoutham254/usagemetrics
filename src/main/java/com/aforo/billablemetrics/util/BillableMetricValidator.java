package com.aforo.billablemetrics.util;

import com.aforo.billablemetrics.entity.UsageCondition;
import com.aforo.billablemetrics.enums.*;

import java.util.List;
import java.util.Optional;

public class BillableMetricValidator {

    public static void validateAll(UnitOfMeasure uom,
                                   AggregationFunction function,
                                   AggregationWindow window,
                                   List<UsageCondition> usageConditions) {

        // ✅ 1. Validate aggregation function for UOM
        if (!UnitOfMeasureValidator.getSupportedFunctions(uom).contains(function)) {
            throw new IllegalArgumentException("AggregationFunction " + function + " is not valid for UOM " + uom);
        }

        // ✅ 2. Validate aggregation window for UOM
        if (!UnitOfMeasureValidator.getSupportedWindows(uom).contains(window)) {
            throw new IllegalArgumentException("AggregationWindow " + window + " is not valid for UOM " + uom);
        }

        // ✅ 3. Validate each usage condition
        for (UsageCondition condition : usageConditions) {
            String inputDimensionName = condition.getDimension().getDimension();  // safely get enum's dimension name

            // 🔍 Try to match dimension by string
            Optional<DimensionDefinition> matchedDimensionOpt = UnitOfMeasureValidator
                    .getSupportedDimensions(uom)
                    .stream()
                    .filter(dim -> dim.getDimension().equalsIgnoreCase(inputDimensionName))
                    .findFirst();

            if (matchedDimensionOpt.isEmpty()) {
                throw new IllegalArgumentException("Dimension " + inputDimensionName + " is not valid for UOM " + uom);
            }

            DimensionDefinition matchedDimension = matchedDimensionOpt.get();

            // ✅ 4. Validate operator
            if (!matchedDimension.getValidOperators().contains(condition.getOperator())) {
                throw new IllegalArgumentException("Operator '" + condition.getOperator() + "' is not valid for dimension " + matchedDimension.getDimension());
            }

            // ✅ 5. Set the correct dimension and type
            condition.setDimension(matchedDimension); // override with correct enum
            condition.setType(matchedDimension.getType());
        }
    }
}
