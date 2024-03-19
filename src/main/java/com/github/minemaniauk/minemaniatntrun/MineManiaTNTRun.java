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

package com.github.minemaniauk.minemaniatntrun;

import com.github.cozyplugins.cozylibrary.CozyPlugin;
import com.github.cozyplugins.cozylibrary.command.command.command.ProgrammableCommand;
import com.github.cozyplugins.cozylibrary.location.Region3D;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.game.session.SessionManager;
import com.github.minemaniauk.bukkitapi.MineManiaAPI_Bukkit;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import com.github.minemaniauk.minemaniatntrun.commands.arena.ArenaCreateCommand;
import com.github.minemaniauk.minemaniatntrun.commands.arena.ArenaSetSchematicCommand;
import com.github.minemaniauk.minemaniatntrun.commands.arena.ArenaSetSpawnPointCommand;
import com.github.minemaniauk.minemaniatntrun.configuration.ArenaConfiguration;
import com.github.minemaniauk.minemaniatntrun.session.TNTSession;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents the main class.
 */
public final class MineManiaTNTRun extends CozyPlugin {

    private static @NotNull MineManiaTNTRun instance;

    private @NotNull ArenaConfiguration arenaConfiguration;
    private @NotNull SessionManager<TNTSession, TNTArena> sessionManager;

    @Override
    public boolean enableCommandDirectory() {
        return false;
    }

    @Override
    public void onCozyEnable() {

        // Initialize this instance.
        MineManiaTNTRun.instance = this;

        // Add configuration.
        this.arenaConfiguration = new ArenaConfiguration();
        this.arenaConfiguration.reload();

        // Add arenas from configuration to api.
        this.arenaConfiguration.getAllTypes().forEach(
                arena -> MineManiaTNTRun.getAPI().getGameManager().registerArena(arena)
        );

        // Add session manager.
        this.sessionManager = new SessionManager<>();

        // Add commands.
        this.addCommand(new ProgrammableCommand("tntrun")
                .setDescription("Contains tnt run commands.")
                .setSyntax("/tntrun")
                .addSubCommand(new ProgrammableCommand("arena")
                        .setDescription("Contains the arena commands")
                        .setSyntax("/tntrun arena")
                        .addSubCommand(new ArenaCreateCommand())
                        .addSubCommand(new ArenaSetSpawnPointCommand())
                        .addSubCommand(new ArenaSetSchematicCommand())
                )
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Loop though all sessions and stop them.
        this.sessionManager.stopAllSessionComponents();

        // Unregister the local arenas.
        MineManiaTNTRun.getAPI().getGameManager().unregisterLocalArenas();
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
     * Used to get the instance of the session manager.
     *
     * @return The instance of the session manager.
     */
    public @NotNull SessionManager<TNTSession, TNTArena> getSessionManager() {
        return this.sessionManager;
    }

    /**
     * Used to create a new arena and register it
     * with the api and plugin.
     *
     * @param identifier The instance of the identifier.
     * @param region     The instance of the arena region.
     * @param minPlayers The min number of players.
     * @param maxPlayers The max number of players.
     * @return The instance of the tnt arena created.
     */
    public @NotNull TNTArena createArena(@NotNull UUID identifier, @NotNull Region3D region, int minPlayers, int maxPlayers) {
        TNTArena arena = new TNTArena(identifier);
        arena.setRegion(region);
        arena.setMinPlayers(minPlayers);
        arena.setMaxPlayers(maxPlayers);

        // Register and save the arena.
        MineManiaTNTRun.getAPI().getGameManager().registerArena(arena);
        return arena;
    }

    /**
     * Used to get the instance of an arena from a specific location.
     *
     * @param location The location inside an arena.
     * @return The arena that contains the location.
     */
    public @NotNull Optional<TNTArena> getArena(@NotNull Location location) {
        for (TNTArena arena : this.getArenaConfiguration().getAllTypes()) {
            if (arena.getRegion().contains(location)) return Optional.of(arena);
        }

        return Optional.empty();
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
