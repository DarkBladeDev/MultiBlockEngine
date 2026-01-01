package com.mbe.ui.api.services.ux;

import com.darkbladedev.engine.model.MultiblockInstance;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public interface UiUxMenusService {
    int apiVersion();

    Path menusDir();

    void reload();

    Optional<String> saveMenuYaml(String fileName, String yamlText);

    void saveMenuYamlAsync(String fileName, String yamlText);

    void open(Player player, String menuId, Map<String, Object> variables, Optional<MultiblockInstance> multiblock);
}

