package com.darkbladedev.engine.api.inspection;

import org.bukkit.entity.Player;

public interface InspectionRenderer {
    void render(Player player, InspectionData data, InspectionContext ctx);
}

