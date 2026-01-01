package com.mbe.ui.api.services;

import org.bukkit.entity.Player;

public interface UiPlaceholderService {
    int apiVersion();

    String process(Player player, String input);
}

