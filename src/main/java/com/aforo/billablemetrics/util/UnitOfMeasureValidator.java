package com.aforo.billablemetrics.util;

import com.aforo.billablemetrics.enums.*;

import java.util.*;
import java.util.stream.Collectors;

public class UnitOfMeasureValidator {

    // ✅ FINAL MAPPING: ProductType -> UnitOfMeasure
    private static final Map<String, Set<UnitOfMeasure>> PRODUCT_TYPE_UOM_MAP = Map.of(
        "API", Set.of(
            UnitOfMeasure.API_CALL,
            UnitOfMeasure.REQUEST,
            UnitOfMeasure.TRANSACTION,
            UnitOfMeasure.HIT
        ),
        "LLM", Set.of(
            UnitOfMeasure.TOKEN,
            UnitOfMeasure.COMPLETION_TOKEN,
            UnitOfMeasure.PROMPT_TOKEN
        ),
        "FLATFILE", Set.of(
            UnitOfMeasure.FILE,
            UnitOfMeasure.DELIVERY,
            UnitOfMeasure.MB,
            UnitOfMeasure.RECORD,
            UnitOfMeasure.ROW
        ),
        "SQL", Set.of(
            UnitOfMeasure.ROW,
            UnitOfMeasure.QUERY_EXECUTION,
            UnitOfMeasure.CELL,
            UnitOfMeasure.MB
        )
    );

    // ✅ Aggregation Functions by UOM
    private static final Map<UnitOfMeasure, Set<AggregationFunction>> UOM_FUNCTIONS = Map.ofEntries(
        Map.entry(UnitOfMeasure.API_CALL, Set.of(AggregationFunction.COUNT)),
        Map.entry(UnitOfMeasure.HIT, Set.of(AggregationFunction.COUNT)),
        Map.entry(UnitOfMeasure.TRANSACTION, Set.of(AggregationFunction.COUNT, AggregationFunction.DISTINCT_COUNT)),
        Map.entry(UnitOfMeasure.REQUEST, Set.of(AggregationFunction.COUNT)),
        Map.entry(UnitOfMeasure.TOKEN, Set.of(AggregationFunction.SUM, AggregationFunction.AVG, AggregationFunction.MAX)),
        Map.entry(UnitOfMeasure.COMPLETION_TOKEN, Set.of(AggregationFunction.SUM, AggregationFunction.AVG)),
        Map.entry(UnitOfMeasure.PROMPT_TOKEN, Set.of(AggregationFunction.SUM, AggregationFunction.AVG)),
        Map.entry(UnitOfMeasure.FILE, Set.of(AggregationFunction.COUNT)),
        Map.entry(UnitOfMeasure.DELIVERY, Set.of(AggregationFunction.COUNT)),
        Map.entry(UnitOfMeasure.MB, Set.of(AggregationFunction.SUM, AggregationFunction.AVG, AggregationFunction.MAX)),
        Map.entry(UnitOfMeasure.RECORD, Set.of(AggregationFunction.COUNT, AggregationFunction.SUM)),
        Map.entry(UnitOfMeasure.ROW, Set.of(AggregationFunction.COUNT, AggregationFunction.SUM, AggregationFunction.AVG, AggregationFunction.MAX)),
        Map.entry(UnitOfMeasure.QUERY_EXECUTION, Set.of(AggregationFunction.COUNT, AggregationFunction.AVG, AggregationFunction.MAX)),
        Map.entry(UnitOfMeasure.CELL, Set.of(AggregationFunction.SUM, AggregationFunction.AVG))
    );

