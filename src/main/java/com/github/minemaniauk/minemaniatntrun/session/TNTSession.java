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

package com.github.minemaniauk.minemaniatntrun.session;

import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.minemaniauk.api.MineManiaLocation;
import com.github.minemaniauk.api.database.collection.UserCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.session.Session;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.minemaniatntrun.MineManiaTNTRun;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArenaFactory;
import com.github.minemaniauk.minemaniatntrun.session.component.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a tnt session.
 */
public class TNTSession extends Session<TNTArena> {

    private final @NotNull List<UUID> playersAlive;
    private final @NotNull List<UUID> playersJoined;
    private @NotNull TNTStatus status;
    private UUID winnerUuid;

    /**
     * Used to create a new tnt session.
     *
     * @param arenaIdentifier The arena's identifier.
     */
    public TNTSession(@NotNull UUID arenaIdentifier) {
        super(arenaIdentifier, new TNTArenaFactory());

        this.playersAlive = new ArrayList<>();
        this.playersJoined = new ArrayList<>();
        this.status = TNTStatus.WAITING;

        // Add components.
        this.registerComponent(new TNTScoreboardComponent(this));
        this.registerComponent(new TNTStartUpComponent(this));
        this.registerComponent(new TNTRemovalComponent(this));
        this.registerComponent(new TNTDeathCheckComponent(this));
        this.registerComponent(new TNTSpectatorComponent(this));
        this.registerComponent(new TNTEndComponent(this));

        // Start base components.
        this.getComponent(TNTScoreboardComponent.class).start();
        this.getComponent(TNTStartUpComponent.class).start();
    }

    /**
     * Used to start the tnt game.
     *
     * @return This instance.
     */
    public @NotNull TNTSession startGame() {

        // Add all online players.
        this.playersAlive.addAll(this.getOnlinePlayers()
                .stream()
                .map(Player::getUniqueId)
                .toList()
        );

        // Check if there are enough players to start the game.
        if (this.playersAlive.size() <= 1) {
            this.endGameFully();
        }

        // Start removing tnt.
        this.getComponent(TNTRemovalComponent.class).start();
        this.getComponent(TNTDeathCheckComponent.class).start();
        this.getComponent(TNTSpectatorComponent.class).start();

        return this;
    }

    /**
     * Used to start ending the session.
     *
     * @param winnerUuid The winning player.
     * @return This instance.
     */
    public @NotNull TNTSession endGame(@NotNull UUID winnerUuid) {

        // Set the winner.
        this.winnerUuid = winnerUuid;

        // Set the end status.
        this.setStatus(TNTStatus.END);

        MineManiaTNTRun.getAPI().getDatabase()
                .getTable(UserCollection.class)
                .getUserRecord(winnerUuid)
                .ifPresent(user -> {
                    user.addPaws(20);
                    MineManiaTNTRun.getAPI().getDatabase()
                            .getTable(UserCollection.class)
                            .insertRecord(user);
                });

        MineManiaTNTRun.getInstance()
                .getOnlinePlayer(winnerUuid)
                .ifPresent(player -> {
                    new PlayerUser(player).sendMessage("&a&l> &a+20 paws");
                });

        this.getComponent(TNTEndComponent.class).start();
        return this;
    }

    /**
     * Used to end the game fully.
     *
     * @return The instance.
     */
    public @NotNull TNTSession endGameFully() {
        MineManiaLocation location = new MineManiaLocation("hub", "null", 0, 0, 0);

        // Teleport the players.
        for (Player player : this.getOnlinePlayers()) {
            MineManiaUser user = new MineManiaUser(player.getUniqueId(), player.getName());
            user.getActions().teleport(location);
        }

        // Stop the arena and unregister the session.
        this.getArena().deactivate();
        return this;
    }

    public @NotNull TNTSession onPlayerJoin(@NotNull Player player) {
        this.playersJoined.add(player.getUniqueId());

        PlayerUser user = new PlayerUser(player);
        user.sendMessage(List.of(
                "&8&l------------] &e&lTNT RUN &8&l[------------",
                "&7- &fThe tnt you stand on will slowly disappear.",
                "&7- &fLast player to fall to the bottom wins."
        ));
        return this;
    }

