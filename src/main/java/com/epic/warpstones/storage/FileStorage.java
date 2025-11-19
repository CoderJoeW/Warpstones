package com.epic.warpstones.storage;

import com.epic.warpstones.Warpstones;
import com.epic.warpstones.interfaces.WarpstoneStorage;
import com.epic.warpstones.models.Warpstone;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileStorage implements WarpstoneStorage {
    private JavaPlugin plugin;
    private File database;

    private final String DATABASE_FILE_NAME = "warpstones.dat";

    public FileStorage() throws IOException {
        this.plugin = JavaPlugin.getPlugin(Warpstones.class);

        File dataFolder = this.plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.database =  new File(dataFolder, DATABASE_FILE_NAME);

        if (!this.database.exists()) {
            this.database.createNewFile();
        }
    }

    @Override
    public List<Warpstone> loadWarpstones() {
        List<Warpstone> warpstones = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(this.database.getAbsolutePath()));

            for (String line : lines) {
                Warpstone warpstone = new Warpstone();
                String[] parts = line.split("\\|");

                warpstone.setX(Integer.parseInt(parts[0]));
                warpstone.setY(Integer.parseInt(parts[1]));
                warpstone.setZ(Integer.parseInt(parts[2]));
                warpstone.setName(parts[3]);
                warpstone.setDestination(parts[4]);
                warpstone.setOwner(UUID.fromString(parts[5]));
                warpstones.add(warpstone);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return warpstones;
    }

    @Override
    public void saveWarpstones(List<Warpstone> warpstones) {
        try {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < warpstones.size(); i++) {
                Warpstone warpstone = warpstones.get(i);

                builder.append(warpstone.getX())
                        .append("|")
                        .append(warpstone.getY())
                        .append("|")
                        .append(warpstone.getZ())
                        .append("|")
                        .append(warpstone.getName())
                        .append("|")
                        .append(warpstone.getDestination())
                        .append("|")
                        .append(warpstone.getOwner().toString());

                if (i + 1 != warpstones.size()) {
                    builder.append("\n");
                }
            }

            Files.writeString(Paths.get(this.database.getAbsolutePath()), builder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
