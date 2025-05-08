package main.java;

import arc.util.Nullable;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class World extends mindustry.core.World {
    Tiles tiles = new Tiles(512, 512);
    public World() {
        super();
    }
    @Override
    @Nullable
    public Tile tile(int x, int y){
        return tiles.get(x, y);
    }
}
