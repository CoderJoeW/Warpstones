package com.epic.warpstones.models

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side

class WarpstoneSign {
    public val title: String
    public val name: String
    public val destination: String

    constructor(sign: Sign) {
        this.title = this.getContent(sign, 0)
        this.name = this.getContent(sign, 1)
        this.destination = this.getContent(sign, 2)
    }

    constructor(lines: List<Component>) {
        this.title = this.getContent(lines, 0)
        this.name = this.getContent(lines, 1)
        this.destination = this.getContent(lines, 2)
    }

    public fun isValidWarpstoneSign(): Boolean {
        if (this.title.equals("Warpstone")) {
            return true
        }

        return false
    }

    private fun getContent(sign: Sign, index: Int): String {
        val textComponent = sign.getSide(Side.FRONT).lines()[index] as TextComponent
        return textComponent.content()
    }

    private fun getContent(lines: List<Component>, index: Int): String {
        val textComponent = lines[index] as TextComponent
        return textComponent.content()
    }
}