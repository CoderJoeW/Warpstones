package com.epic.warpstones.interfaces

import com.epic.warpstones.models.Warpstone

interface WarpstoneStorage {
    fun loadWarpstones(): ArrayList<Warpstone>
    fun saveWarpstones(warpstones: ArrayList<Warpstone>)
}