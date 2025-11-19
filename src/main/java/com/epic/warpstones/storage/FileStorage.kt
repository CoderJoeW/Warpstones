package com.epic.warpstones.storage

import com.epic.warpstones.Warpstones
import com.epic.warpstones.interfaces.WarpstoneStorage
import com.epic.warpstones.models.Warpstone
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

private const val DATABASE_FILE_NAME = "warpstones.dat"

class FileStorage: WarpstoneStorage {
    private val plugin: JavaPlugin
    private val database: File
    private val databaseAbsolutePath: Path

    constructor() {
        this.plugin = JavaPlugin.getPlugin(Warpstones::class.java)

        val dataFolder = this.plugin.dataFolder

        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        this.database = File(dataFolder, DATABASE_FILE_NAME)

        if (!this.database.exists()) {
            this.database.createNewFile()
        }

        this.databaseAbsolutePath = Paths.get(this.database.absolutePath)
    }

    override fun loadWarpstones(): ArrayList<Warpstone> {
        val warpstones = ArrayList<Warpstone>()

        try {
            val lines = Files.readAllLines(this.databaseAbsolutePath)

            for (line in lines) {
                val warpstone = Warpstone()
                val parts = line.split('|')

                warpstone.x = parts[0].toDouble()
                warpstone.y = parts[1].toDouble()
                warpstone.z = parts[2].toDouble()
                warpstone.name = parts[3]
                warpstone.destination = parts[4]
                warpstone.owner = UUID.fromString(parts[5])
                warpstones.add(warpstone)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not load warpstones", e)
        }

        println("Loaded ${warpstones.size} warpstones")
        println(warpstones.toString())

        return warpstones
    }

    override fun saveWarpstones(warpstones: ArrayList<Warpstone>) {
        try {
            val builder = StringBuilder()

            for (warpstone in warpstones) {
                builder.append("${warpstone.x}|${warpstone.y}|${warpstone.z}|")
                builder.append("${warpstone.name}|${warpstone.destination}|${warpstone.owner.toString()}")
                builder.append("\n")
            }

            builder.trimEnd()

            Files.writeString(this.databaseAbsolutePath, builder.toString())
        } catch(e: IOException) {
            throw RuntimeException("Could not save warpstones", e)
        }
    }

}