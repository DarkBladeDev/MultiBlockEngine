package com.darkbladedev.engine.api.inspection;

import java.util.Map;

public record InspectionData(
    Map<String, InspectionEntry> entries
) {
}

