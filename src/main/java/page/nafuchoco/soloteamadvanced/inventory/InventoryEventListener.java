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

package page.nafuchoco.soloteamadvanced.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloteamadvanced.SoloTeamAdvanced;
import page.nafuchoco.soloteamadvanced.data.TeamSpawnLocation;
import page.nafuchoco.soloteamadvanced.database.TeamSpawnPointTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryEventListener implements Listener {
    private static final List<MenuInventory> openingMenu = new ArrayList<>();
    private final TeamSpawnPointTable spawnPointTable;

    public static List<MenuInventory> getOpeningMenu() {
        return openingMenu;
    }


    public InventoryEventListener(TeamSpawnPointTable spawnPointTable) {
        this.spawnPointTable = spawnPointTable;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (openingMenu.stream().anyMatch(i -> i.getInventory().equals(event.getInventory()))) {
            MenuInventory menu =
                    openingMenu.stream().filter(i -> i.getInventory().equals(event.getInventory())).findFirst().get();

            if (event.getCurrentItem() != null || event.getCurrentItem().getType() != Material.AIR) {
                try {
                    switch (menu.getInventoryType()) {
                        case SPAWN:
                            if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§aReturn default spawn")) {
                                SoloServerApi soloServerApi = SoloServerApi.getInstance();
                                Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
                                Location location;

                                PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                                if (joinedTeam == null) {
                                    location = soloServerApi.getSSCPlayer(player).getSpawnLocationObject();
                                } else {
                                    if (Boolean.parseBoolean(soloServerApi.getPluginSetting("teamSpawnCollect")))
                                        location = soloServerApi.getOfflineSSCPlayer(joinedTeam.getOwner()).getSpawnLocationObject();
                                    else
                                        location = soloServerApi.getSSCPlayer(player).getSpawnLocationObject();
                                }
                                player.teleport(location);
                            } else {
                                TeamSpawnLocation spawnLocation;
                                List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                                UUID pointId = UUID.fromString(lore.get(lore.size() - 1));
                                spawnLocation = spawnPointTable.getPoint(pointId);
                                event.getWhoClicked().teleport(spawnLocation.getSpawnLocationObject());
                            }
                            break;
                    }
                } catch (SQLException e) {
                    SoloTeamAdvanced.getInstance().getLogger().log(
                            Level.WARNING,
                            "An error occurred while communicating with the database.",
                            e
                    );
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent event) {
        if (openingMenu.stream().anyMatch(i -> i.getInventory().equals(event.getInventory())))
            event.setCancelled(true);
    }
}
