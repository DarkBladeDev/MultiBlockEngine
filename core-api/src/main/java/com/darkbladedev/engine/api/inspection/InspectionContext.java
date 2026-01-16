package com.darkbladedev.engine.api.inspection;

import org.bukkit.entity.Player;

public record InspectionContext(
    Player player,
    InspectionLevel requestedLevel,
    InteractionSource source
) {
}

