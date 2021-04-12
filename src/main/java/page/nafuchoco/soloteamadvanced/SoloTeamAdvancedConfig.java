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

import lombok.AllArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import page.nafuchoco.soloservercore.SoloServerCoreConfig;

public class SoloTeamAdvancedConfig {
    private InitConfig initConfig;

    public void reloadConfig() {
        SoloTeamAdvanced instance = SoloTeamAdvanced.getInstance();
        instance.reloadConfig();
        FileConfiguration config = instance.getConfig();

        if (initConfig == null) {
            SoloServerCoreConfig.DatabaseType databaseType = SoloServerCoreConfig.DatabaseType.valueOf(config.getString("database.type"));
            String address = config.getString("database.address");
            int port = config.getInt("database.port", 3306);
            String database = config.getString("database.database");
            String username = config.getString("database.username");
            String password = config.getString("database.password");
            String tablePrefix = config.getString("database.tablePrefix");
            initConfig = new InitConfig(databaseType, address, port, database, username, password, tablePrefix);
        }
    }

    public InitConfig getInitConfig() {
        return initConfig;
    }

    @AllArgsConstructor
    public static class InitConfig {
        private SoloServerCoreConfig.DatabaseType databaseType;
        private String address;
        private int port;
        private String database;
        private String username;
        private String password;
        private String tablePrefix;

        public SoloServerCoreConfig.DatabaseType getDatabaseType() {
            return databaseType;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getTablePrefix() {
            return tablePrefix;
        }
    }
}
