package com.epic.warpstones.recipes

import com.epic.warpstones.Warpstones
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

class WarpstoneGuideBook {
    private val recipeKey: NamespacedKey;

    constructor(namespace: Warpstones) {
        this.recipeKey = NamespacedKey(namespace, "warpstones_guide_book_recipe")

        this.registerCustomBookRecipe()
    }

    private fun registerCustomBookRecipe() {
        val book = com.epic.warpstones.items.WarpstoneGuideBook().book
        val recipe = ShapedRecipe(this.recipeKey, book)

        recipe.shape("PPP", "PPP", "PPP")
        recipe.setIngredient('P', Material.PAPER)

        Bukkit.addRecipe(recipe)
    }
}