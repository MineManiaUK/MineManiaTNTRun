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

package com.github.minemaniauk.minemaniasmp.session;

import com.github.minemaniauk.api.game.Arena;
import org.jetbrains.annotations.NotNull;

public interface SessionComponent<A extends Arena> {

    /**
     * Used to get the instance of the session this
     * component is part of.
     *
     * @return The instance of the session.
     */
    @NotNull Session<A> getSession();

    /**
     * Used to start the session component.
     */
    void start();

    /**
     * Used to stop the session component.
     */
    void stop();
}