    /**
     * Called when a player dies.
     *
     * @param player The instance of the player.
     * @return The instance of the tnt session.
     */
    public @NotNull TNTSession onPlayerDeath(@NotNull Player player) {
        // Spawn particles and play sound.
        if (player.getLocation().getWorld() != null) {
            player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);
            player.getLocation().getWorld().playSound(player.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1, 1);
        }

        // Remove from ths living player list.
        this.playersAlive.remove(player.getUniqueId());

        // Check if the number of players is now 1.
        if (this.playersAlive.size() == 1) {
            this.endGame(this.playersAlive.get(0));
        }
        return this;
    }

    /**
     * Called when a player disconnects from the game.
     *
     * @param uuid The player's uuid.
     * @return This instance.
     */
    public @NotNull TNTSession onDisconnect(UUID uuid) {
        this.playersAlive.remove(uuid);

        // Check if the number of players is now 1.
        if (this.playersAlive.size() == 1) {
            this.endGame(this.playersAlive.get(0));
        }
        return this;
    }

    /**
     * Used to get the list of player uuid's that is alive.
     *
     * @return The list of living players.
     */
    public @NotNull List<UUID> getAlivePlayerUUIDList() {
        return this.playersAlive;
    }

    /**
     * Used to get the list of living players.
     *
     * @return The list of living players.
     */
    public @NotNull List<Player> getAlivePlayers() {
        List<Player> playerList = new ArrayList<>();

        this.getOnlinePlayers().forEach(player -> {
            if (this.playersAlive.contains(player.getUniqueId())) playerList.add(player);
        });

        return playerList;
    }

    /**
     * The status of the session.
     *
     * @return The tnt status.
     */
    public @NotNull TNTStatus getStatus() {
        return this.status;
    }

    public @NotNull String getWinnerName() {
        if (this.winnerUuid == null) return "None";
        return Objects.requireNonNull(Bukkit.getOfflinePlayer(this.winnerUuid).getName());
    }

    /**
     * Used to set the session status.
     *
     * @param status The status sto set.
     * @return This instance.
     */
    public @NotNull TNTSession setStatus(@NotNull TNTStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Used to get the list of online players.
     *
     * @return The list of online players.
     */
    public @NotNull List<Player> getOnlinePlayers() {

        // Get the instance of the game room record.
        GameRoomRecord gameRoomRecord = this.getArena().getGameRoom().orElse(null);

        // Check if the game room no longer exists.
        if (gameRoomRecord == null) {
            return new ArrayList<>();
        }

        // Create the instance of the list.
        List<Player> playerList = new ArrayList<>();

        // Add the online players.
        gameRoomRecord.getPlayers().forEach(
                user -> MineManiaTNTRun.getInstance()
                        .getOnlinePlayer(user.getUniqueId())
                        .ifPresent(playerList::add)
        );

        return playerList;
    }

    /**
     * Used to get the list of dead players / spectators.
     *
     * @return The list of spectators.
     */
    public @NotNull List<Player> getSpectators() {
        List<Player> playerList = new ArrayList<>();

        for (Player player : this.getOnlinePlayers()) {
            if (this.getAlivePlayerUUIDList().contains(player.getUniqueId())) continue;
            playerList.add(player);
        }

        return playerList;
    }

    /**
     * Used to get the list of disconnected players.
     *
     * @return The list of disconnected players.
     */
    public @NotNull List<UUID> getDisconnectedPlayers() {
        List<UUID> playerUuidList = new ArrayList<>();

        for (UUID playerUuid : this.playersAlive) {
            if (this.getOnlinePlayers().stream().map(Player::getUniqueId).toList().contains(playerUuid)) continue;
            playerUuidList.add(playerUuid);
        }

        return playerUuidList;
    }

    /**
     * Used to get the list of new players joined.
     *
     * @return The list of new players joined.
     */
    public @NotNull List<Player> getPlayersJoined() {
        List<Player> playerUuidList = new ArrayList<>();

        for (Player player : this.getOnlinePlayers()) {
            if (this.playersJoined.contains(player.getUniqueId())) continue;
            playerUuidList.add(player);
        }

        return playerUuidList;
    }

    /**
     * Used to get the number of players that are alive.
     *
     * @return The number of players alive.
     */
    public int getAmountOfPlayersAlive() {
        return this.playersAlive.size();
    }
}
