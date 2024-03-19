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

import com.github.cozyplugins.cozylibrary.task.TaskContainer;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.minemaniauk.api.game.session.Session;
import com.github.minemaniauk.api.game.session.SessionComponent;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import com.github.minemaniauk.minemaniatntrun.session.TNTSession;
import com.github.minemaniauk.minemaniatntrun.session.TNTStatus;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class TNTStartUpComponent extends TaskContainer implements SessionComponent<TNTArena> {

    private static final @NotNull String STARTUP_IDENTIFIER = "STARTUP_IDENTIFIER";
    private static final Duration toWait = Duration.ofSeconds(20);

    private final @NotNull Session<TNTArena> session;
    private long startTimeStamp;

    /**
     * Used to create the scoreboard component.
     *
     * @param session The instance of the session.
     */
    public TNTStartUpComponent(@NotNull Session<TNTArena> session) {
        this.session = session;
    }

    @Override
    public @NotNull TNTSession getSession() {
        return (TNTSession) this.session;
    }

    @Override
    public void start() {

        // Set the start time stamp.
        this.startTimeStamp = System.currentTimeMillis();

        this.runTaskLoop(STARTUP_IDENTIFIER, () -> {

            // Check for new players.
            for (Player player : this.getSession().getPlayersJoined()) {
                this.getSession().onPlayerJoin(player);
            }

            // Check if it's time to start the game.
            if (startTimeStamp + toWait.toMillis() < System.currentTimeMillis()) {
                this.stop();
                this.getSession().setStatus(TNTStatus.GAME);
                this.getSession().startGame();
            }

        }, 20);
    }

    @Override
    public void stop() {
        this.stopAllTasks();
    }

    /**
     * Used to get how long until the session will start.
     *
     * @return The duration until the session will start.
     */
    public @NotNull Duration getCountDown() {
        return Duration.ofMillis((startTimeStamp + toWait.toMillis()) - System.currentTimeMillis());
    }
}
