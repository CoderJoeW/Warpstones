package com.epic.warpstones.items

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

class WarpstoneGuideBook {
    public val book: ItemStack
    public val bookMeta: BookMeta

    init {
        this.book = ItemStack(Material.WRITTEN_BOOK)
        this.bookMeta = this.book.itemMeta as BookMeta

        this.bookMeta.title = "Warpstone Book"
        this.bookMeta.author = "Epic"
        this.bookMeta.lore = listOf("Instructions on how to create warpstones!")

        this.bookMeta.addPage(this.page1(), this.page2(), this.page3())
        this.book.itemMeta = this.bookMeta
    }

    fun page1(): String {
        return """
            §lWarpstone Guide§r
            §8--------------------§r
            Create warpstones with signs.
            Follow this format:
            §nLine 1§r: "Warpstone"
            §nLine 2§r: §oYour Warpstone Name§r
            §nLine 3§r: §oDestination Name§r
            §nLine 4§r: Status (auto)
        """.trimIndent()
    }

    fun page2(): String {
        return """
            §lDetails§r
            §8--------------§r
            Line 2 = this warpstone’s
            name. Used by other signs
            to link. Keep it unique.
            Line 3 = where to travel.
            Must exactly match that
            warpstone’s Line 2.
            Example:
              L2: §oTownSquare§r
              L3: §oMineEntrance§r
        """.trimIndent()
    }

    fun page3(): String {
        return """
            §lStatus§r
            §8--------§r
            Line 4 shows:
              • §aLinked§r — names match
              • §cNot Linked§r — mismatch
            Quick checks:
              • Spelling & case match
              • No extra spaces
              • Keep names short
        """.trimIndent()
    }

}