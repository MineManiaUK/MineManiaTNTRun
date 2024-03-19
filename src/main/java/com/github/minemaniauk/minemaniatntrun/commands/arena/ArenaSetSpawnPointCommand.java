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

package com.github.minemaniauk.minemaniatntrun.commands.arena;

import com.github.cozyplugins.cozylibrary.command.command.CozyCommand;
import com.github.cozyplugins.cozylibrary.command.datatype.*;
import com.github.cozyplugins.cozylibrary.pool.PermissionPool;
import com.github.cozyplugins.cozylibrary.user.ConsoleUser;
import com.github.cozyplugins.cozylibrary.user.FakeUser;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.github.cozyplugins.cozylibrary.user.User;
import com.github.minemaniauk.minemaniatntrun.MineManiaTNTRun;
import com.github.minemaniauk.minemaniatntrun.arena.TNTArena;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArenaSetSpawnPointCommand implements CozyCommand {

    @Override
    public @NotNull String getName() {
        return "setSpawnPoint";
    }

    @Override
    public @Nullable CommandAliases getAliases() {
        return null;
    }

    @Override
    public @Nullable String getDescription() {
        return "Used to set an arena's spawn point.";
    }

    @Override
    public @Nullable String getSyntax() {
        return "/tntrun arena setSpawnPoint";
    }

    @Override
    public @Nullable PermissionPool getPermissionPool() {
        return new PermissionPool().append("tntrun.admin");
    }

    @Override
    public @Nullable CommandPool getSubCommands() {
        return null;
    }

    @Override
    public @Nullable CommandSuggestions getSuggestions(@NotNull User user, @NotNull CommandArguments commandArguments) {
        return null;
    }

    @Override
    public @Nullable CommandStatus onUser(@NotNull User user, @NotNull CommandArguments commandArguments) {
        return null;
    }

    @Override
    public @Nullable CommandStatus onPlayerUser(@NotNull PlayerUser user, @NotNull CommandArguments commandArguments, @NotNull CommandStatus commandStatus) {

        // Check if they are standing in an arena.
        final Location location = user.getPlayer().getLocation();

        // Get the instance of the arena.
        final TNTArena arena = MineManiaTNTRun.getInstance().getArena(location).orElse(null);

        // Check if they are not standing in an arena.
        if (arena == null) {
            user.sendMessage("&7&l> &7You are not standing inside an arena.");
            return new CommandStatus();
        }

        // Set the arena's spawn point.
        arena.setSpawnPoint(location);
        arena.save();

        // Send a confirmation message.
        user.sendMessage("&7&l> &7The spawn of " + arena.getIdentifier() + " &7is now set to &f" + location);
        return new CommandStatus();
    }

    @Override
    public @Nullable CommandStatus onFakeUser(@NotNull FakeUser fakeUser, @NotNull CommandArguments commandArguments, @NotNull CommandStatus commandStatus) {
        return null;
    }

    @Override
    public @Nullable CommandStatus onConsoleUser(@NotNull ConsoleUser consoleUser, @NotNull CommandArguments commandArguments, @NotNull CommandStatus commandStatus) {
        return null;
    }
}
