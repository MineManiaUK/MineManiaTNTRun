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

package com.github.minemaniauk.minemaniatntrun.configuration;

import com.github.cozyplugins.cozylibrary.configuration.SingleTypeConfigurationDirectory;
import com.github.minemaniauk.minemaniatntrun.MineManiaTNTRun;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import net.royawesome.jlibnoise.module.combiner.Min;
import org.bukkit.block.data.type.TNT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents an arena configuration directory.
 */
public class ArenaConfiguration extends SingleTypeConfigurationDirectory<TNTArena> {

    /**
     * Used to create an arena configuration directory.
     */
    public ArenaConfiguration() {
        super("arenas", MineManiaTNTRun.class);
    }

    @Override
    public @Nullable String getDefaultFileName() {
        return "arenas.yml";
    }

    @Override
    protected void onReload() {

    }

    @Override
    public @NotNull TNTArena createEmpty(@NotNull String identifier) {
        return new TNTArena(UUID.fromString(identifier));
    }

    /**
     * Used to update arenas that are registered.
     *
     * @return This instance.
     */
    public @NotNull ArenaConfiguration reloadRegisteredArenas() {
        MineManiaTNTRun.getAPI().getGameManager().unregisterLocalArenas();

        for (TNTArena arena : this.getAllTypes()) {
            MineManiaTNTRun.getAPI().getGameManager().registerArena(arena);
        }

        return this;
    }
}
