package com.epic.warpstones.models;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class WarpstoneSign {
    public String title;
    public String name;
    public int id;

    public WarpstoneSign(Sign sign) {
        this.title = getContent(sign, 0);
        this.name = getContent(sign, 1);

        String idLine = getContent(sign, 2);

        if (idLine.length() > 0) {
            this.id = Integer.parseInt(idLine.split(" ")[1]);
        }
    }

    public boolean isValidWarpstoneSign() {
        if (this.title.equals("Warpstone")) {
            return true;
        }

        return false;
    }

    private String getContent(Sign sign, int index) {
        return ((TextComponent)sign.getSide(Side.FRONT).lines().get(index)).content();
    }
}
