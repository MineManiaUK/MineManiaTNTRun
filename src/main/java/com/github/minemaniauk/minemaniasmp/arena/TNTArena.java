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

package com.github.minemaniauk.minemaniasmp.arena;

import com.github.cozyplugins.cozylibrary.indicator.LocationConvertable;
import com.github.cozyplugins.cozylibrary.indicator.Savable;
import com.github.cozyplugins.cozylibrary.location.Region3D;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.game.GameType;
import com.github.minemaniauk.minemaniasmp.MineManiaTNTRun;
import com.github.smuddgge.squishyconfiguration.indicator.ConfigurationConvertable;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.github.smuddgge.squishyconfiguration.memory.MemoryConfigurationSection;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Represents a tnt arena.
 */
public class TNTArena extends Arena implements ConfigurationConvertable<TNTArena>, Savable, LocationConvertable {

    private @Nullable Region3D region;
    private @Nullable Location spawnPoint;

    /**
     * Used to create a new instance of a tnt run arena.
     */
    public TNTArena(@NotNull UUID identifier) {
        super(identifier, MineManiaTNTRun.getAPI().getServerName(), GameType.TNT_RUN);
    }

    /**
     * Used to get the region of the arena.
     *
     * @return The arena region.
     */
    public @NotNull Region3D getRegion() {

        // Check if the region is null.
        if (this.region == null) {
            throw new RuntimeException("Region is null for arena with identifier " + this.getIdentifier());
        }

        return this.region;
    }

    /**
     * Used to get the spawn point of the arena.
     *
     * @return The spawn point.
     */
    public @NotNull Location getSpawnPoint() {

        // Check if the spawn point is null.
        if (this.region == null) {
            throw new RuntimeException("Spawn point is null for arena with identifier " + this.getIdentifier());
        }

        return this.spawnPoint;
    }

    /**
     * Used to set the region of the arena.
     *
     * @param region The region to set this arena to.
     * @return This instance.
     */
    public @NotNull TNTArena setRegion(@NotNull Region3D region) {
        this.region = region;
        return this;
    }

    /**
     * Used to set the location of the spawn point.
     *
     * @param spawnPoint The location of the spawn point.
     * @return The instance.
     */
    public @NotNull TNTArena setSpawnPoint(@NotNull Location spawnPoint) {
        this.spawnPoint = spawnPoint;
        return this;
    }

    @Override
    public void activate() {
        this.save();
    }

    @Override
    public void deactivate() {
        this.save();
    }

    @Override
    public @NotNull ConfigurationSection convert() {
        ConfigurationSection section = new MemoryConfigurationSection(new LinkedHashMap<>());

        section.set("serverName", this.getServerName());
        section.set("gameType", this.getGameType().name());
        section.set("gameRoomIdentifier", this.getGameRoomIdentifier());
        section.set("minPlayers", this.getMinPlayers());
        section.set("maxPlayers", this.getMaxPlayers());

        section.set("region", this.getRegion().convert());
        section.set("spawn_point", this.convertLocation(this.spawnPoint));

        return section;
    }

    @Override
    public @NotNull TNTArena convert(@NotNull ConfigurationSection section) {

        this.setGameRoomIdentifier(UUID.fromString(section.getString("gameRoomIdentifier")));
        this.setMinPlayers(section.getInteger("minPlayers"));
        this.setMaxPlayers(section.getInteger("maxPlayers"));

        this.setRegion(new Region3D(section));
        this.setSpawnPoint(this.convertLocation(section.getSection("spawn_point")));
        return this;
    }

    @Override
    public void save() {

        // Save the api.
        super.save();

        // Save to local storage.
        MineManiaTNTRun.getInstance().getArenaConfiguration()
                .insertType(this.getIdentifier().toString(), this);
    }
}
