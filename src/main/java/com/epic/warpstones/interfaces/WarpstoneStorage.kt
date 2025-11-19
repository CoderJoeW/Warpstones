package com.epic.warpstones.interfaces

import com.epic.warpstones.models.Warpstone

interface WarpstoneStorage {
    fun loadWarpstones(): MutableList<Warpstone>
    fun saveWarpstones(warpstones: MutableList<Warpstone>)
}