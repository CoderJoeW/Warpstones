package com.epic.warpstones.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class WarpstoneGuideBook {
    public ItemStack book;
    private BookMeta bookMeta;

    public WarpstoneGuideBook() {
        this.book = new ItemStack(Material.WRITTEN_BOOK);
        this.bookMeta = (BookMeta) book.getItemMeta();

        this.bookMeta.setTitle("Warpstone Book");
        this.bookMeta.setAuthor("Epic");
        this.bookMeta.setLore(List.of("Instructions on how to create warpstones!"));

        String page1 =
                "§lWarpstone Guide§r\n" +
                        "§8--------------------§r\n" +
                        "\n" +
                        "Create warpstones with signs.\n" +
                        "Follow this format:\n" +
                        "\n" +
                        "§nLine 1§r: \"Warpstone\"\n" +
                        "§nLine 2§r: §oYour Warpstone Name§r\n" +
                        "§nLine 3§r: §oDestination Name§r\n" +
                        "§nLine 4§r: Status (auto)\n";

        String page2 =
                "§lDetails§r\n" +
                        "§8--------------§r\n" +
                        "\n" +
                        "Line 2 = this warpstone’s\n" +
                        "name. Used by other signs\n" +
                        "to link. Keep it unique.\n" +
                        "\n" +
                        "Line 3 = where to travel.\n" +
                        "Must exactly match that\n" +
                        "warpstone’s Line 2.\n" +
                        "\n" +
                        "Example:\n" +
                        "  L2: §oTownSquare§r\n" +
                        "  L3: §oMineEntrance§r\n";

        String page3 =
                "§lStatus§r\n" +
                        "§8--------§r\n" +
                        "\n" +
                        "Line 4 shows:\n" +
                        "  • §aLinked§r — names match\n" +
                        "  • §cNot Linked§r — mismatch\n" +
                        "\n" +
                        "Quick checks:\n" +
                        "• Spelling & case match\n" +
                        "• No extra spaces\n" +
                        "• Keep names short\n";

        this.bookMeta.addPage(page1, page2, page3);
        book.setItemMeta(this.bookMeta);
    }
}
