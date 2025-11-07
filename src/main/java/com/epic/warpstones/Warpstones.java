package com.epic.warpstones;

import com.epic.warpstones.models.Warpstone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Warpstones extends JavaPlugin implements Listener {
    public List<Warpstone> warpstonesList = new ArrayList<>();

    public final String WARPSTONE_IDENTIFIER = "Warpstone";
    public final Component WARPSTONE_LINKED_TEXT = Component
            .text("Linked")
            .color(TextColor.color(0, 255, 0));
    public final Component WARPSTONE_NOT_LINKED_TEXT = Component
            .text("Not Linked")
            .color(TextColor.color(255, 0, 0));

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        List<Component> lines = event.lines();
        String titleLine = ((TextComponent)lines.getFirst()).content();
        String nameLine = ((TextComponent)lines.get(1)).content();

        if (titleLine.equals(WARPSTONE_IDENTIFIER)) {
            if (nameLine.isEmpty()) {
                event.getPlayer().sendMessage("Warpstones require a name for linking on the second line");
                return;
            }

            if (this.warpstoneIsAlreadyLinked(nameLine)) {
                event.getPlayer().sendMessage("That name has already 2 warpstones linked");
                return;
            }

            int warpstoneId = 1;

            if (this.warpstoneExists(nameLine)) {
                Warpstone warpstone = this.findWarpstone(nameLine);
                warpstoneId = (warpstone.id == 1) ? 2 : 1;
            }

            Warpstone newWarpstone = new Warpstone();
            newWarpstone.name = nameLine;
            newWarpstone.id = warpstoneId;
            newWarpstone.x = event.getBlock().getX();
            newWarpstone.y = event.getBlock().getY();
            newWarpstone.z = event.getBlock().getZ();

            this.warpstonesList.add(newWarpstone);

            event.setLine(2, "ID: " + warpstoneId);

            if (this.warpstoneIsAlreadyLinked(nameLine)) {
                event.line(3, WARPSTONE_LINKED_TEXT);

                Warpstone linkedWarpstone = this.findWarpstone(nameLine, (warpstoneId == 1) ? 2 : 1);
                BlockState state = event.getBlock().getWorld().getBlockAt(linkedWarpstone.x,  linkedWarpstone.y, linkedWarpstone.z).getState();

                if (state instanceof Sign sign) {
                    sign.getSide(Side.FRONT).line(3, WARPSTONE_LINKED_TEXT);
                    sign.update();
                }
            }

            event.getPlayer().sendMessage("Warpstone added");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();

        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            String titleLine = ((TextComponent)sign.lines().getFirst()).content();
            String nameLine = ((TextComponent)sign.lines().get(1)).content();
            String idLine = ((TextComponent)sign.lines().get(2)).content().split(" ")[1];
            int id = Integer.parseInt(idLine);

            if (!titleLine.equals(WARPSTONE_IDENTIFIER) || !this.warpstoneExists(nameLine, id)) {
                return;
            }

            Warpstone ws = this.findWarpstone(nameLine, id);

            this.warpstonesList.remove(ws);

            event.getPlayer().sendMessage("Warpstone removed");

            Warpstone linkedWarpstone = this.findWarpstone(nameLine);

            if (linkedWarpstone != null) {
                BlockState blockState = event.getBlock().getWorld().getBlockAt(linkedWarpstone.x,  linkedWarpstone.y, linkedWarpstone.z).getState();

                if (blockState instanceof Sign linkedSign) {
                    linkedSign.getSide(Side.FRONT).line(3, WARPSTONE_NOT_LINKED_TEXT);
                    linkedSign.update();
                }
            }
        }
    }

    private Warpstone findWarpstone(String name) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name)) {
                return ws;
            }
        }

        return null;
    }

    private Warpstone findWarpstone(String name, int id) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name) && ws.id == id) {
                return ws;
            }
        }

        return null;
    }

    private boolean warpstoneExists(String name) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private boolean warpstoneExists(String name, int id) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name) && ws.id == id) {
                return true;
            }
        }

        return false;
    }

    private boolean warpstoneIsAlreadyLinked(String name) {
        int warpstones = 0;

        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name)) {
                warpstones++;
            }
        }

        return warpstones == 2;
    }
}
