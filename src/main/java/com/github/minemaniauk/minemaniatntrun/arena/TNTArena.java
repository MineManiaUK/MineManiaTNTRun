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

package com.github.minemaniauk.minemaniatntrun.arena;

import com.github.cozyplugins.cozylibrary.indicator.LocationConvertable;
import com.github.cozyplugins.cozylibrary.indicator.Savable;
import com.github.cozyplugins.cozylibrary.item.CozyItem;
import com.github.cozyplugins.cozylibrary.location.Region3D;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.game.GameType;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.bukkitapi.BukkitLocationConverter;
import com.github.minemaniauk.minemaniatntrun.MineManiaTNTRun;
import com.github.minemaniauk.minemaniatntrun.WorldEditUtility;
import com.github.minemaniauk.minemaniatntrun.session.TNTSession;
import com.github.smuddgge.squishyconfiguration.indicator.ConfigurationConvertable;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.github.smuddgge.squishyconfiguration.memory.MemoryConfigurationSection;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private @Nullable String schematic;

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
        if (this.spawnPoint == null) {
            throw new RuntimeException("Spawn point is null for arena with identifier " + this.getIdentifier());
        }

        return this.spawnPoint;
    }

    /**
     * Used to get the arenas schematic.
     * This schematic will be pasted before teleporting
     * the players.
     *
     * @return The schematic identifier.
     */
    public @NotNull String getSchematic() {

        // Check if the spawn point is null.
        if (this.schematic == null) {
            throw new RuntimeException("Schematic is null for arena with identifier " + this.getIdentifier());
        }

        return this.schematic;
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

    /**
     * Used to set the schematic that should be used.
     *
     * @param schematic The schematic identifier without extensions.
     * @return This instance.
     */
    public @NotNull TNTArena setSchematic(@NotNull String schematic) {
        this.schematic = schematic;
        if (schematic.equalsIgnoreCase("gold_mine")) {
            this.setMapName("Gold Mine");
            this.setDisplayItemSection(new CozyItem()
                    .setMaterial(Material.BLADE_POTTERY_SHERD)
                    .setName("&6&lGold Mine")
                    .setLore("&7",
                            "&fMin Players &a" + this.getMinPlayers(),
                            "&fMax Players &a" + this.getMaxPlayers(),
                            "&7",
                            "&fMap Name &eGold Mine",
                            "&7",
                            "&f&l" + this.getGameType().getTitle()
                    )
                    .convert()
            );
        }
        return this;
    }

    @Override
    public void activate() {
        try {
            this.save();

            // Check if the game room identifier has been provided.
            if (this.getGameRoom().isEmpty()) {
                MineManiaTNTRun.getInstance().getLogger().warning("Couldn't not find game room identifier {" + this.getGameRoomIdentifier() + "} for " + this.getIdentifier());
                return;
            }

            MineManiaTNTRun.getInstance()
                    .getSessionManager()
                    .registerSession(new TNTSession(this.getIdentifier()));

            // Check if the schematic has been provided.
            if (this.schematic == null || !WorldEditUtility.getSchematicList().contains(this.getSchematic())) {
                MineManiaTNTRun.getInstance().getLogger().warning("Couldn't not find schematic {" + this.schematic + "} for " + this.getIdentifier());
                return;
            }

            // Paste the schematic.
            Clipboard clipboard = WorldEditUtility.getSchematic(this.getSchematic());
            WorldEditUtility.pasteClipboard(this.getRegion().getMinPoint(), clipboard);

            // Get spawn point as a mine mania location.
            MineManiaLocation location = new BukkitLocationConverter()
                    .getMineManiaLocation(this.getSpawnPoint());

            // Teleport the players.
            for (MineManiaUser user : this.getGameRoom().get().getPlayers()) {
                user.getActions().sendMessage("&7&l> &fGame started! &7Teleporting you to the game arena.");
                user.getActions().teleport(location);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void deactivate() {
        this.setGameRoomIdentifier(null);
        this.save();
        MineManiaTNTRun.getInstance().getSessionManager()
                .getSession(this.getIdentifier())
                .ifPresent(session -> {
                    session.stopComponents();
                    MineManiaTNTRun.getInstance().getSessionManager().unregisterSession(session);
                });
    }

    @Override
    public @NotNull ConfigurationSection convert() {
        ConfigurationSection section = new MemoryConfigurationSection(new LinkedHashMap<>());

        section.set("server_name", this.getServerName());
        section.set("game_type", this.getGameType().name());
        if (this.getGameRoomIdentifier().isPresent())
            section.set("game_room_identifier", this.getGameRoomIdentifier().get().toString());
        section.set("min_players", this.getMinPlayers());
        section.set("max_players", this.getMaxPlayers());
        section.set("display_item", this.getDisplayItemSection() == null ? null : this.getDisplayItemSection().getMap());
        section.set("map_name", this.getMapName());

        section.set("region", this.getRegion().convert().getMap());
        if (this.spawnPoint != null) section.set("spawn_point", this.convertLocation(this.spawnPoint));
        if (this.schematic != null) section.set("schematic", this.getSchematic());

        return section;
    }

    @Override
    public @NotNull TNTArena convert(@NotNull ConfigurationSection section) {

        if (section.getKeys().contains("game_room_identifier"))
            this.setGameRoomIdentifier(UUID.fromString(section.getString("game_room_identifier")));
        this.setMinPlayers(section.getInteger("min_players"));
        this.setMaxPlayers(section.getInteger("max_players"));
        if (section.getKeys().contains("display_item")) this.setDisplayItemSection(section.getSection("display_item"));
        if (section.getKeys().contains("map_name")) this.setMapName(section.getString("map_name"));

        this.setRegion(new Region3D(section.getSection("region")));
        if (section.getKeys().contains("spawn_point"))
            this.setSpawnPoint(this.convertLocation(section.getSection("spawn_point")));
        if (section.getKeys().contains("schematic")) this.setSchematic(section.getString("schematic"));
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
