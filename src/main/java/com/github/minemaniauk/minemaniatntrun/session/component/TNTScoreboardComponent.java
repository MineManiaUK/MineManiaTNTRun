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

package com.github.minemaniauk.minemaniatntrun.session.component;

import com.github.cozyplugins.cozylibrary.scoreboard.Scoreboard;
import com.github.cozyplugins.cozylibrary.task.TaskContainer;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.minemaniauk.api.game.session.Session;
import com.github.minemaniauk.api.game.session.SessionComponent;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import com.github.minemaniauk.minemaniatntrun.session.TNTSession;
import com.github.minemaniauk.minemaniatntrun.session.TNTStatus;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the scoreboard component.
 */
public class TNTScoreboardComponent extends TaskContainer implements SessionComponent<TNTArena> {

    private static final @NotNull String SCOREBOARD_IDENTIFIER = "SCOREBOARD_IDENTIFIER";

    private final @NotNull Session<TNTArena> session;

    /**
     * Used to create the scoreboard component.
     *
     * @param session The instance of the session.
     */
    public TNTScoreboardComponent(@NotNull Session<TNTArena> session) {
        this.session = session;
    }

    @Override
    public @NotNull TNTSession getSession() {
        return (TNTSession) this.session;
    }

    @Override
    public void start() {
        this.runTaskLoop(SCOREBOARD_IDENTIFIER, () -> {

            Scoreboard scoreboard = this.generateScoreboard();

            for (Player player : this.getSession().getOnlinePlayers()) {
                new PlayerUser(player).setScoreboard(scoreboard);
            }

        }, 20);
    }

    @Override
    public void stop() {
        this.stopAllTasks();
    }

    /**
     * Used to generate a new updated instance of the scoreboard.
     *
     * @return The instance of the scoreboard.
     */
    public @NotNull Scoreboard generateScoreboard() {
        if (this.getSession().getStatus().equals(TNTStatus.END)) {
            return new Scoreboard()
                    .setTitle("&e&lTNT RUN")
                    .setLines("&8" + this.getSession().getArenaIdentifier().toString().substring(0, 7),
                            "&7",
                            "&fWinner &a" + this.getSession().getWinnerName(),
                            "&fEnding in &a" + this.getSession().getComponent(TNTEndComponent.class).getCountDown().getSeconds(),
                            "&7",
                            "&e63.135.76.209:25565"
                    );
        }
        if (this.getSession().getStatus().equals(TNTStatus.WAITING)) {
            return new Scoreboard()
                    .setTitle("&e&lTNT RUN")
                    .setLines("&8" + this.getSession().getArenaIdentifier().toString().substring(0, 7),
                            "&7",
                            "&fStarting in &a" + this.getSession().getComponent(TNTStartUpComponent.class).getCountDown().toSeconds(),
                            "&7",
                            "&e63.135.76.209:25565"
                    );
        }
        return new Scoreboard()
                .setTitle("&e&lTNT RUN")
                .setLines("&8" + this.getSession().getArenaIdentifier().toString().substring(0, 7),
                        "&7",
                        "&fPlayers Alive &a" + this.getSession().getAmountOfPlayersAlive(),
                        "&7",
                        "&e63.135.76.209:25565"
                );
    }
}
