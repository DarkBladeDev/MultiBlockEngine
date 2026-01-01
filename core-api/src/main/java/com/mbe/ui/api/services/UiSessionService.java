package com.mbe.ui.api.services;

import org.bukkit.entity.Player;

import java.util.Map;

public interface UiSessionService {
    int apiVersion();

    Map<String, Object> sessionData(Player player);
}

