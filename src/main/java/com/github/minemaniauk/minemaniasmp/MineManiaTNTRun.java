/*
 * MineManiaTNTRun
 * Used for interacting with the database and message broker.
 * Copyright (C) 2023  MineManiaUK Staff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.minemaniauk.minemaniasmp;

import com.github.cozyplugins.cozylibrary.CozyPlugin;
import com.github.cozyplugins.cozylibrary.command.command.command.ProgrammableCommand;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import com.github.minemaniauk.minemaniasmp.configuration.ArenaConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the main class.
 */
public final class MineManiaTNTRun extends CozyPlugin {

    private static @NotNull MineManiaTNTRun instance;

    private @NotNull ArenaConfiguration arenaConfiguration;

    @Override
    public boolean enableCommandDirectory() {
        return true;
    }

    @Override
    public void onCozyEnable() {

        // Initialize this instance.
        MineManiaTNTRun.instance = this;

        // Add configuration.
        this.arenaConfiguration = new ArenaConfiguration();

        // Add commands.
        this.addCommand(new ProgrammableCommand("tntrun")
                .setDescription("Contains tnt run commands.")
        );
    }

    /**
     * Used to get the instance of the arena configuration.
     *
     * @return The instance of the arena configuration.
     */
    public @NotNull ArenaConfiguration getArenaConfiguration() {
        return this.arenaConfiguration;
    }

    /**
     * Used to get the instance of this plugin.
     *
     * @return The instance of this plugin.
     */
    public static @NotNull MineManiaTNTRun getInstance() {
        return MineManiaTNTRun.instance;
    }

    /**
     * Used to get the instance of the mine mania api.
     *
     * @return The instance of the mine mania api.
     */
    public static @NotNull MineManiaAPI getAPI() {
        return MineManiaAPI_Bukkit.getInstance().getAPI();
    }
}
