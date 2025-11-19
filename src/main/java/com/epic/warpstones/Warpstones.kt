package com.epic.warpstones

import com.epic.warpstones.interfaces.WarpstoneStorage
import com.epic.warpstones.models.Warpstone
import com.epic.warpstones.models.WarpstoneSign
import com.epic.warpstones.recipes.WarpstoneGuideBook
import com.epic.warpstones.storage.FileStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

const val WARPSTONE_IDENTIFIER = "Warpstone"

class Warpstones: JavaPlugin(), Listener {
    public var warpstonesList = ArrayList<Warpstone>()
    public lateinit var warpstoneStorage: WarpstoneStorage

    val warpstoneLinkedText = Component
        .text("Linked")
        .color(TextColor.color(0, 255, 0));
    val warpstoneNotLinkedText = Component
        .text("Not Linked")
        .color(TextColor.color(255, 0, 0));

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)

        this.loadRecipes()
        this.initWarpstoneStorage()
    }

    private fun loadRecipes() {
        WarpstoneGuideBook(this)
    }

    private fun initWarpstoneStorage() {
        this.warpstoneStorage = FileStorage()
        this.warpstonesList = this.warpstoneStorage.loadWarpstones()
    }

    @EventHandler
    public fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val book = com.epic.warpstones.items.WarpstoneGuideBook().book

        event.player.inventory.addItem(book)
    }

    @EventHandler
    public fun onSignChange(event: SignChangeEvent) {
        val warpstoneSign = WarpstoneSign(event.lines())

        if (!warpstoneSign.title.equals(WARPSTONE_IDENTIFIER)) {
            return
        }

        if (warpstoneSign.name.isEmpty()) {
            event.player.sendMessage("Warpstones require a name for linking on the second line")
            event.isCancelled = true
            return
        }

        if (warpstoneSign.destination.isEmpty()) {
            event.player.sendMessage("Warpstones require a destination for linking on the third line")
            event.isCancelled = true
            return
        }

        if (this.findWarpstone(warpstoneSign.name, event.player.uniqueId) != null) {
            event.player.sendMessage("Warpstone with name $warpstoneSign.name already exists")
            event.isCancelled = true
            return
        }

        val newWarpstone = Warpstone()

        if (this.warpstoneExists(warpstoneSign.destination, event.player.uniqueId)) {
            val warpstone = this.findWarpstone(warpstoneSign.destination, event.player.uniqueId)
            event.line(3, this.warpstoneLinkedText)
        } else {
            event.line(3, this.warpstoneNotLinkedText)
        }

        newWarpstone.name = warpstoneSign.name
        newWarpstone.destination = warpstoneSign.destination
        newWarpstone.x = event.block.location.x
        newWarpstone.y = event.block.location.y
        newWarpstone.z = event.block.location.z
        newWarpstone.owner = event.player.uniqueId

        this.warpstonesList.add(newWarpstone)

        event.player.sendMessage("Warpstone added")

        val linkedWarpstones = this.findLinkedWarpstones(warpstoneSign.name, event.player.uniqueId)

        for (ws in linkedWarpstones) {
            val linkedSign = event.block.world.getBlockAt(Location(event.block.world, ws.x, ws.y, ws.z)).state

            if (linkedSign is Sign) {
                linkedSign.getSide(Side.FRONT).line(3, this.warpstoneLinkedText)
                linkedSign.update()
            }
        }

        this.warpstoneStorage.saveWarpstones(this.warpstonesList)
    }

    @EventHandler
    public fun onBlockBreak(event: BlockBreakEvent) {
        val sign = event.block.state;

        if (sign !is Sign) {
            return
        }

        val warpstoneSign = WarpstoneSign(sign);

        if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name, event.player.uniqueId)) {
            return
        }

        val warpstone = this.findWarpstone(warpstoneSign.name, event.player.uniqueId)
        this.warpstonesList.remove(warpstone)

        event.player.sendMessage("Warpstone removed")

        val linkedWarpstones = this.findLinkedWarpstones(warpstoneSign.name, event.player.uniqueId)
        for (ws in linkedWarpstones) {
            val linkedSign = event.block.world.getBlockAt(Location(event.block.world, ws.x, ws.y, ws.z)).state

            if (linkedSign is Sign) {
                linkedSign.getSide(Side.FRONT).line(3, this.warpstoneNotLinkedText)
                linkedSign.update()
            }
        }
    }

    @EventHandler
    public fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val block = event.clickedBlock ?: return

        if (block.state !is Sign) {
            return
        }

        val warpstoneSign = WarpstoneSign(block.state as Sign)

        if (!this.warpstoneAtLocationBelongsToPlayer(warpstoneSign.name, event.player.uniqueId, event.clickedBlock!!.location)) {
            event.player.sendMessage("Warpstone does not belong to you")
            event.isCancelled = true
            return
        }

        if (!warpstoneSign.isValidWarpstoneSign() || !this.warpstoneExists(warpstoneSign.name, event.player.uniqueId)) {
            return
        }

        event.isCancelled = true

        val linkedWarpstone = this.findWarpstone(warpstoneSign.destination, event.player.uniqueId)

        if (linkedWarpstone == null) {
            event.player.sendMessage("Warpstone is not linked")
            return
        }

        val location = Location(event.player.world, linkedWarpstone.x, linkedWarpstone.y, linkedWarpstone.z)
        event.player.teleportAsync(location)
    }

    private fun findLinkedWarpstones(name: String, owner: UUID): ArrayList<Warpstone> {
        var warpstones = ArrayList<Warpstone>()

        for (ws in this.warpstonesList) {
            if (ws.destination.equals(name) && ws.owner!!.equals(owner)) {
                warpstones.add(ws)
            }
        }

        return warpstones
    }

    private fun findWarpstone(name: String, owner: UUID): Warpstone? {
        for (ws in this.warpstonesList) {
            if (ws.name.equals(name) && ws.owner!!.equals(owner)) {
                return ws
            }
        }

        return null
    }

    private fun warpstoneExists(name: String, owner: UUID): Boolean {
        for (ws in this.warpstonesList) {
            if (ws.name.equals(name) && ws.owner!!.equals(owner)) {
                return true
            }
        }

        return false
    }

    private fun warpstoneExists(name: String): Boolean {
        for (ws in this.warpstonesList) {
            if (ws.name.equals(name)) {
                return true
            }
        }

        return false
    }

    private fun warpstoneAtLocationBelongsToPlayer(name: String, owner: UUID, location: Location): Boolean {
        for (ws in this.warpstonesList) {
            if (ws.name == name && ws.owner == owner && ws.x == location.x && ws.y == location.y && ws.z == location.z) {
                return true
            }
        }

        return false
    }
}