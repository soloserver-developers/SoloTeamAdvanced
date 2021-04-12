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

package page.nafuchoco.soloteamadvanced.database;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.database.DatabaseConnector;
import page.nafuchoco.soloservercore.database.DatabaseTable;
import page.nafuchoco.soloteamadvanced.data.TeamSpawnLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamSpawnPointTable extends DatabaseTable {
    private static final Gson gson = new Gson();

    public TeamSpawnPointTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, team_id VARCHAR(36) NOT NULL, " +
                "point_owner VARCHAR(36) NOT NULL, point_name VARCHAR(16) NOT NULL, " +
                "point_location TEXT NOT NULL");
    }

    public TeamSpawnLocation getPoint(@NotNull UUID pointId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, pointId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("id"));
                    UUID teamId = UUID.fromString(resultSet.getString("team_id"));
                    UUID ownerId = UUID.fromString(resultSet.getString("point_owner"));
                    String name = resultSet.getString("point_name");
                    String location = resultSet.getString("point_location");
                    TeamSpawnLocation spawnLocation = new TeamSpawnLocation(id, teamId, ownerId, name, location);
                    return spawnLocation;
                }
            }
        }
        return null;
    }

    public List<TeamSpawnLocation> getPoints(@NotNull UUID teamId) throws SQLException {
        List<TeamSpawnLocation> points = new ArrayList<>();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE team_id = ?"
             )) {
            ps.setString(1, teamId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("id"));
                    UUID ownerId = UUID.fromString(resultSet.getString("point_owner"));
                    String name = resultSet.getString("point_name");
                    String location = resultSet.getString("point_location");
                    TeamSpawnLocation spawnLocation = new TeamSpawnLocation(id, teamId, ownerId, name, location);
                    points.add(spawnLocation);
                }
            }
        }
        return points;
    }

    public void registerPoint(@NotNull TeamSpawnLocation spawnLocation) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " VALUES (?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, spawnLocation.getId().toString());
            ps.setString(2, spawnLocation.getTeamId().toString());
            ps.setString(3, spawnLocation.getPointOwner().toString());
            ps.setString(4, spawnLocation.getPointName());
            ps.setString(5, spawnLocation.getSpawnLocation());
            ps.execute();
        }
    }

    public void deletePoint(@NotNull UUID pointId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, pointId.toString());
            ps.execute();
        }
    }
}
