package com.darkbladedev.engine.api.inspection;

public record InspectionEntry(
    String key,
    Object value,
    EntryType type,
    InspectionLevel visibility
) {
}

