package com.epic.warpstones;

import com.epic.warpstones.interfaces.WarpstoneStorage;
import com.epic.warpstones.models.Warpstone;
import com.epic.warpstones.models.WarpstoneSign;
import com.epic.warpstones.recipes.WarpstoneGuideBook;
import com.epic.warpstones.storage.FileStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Warpstones extends JavaPlugin implements Listener {
    public List<Warpstone> warpstonesList = new ArrayList<>();
    public WarpstoneStorage warpstoneStorage;

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

        new WarpstoneGuideBook(this);

        try {
            warpstoneStorage = new FileStorage();

            warpstonesList = warpstoneStorage.loadWarpstones();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        WarpstoneSign warpstoneSign = new WarpstoneSign(event.lines());

        if (!warpstoneSign.title.equals(WARPSTONE_IDENTIFIER)) {
            return;
        }

        if (warpstoneSign.name.isEmpty()) {
            event.getPlayer().sendMessage("Warpstones require a name for linking on the second line");
            event.setCancelled(true);
            return;
        }

        if (warpstoneSign.destination.isEmpty()) {
            event.getPlayer().sendMessage("Warpstones require a destination for linking on the second line");
            event.setCancelled(true);
            return;
        }

        if (this.findWarpstone(warpstoneSign.name, event.getPlayer().getUniqueId()) != null) {
            event.getPlayer().sendMessage("Warpstone with name " + warpstoneSign.name + " already exists");
            event.setCancelled(true);
            return;
        }

        Warpstone newWarpstone = new Warpstone();

        if (this.warpstoneExists(warpstoneSign.destination,  event.getPlayer().getUniqueId())) {
            Warpstone warpstone = this.findWarpstone(warpstoneSign.destination,  event.getPlayer().getUniqueId());
            event.line(3, WARPSTONE_LINKED_TEXT);
        } else {
            event.line(3, WARPSTONE_NOT_LINKED_TEXT);
        }

        newWarpstone.name = warpstoneSign.name;
        newWarpstone.destination = warpstoneSign.destination;
        newWarpstone.x = event.getBlock().getX();
        newWarpstone.y = event.getBlock().getY();
        newWarpstone.z = event.getBlock().getZ();
        newWarpstone.owner = event.getPlayer().getUniqueId();

        this.warpstonesList.add(newWarpstone);

        event.getPlayer().sendMessage("Warpstone added");

        List<Warpstone> linkedWarpstones = this.findLinkedWarpstones(warpstoneSign.name, event.getPlayer().getUniqueId());

        for (Warpstone w : linkedWarpstones) {
            BlockState blockState = event.getBlock().getWorld().getBlockAt(w.x,  w.y, w.z).getState();

            if (blockState instanceof Sign linkedSign) {
                linkedSign.getSide(Side.FRONT).line(3, WARPSTONE_LINKED_TEXT);
                linkedSign.update();
            }
        }

        this.warpstoneStorage.saveWarpstones(this.warpstonesList);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();

        if (state instanceof Sign sign) {
            WarpstoneSign warpstoneSign = new WarpstoneSign(sign);

            if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name,  event.getPlayer().getUniqueId())) {
                return;
            }

            Warpstone ws = this.findWarpstone(warpstoneSign.name, event.getPlayer().getUniqueId());

            this.warpstonesList.remove(ws);

            event.getPlayer().sendMessage("Warpstone removed");

            List<Warpstone> linkedWarpstones = this.findLinkedWarpstones(warpstoneSign.name, event.getPlayer().getUniqueId());

            for (Warpstone w : linkedWarpstones) {
                BlockState blockState = event.getBlock().getWorld().getBlockAt(w.x,  w.y, w.z).getState();

                if (blockState instanceof Sign linkedSign) {
                    linkedSign.getSide(Side.FRONT).line(3, WARPSTONE_NOT_LINKED_TEXT);
                    linkedSign.update();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getState() instanceof Sign sign) {
            WarpstoneSign warpstoneSign = new WarpstoneSign(sign);

            if (this.warpstoneExists(warpstoneSign.name) && !this.warpstoneExists(warpstoneSign.name, event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage("Warpstone does not belong to you");
                event.setCancelled(true);
                return;
            }

            if (!this.warpstoneAtLocationBelongsToPlayer(warpstoneSign.name, event.getPlayer().getUniqueId(), event.getClickedBlock().getLocation())) {
                event.getPlayer().sendMessage("Warpstone does not belong to you");
                event.setCancelled(true);
                return;
            }

            if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name,  event.getPlayer().getUniqueId())) {
                return;
            }

            event.setCancelled(true);

            Warpstone linkedWarpstone = this.findWarpstone(warpstoneSign.destination,  event.getPlayer().getUniqueId());

            if (linkedWarpstone == null) {
                event.getPlayer().sendMessage("Warpstone is not linked!");
                return;
            }

            Location location = new Location(event.getPlayer().getWorld(), linkedWarpstone.x, linkedWarpstone.y, linkedWarpstone.z);
            event.getPlayer().teleportAsync(location);
        }
    }

    private List<Warpstone> findLinkedWarpstones(String name, UUID owner) {
        List<Warpstone> warpstones = new ArrayList<>();

        for (Warpstone ws : this.warpstonesList) {
            if (ws.destination.equals(name) && ws.owner.equals(owner)) {
                warpstones.add(ws);
            }
        }

        return warpstones;
    }

    private Warpstone findWarpstone(String name, UUID owner) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name) && ws.owner.equals(owner)) {
                return ws;
            }
        }

        return null;
    }

    private boolean warpstoneExists(String name, UUID owner) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name) && ws.owner.equals(owner)) {
                return true;
            }
        }

        return false;
    }

    private boolean warpstoneExists(String name) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private boolean warpstoneAtLocationBelongsToPlayer(String name, UUID owner, Location location) {
        for (Warpstone ws : this.warpstonesList) {
            if (ws.name.equals(name) && ws.owner.equals(owner) && ws.x == location.getX() && ws.y == location.getY() && ws.z == location.getZ()) {
                return true;
            }
        }

        return false;
    }
}
