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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TNTRemovalComponent extends TaskContainer implements SessionComponent<TNTArena> {

    private static final @NotNull String TNT_REMOVAL = "TNT_REMOVAL";

    private final @NotNull Session<TNTArena> session;

    /**
     * Used to create the scoreboard component.
     *
     * @param session The instance of the session.
     */
    public TNTRemovalComponent(@NotNull Session<TNTArena> session) {
        this.session = session;
    }

    @Override
    public @NotNull TNTSession getSession() {
        return (TNTSession) this.session;
    }

    @Override
    public void start() {

        this.runTaskLoop(TNT_REMOVAL, () -> {

            for (Player player : this.getSession().getAlivePlayers()) {
                Location locationToCheck = player.getLocation().clone().add(new Vector(0, -1, 0));

                if (locationToCheck.getBlock().getType().equals(Material.TNT)) {
                    this.removeTnt(locationToCheck);
                    continue;
                }

                Region3D region = new Region3D(
                        locationToCheck.clone().add(new Vector(2, 1, 2)),
                        locationToCheck.clone().add(new Vector(-2, -1, -2))
                );

                for (Block block : region.getBlockList()) {
                    if (!block.getType().equals(Material.TNT)) continue;
                    this.removeTnt(block.getLocation());
                    break;
                }
            }

        }, 5);
    }

    @Override
    public void stop() {
        this.stopAllTasks();
    }

    /**
     * Used to slowly remove a piece of tnt.
     *
     * @param location The location of the tnt.
     * @return This instance.
     */
    public @NotNull TNTRemovalComponent removeTnt(Location location) {

        location.getBlock().setType(Material.WHITE_STAINED_GLASS);
        this.runTaskLater(UUID.randomUUID().toString(), () -> {
            location.getBlock().setType(Material.AIR);
            if (location.getWorld() == null) return;
            location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 1);
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.2F, 1);
        }, 17);

        return this;
    }
}
