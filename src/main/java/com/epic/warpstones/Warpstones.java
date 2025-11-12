package com.epic.warpstones;

import com.epic.warpstones.interfaces.WarpstoneStorage;
import com.epic.warpstones.models.Warpstone;
import com.epic.warpstones.models.WarpstoneSign;
import com.epic.warpstones.storage.FileStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    private NamespacedKey recipeKey;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);

        this.recipeKey = new NamespacedKey(this, "warpstones_book_recipe");

        registerCustomBookRecipe();

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
            return;
        }

        if (this.warpstoneIsAlreadyLinked(warpstoneSign.name)) {
            event.getPlayer().sendMessage("That name has already 2 warpstones linked");
            return;
        }

        Warpstone newWarpstone = new Warpstone();

        if (this.warpstoneExists(warpstoneSign.name)) {
            Warpstone warpstone = this.findWarpstone(warpstoneSign.name);
            newWarpstone.id = (warpstone.id == 1) ? 2 : 1;
        } else {
            newWarpstone.id = 1;
            event.line(3, WARPSTONE_NOT_LINKED_TEXT);
        }

        newWarpstone.name = warpstoneSign.name;
        newWarpstone.x = event.getBlock().getX();
        newWarpstone.y = event.getBlock().getY();
        newWarpstone.z = event.getBlock().getZ();

        this.warpstonesList.add(newWarpstone);

        event.setLine(2, "ID: " + newWarpstone.id);

        if (this.warpstoneIsAlreadyLinked(warpstoneSign.name)) {
            event.line(3, WARPSTONE_LINKED_TEXT);

            Warpstone linkedWarpstone = this.findWarpstone(warpstoneSign.name, (newWarpstone.id == 1) ? 2 : 1);
            BlockState state = event.getBlock().getWorld().getBlockAt(linkedWarpstone.x,  linkedWarpstone.y, linkedWarpstone.z).getState();

            if (state instanceof Sign sign) {
                sign.getSide(Side.FRONT).line(3, WARPSTONE_LINKED_TEXT);
                sign.update();
            }
        }

        event.getPlayer().sendMessage("Warpstone added");

        this.warpstoneStorage.saveWarpstones(this.warpstonesList);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();

        if (state instanceof Sign sign) {
            WarpstoneSign warpstoneSign = new WarpstoneSign(sign);

            if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name, warpstoneSign.id)) {
                return;
            }

            Warpstone ws = this.findWarpstone(warpstoneSign.name, warpstoneSign.id);

            this.warpstonesList.remove(ws);

            event.getPlayer().sendMessage("Warpstone removed");

            Warpstone linkedWarpstone = this.findWarpstone(warpstoneSign.name);

            if (linkedWarpstone != null) {
                BlockState blockState = event.getBlock().getWorld().getBlockAt(linkedWarpstone.x,  linkedWarpstone.y, linkedWarpstone.z).getState();

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

            if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name, warpstoneSign.id)) {
                return;
            }

            event.setCancelled(true);

            Warpstone linkedWarpstone = this.findWarpstone(warpstoneSign.name, (warpstoneSign.id == 1) ? 2 : 1);

            if (linkedWarpstone == null) {
                event.getPlayer().sendMessage("Warpstone is not linked!");
                return;
            }

            Location location = new Location(event.getPlayer().getWorld(), linkedWarpstone.x, linkedWarpstone.y, linkedWarpstone.z);
            event.getPlayer().teleportAsync(location);
        }
    }

    private void registerCustomBookRecipe() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle("Warpstone Book");
        meta.setAuthor("Epic");
        meta.setLore(List.of("Instructions on how to create warpstones!"));

        StringBuilder builder = new StringBuilder();
        builder.append("Warpstone Guide Book: \n")
                .append("Warpstones are created using special wording on signs\n")
                .append("The first line of the warpstone must have \"Warpstone\"")
                .append("The second line of the warpstone can be any name.")
                .append("Keep in mind that this name is how you will link it to other warpstones\n")
                .append("The third line of the sign will be the name of warpstone you wish to travel to.\n\n")
                .append("Once a warpstone has been linked it will show green text \"Linked\" on the 4th row")
                .append("Otherwise it will show red text \"Not Linked\"");

        meta.addPage(builder.toString());

        book.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(this.recipeKey, book);
        recipe.shape("PPP", "PPP", "PPP");
        recipe.setIngredient('P', Material.PAPER);

        Bukkit.addRecipe(recipe);
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