    // ✅ Aggregation Windows by UOM
    private static final Map<UnitOfMeasure, Set<AggregationWindow>> UOM_WINDOWS = Map.ofEntries(
        Map.entry(UnitOfMeasure.API_CALL, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_HOUR, AggregationWindow.PER_EVENT, AggregationWindow.PER_MINUTE)),
        Map.entry(UnitOfMeasure.HIT, Set.of(AggregationWindow.PER_EVENT, AggregationWindow.PER_HOUR, AggregationWindow.PER_MINUTE)),
        Map.entry(UnitOfMeasure.TRANSACTION, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_HOUR, AggregationWindow.PER_EVENT)),
        Map.entry(UnitOfMeasure.REQUEST, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_HOUR, AggregationWindow.PER_EVENT, AggregationWindow.PER_MINUTE)),
        Map.entry(UnitOfMeasure.TOKEN, Set.of(AggregationWindow.PER_EVENT, AggregationWindow.PER_MINUTE, AggregationWindow.PER_HOUR, AggregationWindow.PER_DAY)),
        Map.entry(UnitOfMeasure.COMPLETION_TOKEN, Set.of(AggregationWindow.PER_EVENT, AggregationWindow.PER_HOUR, AggregationWindow.PER_DAY)),
        Map.entry(UnitOfMeasure.PROMPT_TOKEN, Set.of(AggregationWindow.PER_EVENT, AggregationWindow.PER_HOUR, AggregationWindow.PER_DAY)),
        Map.entry(UnitOfMeasure.FILE, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_WEEK, AggregationWindow.PER_MONTH, AggregationWindow.PER_EVENT)),
        Map.entry(UnitOfMeasure.DELIVERY, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_WEEK, AggregationWindow.PER_MONTH)),
        Map.entry(UnitOfMeasure.MB, Set.of(AggregationWindow.PER_MONTH, AggregationWindow.PER_DAY, AggregationWindow.PER_QUERY, AggregationWindow.PER_DELIVERY)),
        Map.entry(UnitOfMeasure.RECORD, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_FILE, AggregationWindow.PER_DELIVERY)),
        Map.entry(UnitOfMeasure.ROW, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_FILE, AggregationWindow.PER_MONTH, AggregationWindow.PER_HOUR, AggregationWindow.PER_QUERY)),
        Map.entry(UnitOfMeasure.QUERY_EXECUTION, Set.of(AggregationWindow.PER_DAY, AggregationWindow.PER_HOUR, AggregationWindow.PER_MONTH)),
        Map.entry(UnitOfMeasure.CELL, Set.of(AggregationWindow.PER_MONTH, AggregationWindow.PER_DAY, AggregationWindow.PER_QUERY))
    );

    // ✅ UOM allowed for product type
    public static boolean isValidUOMForProductType(String productType, UnitOfMeasure uom) {
        Set<UnitOfMeasure> allowedUOMs = PRODUCT_TYPE_UOM_MAP.get(productType.toUpperCase());
        return allowedUOMs != null && allowedUOMs.contains(uom);
    }

    // ✅ Valid dimensions for a UOM
    public static Set<DimensionDefinition> getSupportedDimensions(UnitOfMeasure uom) {
        return Arrays.stream(DimensionDefinition.values())
        .filter(dim -> normalizeUOM(dim.getUom()).equalsIgnoreCase(normalizeUOM(uom.name())))
        .collect(Collectors.toSet());
    }
    private static String normalizeUOM(String rawUOM) {
        return switch (rawUOM.toUpperCase()) {
            case "TOKEN", "PROMPT_TOKEN", "COMPLETION_TOKEN" -> "TOKEN";
            case "MB", "CELL" -> "MB";
            default -> rawUOM.toUpperCase();
        };
    }
    
    // ✅ Valid operators for a dimension
    public static boolean isValidOperatorForDimension(DimensionDefinition dimension, String operator) {
        return dimension.getValidOperators().contains(operator);
    }

    // ✅ Valid functions for a UOM
    public static Set<AggregationFunction> getSupportedFunctions(UnitOfMeasure uom) {
        return UOM_FUNCTIONS.getOrDefault(uom, Set.of());
    }

    public static boolean isValidAggregationFunction(UnitOfMeasure uom, AggregationFunction function) {
        return UOM_FUNCTIONS.getOrDefault(uom, Set.of()).contains(function);
    }

    // ✅ Valid windows for a UOM
    public static Set<AggregationWindow> getSupportedWindows(UnitOfMeasure uom) {
        return UOM_WINDOWS.getOrDefault(uom, Set.of());
    }

    public static boolean isValidAggregationWindow(UnitOfMeasure uom, AggregationWindow window) {
        return UOM_WINDOWS.getOrDefault(uom, Set.of()).contains(window);
    }
}
