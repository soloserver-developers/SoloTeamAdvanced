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

package page.nafuchoco.soloteamadvanced.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public class TeamSpawnLocation {
    private final UUID id;
    private final UUID teamId;
    private final UUID pointOwner;
    private final String pointName;
    private final String spawnLocation;

    public TeamSpawnLocation(@NotNull UUID id, @NotNull UUID teamId, @NotNull UUID pointOwner, @NotNull String pointName, @NotNull String spawnLocation) {
        this.id = id;
        this.teamId = teamId;
        this.pointOwner = pointOwner;
        this.pointName = pointName;
        this.spawnLocation = spawnLocation;
    }

    public TeamSpawnLocation(@NotNull UUID id, @NotNull UUID teamId, @NotNull UUID pointOwner, @NotNull String pointName, @NotNull Location spawnLocation) {
        this.id = id;
        this.teamId = teamId;
        this.pointOwner = pointOwner;
        this.pointName = pointName;

        JsonObject locationJson = new JsonObject();
        locationJson.addProperty("World", spawnLocation.getWorld().getName());
        locationJson.addProperty("X", spawnLocation.getBlockX());
        locationJson.addProperty("Y", spawnLocation.getBlockY());
        locationJson.addProperty("Z", spawnLocation.getBlockZ());
        this.spawnLocation = new Gson().toJson(locationJson);
    }

    public Location getSpawnLocationObject() {
        JsonObject locationJson = new Gson().fromJson(spawnLocation, JsonObject.class);
        String world = locationJson.get("World").getAsString();
        double x = locationJson.get("X").getAsDouble();
        double y = locationJson.get("Y").getAsDouble();
        double z = locationJson.get("Z").getAsDouble();
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
