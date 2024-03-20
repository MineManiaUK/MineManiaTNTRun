package com.github.minemaniauk.minemaniatntrun;

import com.github.cozyplugins.cozylibrary.location.Region3D;
import com.github.cozyplugins.cozylibrary.user.PlayerUser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the world edit utility class.
 * Contains methods that interact with world edit.
 */
public final class WorldEditUtility {

    /**
     * Used to get a user's selection.
     *
     * @param user The instance of a user.
     * @return The requested selection.
     */
    public static @Nullable Region getSelection(@NotNull PlayerUser user) {
        Player actor = BukkitAdapter.adapt(user.getPlayer());
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        return session.getSelection();
    }

    /**
     * Used to get a user's selection as a region 3d.
     *
     * @param user The instance of a user.
     * @return This requested region.
     */
    public static @NotNull Region3D getSelectionRegion3D(@NotNull PlayerUser user) {
        Player actor = BukkitAdapter.adapt(user.getPlayer());

        // Create the region.
        return new Region3D(
                new Location(
                        BukkitAdapter.asBukkitWorld(actor.getWorld()).getWorld(),
                        actor.getSelection().getMaximumPoint().getBlockX(),
                        actor.getSelection().getMaximumPoint().getBlockY(),
                        actor.getSelection().getMaximumPoint().getBlockZ()
                ),
                new Location(
                        BukkitAdapter.asBukkitWorld(actor.getWorld()).getWorld(),
                        actor.getSelection().getMinimumPoint().getBlockX(),
                        actor.getSelection().getMinimumPoint().getBlockY(),
                        actor.getSelection().getMinimumPoint().getBlockZ()
                )
        );
    }

    /**
     * Used to get the list of available schematic identifiers.
     * <li>Without the extensions</li>
     *
     * @return The list of schematic identifiers.
     */
    public static List<String> getSchematicList() {

        // Get the schematics folder.
        File folder = new File(
                Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit").getDataFolder()
                        + "/schematics"
        );

        String[] fileNames = folder.list();
        if (fileNames == null) return new ArrayList<>();

        List<String> fileNameList = new ArrayList<>(Arrays.asList(fileNames));
        List<String> fileNameWithoutExstentionList = new ArrayList<>();

        // Replace the extensions with nothing.
        for (String fileName : fileNameList) {
            fileNameWithoutExstentionList.add(fileName.replace(".schem", ""));
        }

        return fileNameWithoutExstentionList;
    }

    /**
     * Used to get a schematic from world edit.
     *
     * @param identifier The schematic identifier.
     *                   Without extensions.
     * @return The requested clipboard.
     */
    public static @Nullable Clipboard getSchematic(String identifier) {
        // Create file instance.
        File file = new File(
                Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit").getDataFolder() + "/schematics",
                identifier + ".schem"
        );

        // Get clipboard format.
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return null;

        // Create clipboard.
        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            return reader.read();

        } catch (IOException exception) {
            MineManiaTNTRun.getInstance().getLogger().warning("Tried to get a schematic but could not read the file.");
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to load a clipboard.
     *
     * @param location  The location to paste the clipboard.
     * @param clipboard The instance of a clipboard.
     */
    public static void pasteClipboard(@NotNull Location location, @NotNull Clipboard clipboard) {

        // Get the instance of the world.
        World world = BukkitAdapter.adapt(location.getWorld());

        // Paste the clipboard.
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .build();
            Operations.complete(operation);

        } catch (Exception exception) {
            MineManiaTNTRun.getInstance().getLogger().warning("Tried to paste a schematic but was unable to.");
            throw new RuntimeException(exception);
        }
    }
}
