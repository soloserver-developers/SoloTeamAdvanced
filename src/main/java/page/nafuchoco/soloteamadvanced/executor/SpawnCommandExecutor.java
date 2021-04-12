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

package page.nafuchoco.soloteamadvanced.executor;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloteamadvanced.SoloTeamAdvanced;
import page.nafuchoco.soloteamadvanced.data.TeamSpawnLocation;
import page.nafuchoco.soloteamadvanced.database.TeamSpawnPointTable;
import page.nafuchoco.soloteamadvanced.inventory.InventoryEventListener;
import page.nafuchoco.soloteamadvanced.inventory.MenuInventory;
import page.nafuchoco.soloteamadvanced.inventory.MenuInventoryType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public class SpawnCommandExecutor implements CommandExecutor {
    private final TeamSpawnPointTable spawnPointTable;

    public SpawnCommandExecutor(TeamSpawnPointTable spawnPointTable) {
        this.spawnPointTable = spawnPointTable;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SoloServerApi soloServerApi = SoloServerApi.getInstance();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                player.teleport(getDefaultLocation(player));
            } else if (!player.hasPermission("teamadvanced.spawn." + args[0])) {
                player.sendMessage(ChatColor.RED + "[Spawn] ");
            } else switch (args[0]) {
                case "player":
                    player.teleport(soloServerApi.getSSCPlayer(player).getSpawnLocationObject());
                    break;

                case "team": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam == null) {
                        player.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                    } else {
                        player.teleport(soloServerApi.getOfflineSSCPlayer(joinedTeam.getOwner()).getSpawnLocationObject());
                    }
                    break;
                }

                case "add": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam == null) {
                        player.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                    } else {
                        if (args.length >= 2 && args[1].length() <= 16) {
                            TeamSpawnLocation spawnLocation = new TeamSpawnLocation(
                                    UUID.randomUUID(),
                                    joinedTeam.getId(),
                                    player.getUniqueId(),
                                    args[1],
                                    player.getLocation()
                            );
                            try {
                                spawnPointTable.registerPoint(spawnLocation);
                            } catch (SQLException e) {
                                player.sendMessage(ChatColor.RED + "[Spawn] ポイントの登録に失敗しました。");
                                SoloTeamAdvanced.getInstance().getLogger().log(
                                        Level.WARNING,
                                        "An error occurred while communicating with the database.",
                                        e
                                );
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "[Spawn] ポイント名を16文字以内で入力してください。");
                        }
                    }
                    break;
                }

                case "delete": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam == null)
                        player.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                    else
                        openPointInventory(player, joinedTeam.getId(), true);
                    break;
                }

                case "list": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam == null)
                        player.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                    else
                        openPointInventory(player, joinedTeam.getId(), false);
                    break;
                }

                default: {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam == null) {
                        player.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                    } else {

                    }
                }
            }
        } else {
            Bukkit.getLogger().info("This command must be executed in-game.");
        }
        return true;
    }

    private void openPointInventory(Player player, UUID teamId, boolean delete) {
        int inventoryLineSize =
                (int) Math.ceil((Integer.parseInt(SoloServerApi.getInstance().getPluginSetting("maxSpawnPoint")) + 2) / 9);

        Inventory inventory =
                Bukkit.createInventory(null, inventoryLineSize * 9, "Spawn points");

        try {
            for (TeamSpawnLocation point : spawnPointTable.getPoints(teamId)) {
                ItemStack itemStack = new ItemStack(Material.RED_BED, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§b" + point.getPointName());
                OfflinePlayer owner = Bukkit.getOfflinePlayer(point.getPointOwner());
                itemMeta.setLore(Arrays.asList(
                        "Point Owner: " + owner.getName(),
                        "X: " + point.getSpawnLocationObject().getX(),
                        "Y: " + point.getSpawnLocationObject().getY(),
                        "Z: " + point.getSpawnLocationObject().getZ(),
                        null,
                        point.getId().toString()));
                itemStack.setItemMeta(itemMeta);
                inventory.addItem(itemStack);
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "[Spawn] ポイントの取得中にエラーが発生しました。");
            SoloTeamAdvanced.getInstance().getLogger().log(
                    Level.WARNING,
                    "An error occurred while communicating with the database.",
                    e
            );
        }

        if (!delete) {
            ItemStack itemStack = new ItemStack(Material.WHITE_BED, 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§aReturn default spawn");
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(inventoryLineSize * 9 - 1, itemStack);
        }

        player.openInventory(inventory);
        MenuInventoryType inventoryType;
        if (delete)
            inventoryType = MenuInventoryType.SPAWN_DELETE;
        else
            inventoryType = MenuInventoryType.SPAWN;
        InventoryEventListener.getOpeningMenu().add(new MenuInventory(inventoryType, inventory));
    }

    private Location getDefaultLocation(Player player) {
        SoloServerApi soloServerApi = SoloServerApi.getInstance();
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

        return location;
    }
}
