package com.epic.warpstones.recipes;

import com.epic.warpstones.Warpstones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class WarpstoneGuideBook {
    private NamespacedKey recipeKey;

    public WarpstoneGuideBook(Warpstones namespace) {
        this.recipeKey = new NamespacedKey(namespace, "warpstones_guide_book_recipe");

        this.registerCustomBookRecipe();
    }

    private void registerCustomBookRecipe() {
        ItemStack book = new com.epic.warpstones.items.WarpstoneGuideBook().book;
        ShapedRecipe recipe = new ShapedRecipe(this.recipeKey, book);
        recipe.shape("PPP", "PPP", "PPP");
        recipe.setIngredient('P', Material.PAPER);

        Bukkit.addRecipe(recipe);
    }
}
