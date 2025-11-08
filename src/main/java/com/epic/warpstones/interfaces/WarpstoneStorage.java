package com.epic.warpstones.interfaces;

import com.epic.warpstones.models.Warpstone;

import java.util.List;

public interface WarpstoneStorage {
    List<Warpstone> loadWarpstones();
    void saveWarpstones(List<Warpstone> warpstones);
}
