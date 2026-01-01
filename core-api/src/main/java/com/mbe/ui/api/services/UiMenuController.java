package com.mbe.ui.api.services;

import org.bukkit.entity.Player;

import java.util.Map;

public interface UiMenuController {
    int apiVersion();

    void openMenu(Player player, String menuId, Map<String, Object> variables);

    void refresh(Player player);

    void close(Player player);
}

