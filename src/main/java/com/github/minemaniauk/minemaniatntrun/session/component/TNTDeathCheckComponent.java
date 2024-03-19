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

import com.github.cozyplugins.cozylibrary.location.Region3D;
import com.github.cozyplugins.cozylibrary.task.TaskContainer;
import com.github.minemaniauk.api.game.session.Session;
import com.github.minemaniauk.api.game.session.SessionComponent;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import com.github.minemaniauk.minemaniatntrun.session.TNTSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TNTDeathCheckComponent extends TaskContainer implements SessionComponent<TNTArena> {

    private static final @NotNull String TNT_DEATH = "TNT_DEATH";

    private final @NotNull Session<TNTArena> session;

    /**
     * Used to create the scoreboard component.
     *
     * @param session The instance of the session.
     */
    public TNTDeathCheckComponent(@NotNull Session<TNTArena> session) {
        this.session = session;
    }

    @Override
    public @NotNull TNTSession getSession() {
        return (TNTSession) this.session;
    }

    @Override
    public void start() {

        this.runTaskLoop(TNT_DEATH, () -> {

            for (UUID uuid : this.getSession().getDisconnectedPlayers()) {
                this.getSession().onDisconnect(uuid);
            }

            for (Player player : this.getSession().getAlivePlayers()) {
                Region3D region = new Region3D(player.getLocation().clone(), player.getLocation().clone());
                region.expand(200, 10, 200);
                if (region.contains(this.getSession().getArena().getRegion().getMinPoint())) {
                    this.getSession().onPlayerDeath(player);
                }
            }

        }, 20);
    }

    @Override
    public void stop() {
        this.stopAllTasks();
    }
}