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

                warpstone.x = Integer.parseInt(parts[0]);
                warpstone.y = Integer.parseInt(parts[1]);
                warpstone.z = Integer.parseInt(parts[2]);
                warpstone.name = parts[3];
                warpstone.id = Integer.parseInt(parts[4]);
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

                builder.append(warpstone.x)
                        .append("|")
                        .append(warpstone.y)
                        .append("|")
                        .append(warpstone.z)
                        .append("|")
                        .append(warpstone.name)
                        .append("|")
                        .append(warpstone.id);

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
