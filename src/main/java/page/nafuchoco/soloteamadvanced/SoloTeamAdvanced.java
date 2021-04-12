/*
 * Copyright 2021 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.soloteamadvanced;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import page.nafuchoco.soloservercore.database.DatabaseConnector;
import page.nafuchoco.soloteamadvanced.database.TeamSpawnPointTable;
import page.nafuchoco.soloteamadvanced.executor.SpawnCommandExecutor;
import page.nafuchoco.soloteamadvanced.inventory.InventoryEventListener;

import java.sql.SQLException;
import java.util.logging.Level;

public final class SoloTeamAdvanced extends JavaPlugin {
    private static SoloTeamAdvanced instance;
    private static SoloTeamAdvancedConfig config;

    private static DatabaseConnector connector;
    private static TeamSpawnPointTable spawnPointTable;

    public static SoloTeamAdvanced getInstance() {
        if (instance == null)
            instance = (SoloTeamAdvanced) Bukkit.getServer().getPluginManager().getPlugin("SoloTeamAdvanced");
        return instance;
    }

    public static SoloTeamAdvancedConfig getSoloTeamAdvancedConfig() {
        if (config == null)
            config = new SoloTeamAdvancedConfig();
        return config;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getSoloTeamAdvancedConfig().reloadConfig();

        // Database Init
        connector = new DatabaseConnector(getSoloTeamAdvancedConfig().getInitConfig().getDatabaseType(),
                getSoloTeamAdvancedConfig().getInitConfig().getAddress() + ":"
                        + getSoloTeamAdvancedConfig().getInitConfig().getPort(),
                getSoloTeamAdvancedConfig().getInitConfig().getDatabase(),
                getSoloTeamAdvancedConfig().getInitConfig().getUsername(),
                getSoloTeamAdvancedConfig().getInitConfig().getPassword());
        spawnPointTable = new TeamSpawnPointTable("teamspawn", connector);
        try {
            spawnPointTable.createTable();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "", e);
        }

        getCommand("tspawn").setExecutor(new SpawnCommandExecutor(spawnPointTable));

        getServer().getPluginManager().registerEvents(new InventoryEventListener(spawnPointTable), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (connector != null)
            connector.close();
    }
}
