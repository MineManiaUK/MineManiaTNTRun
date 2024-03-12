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
import com.github.minemaniauk.minemaniasmp.MineManiaTNTRun;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a session.
 *
 * @param <A> The type of arena this session can be run in.
 */
public class Session<A extends Arena> {

    private final @NotNull String arenaIdentifier;
    private final @NotNull List<SessionComponent<A>> componentList;

    /**
     * Used to create a new session instance.
     *
     * @param arenaIdentifier The arena identifier.
     */
    public Session(@NotNull String arenaIdentifier) {
        this.arenaIdentifier = arenaIdentifier;
        this.componentList = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public @NotNull A getArena() {
        return (A) MineManiaTNTRun.getInstance()
                .getArenaConfiguration()
                .getType(this.arenaIdentifier)
                .orElseThrow();
    }

    /**
     * Used to register a component with this session.
     *
     * @param component The component to register.
     * @return This instance.
     */
    public @NotNull Session<A> registerComponent(@NotNull SessionComponent<A> component) {
        this.componentList.add(component);
        return this;
    }

    /**
     * Used to unregister a component with this session.
     *
     * @param clazz The type of class to unregister.
     * @return This instance.
     */
    public @NotNull Session<A> unregisterComponent(@NotNull Class<SessionComponent<A>> clazz) {
        this.componentList.removeIf(item -> item.getClass().isInstance(clazz));
        return this;
    }

    /**
     * Used to get a session component from this session.
     *
     * @param clazz The type of class to look for.
     * @param <T>   The session component type.
     * @return The instance of the class.
     * @throws RuntimeException If the class doesn't exist.
     */
    public @NotNull <T extends SessionComponent<A>> T getComponent(@NotNull Class<T> clazz) {

        // Loop though components.
        for (SessionComponent<A> component : this.componentList) {
            if (component.getClass().isInstance(clazz)) return (T) component;
        }

        throw new RuntimeException("Tried to get component from session but it doesnt exist. " + clazz.getName());
    }

    /**
     * Used to start all the components in this session.
     *
     * @return This instance.
     */
    public @NotNull Session<A> startComponents() {
        for (SessionComponent<A> component : this.componentList) {
            component.start();
        }

        return this;
    }

    /**
     * Used to stop all the components in this session.
     *
     * @return This instance.
     */
    public @NotNull Session<A> stopComponents() {
        for (SessionComponent<A> component : this.componentList) {
            component.stop();
        }

        return this;
    }
}
