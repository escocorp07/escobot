package main.java;

import mindustry.game.Team;
import mindustry.io.MapIO;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import arc.graphics.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.maps.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import java.io.*;
import java.util.zip.*;

import static mindustry.Vars.*;

/**Custom MapIO*/
public class BMapIo extends MapIO {
    /**Get color for. Returns rgba*/
    public static int colorFor(Block wall, Block floor, Block overlay, Team team, Object buildConfig) {
        if (buildConfig instanceof Item) {
            Item item = (Item) buildConfig;
            return item.color.rgba();
        }
        if(wall.synthetic()){
            return team.color.rgba();
        }
        if(((Floor) overlay).wallOre){
            return overlay.mapColor.rgba();
        } else {
            if(wall.solid){
                return wall.mapColor.rgba();
            } else {
                if(!overlay.useColor){
                    return floor.mapColor.rgba();
                } else {
                    return overlay.mapColor.rgba();
                }
            }
        }
    }
    public static Pixmap generatePreview(Map map) throws IOException{
        map.spawns = 0;
        map.teams.clear();

        try(InputStream is = new InflaterInputStream(map.file.read(bufferSize)); CounterInputStream counter = new CounterInputStream(is); DataInputStream stream = new DataInputStream(counter)){
            SaveIO.readHeader(stream);
            int version = stream.readInt();
            SaveVersion ver = SaveIO.getSaveWriter(version);
            ver.region("meta", stream, counter, ver::readStringMap);

            Pixmap floors = new Pixmap(map.width, map.height);
            Pixmap walls = new Pixmap(map.width, map.height);
            int black = 255;
            int shade = Color.rgba8888(0f, 0f, 0f, 0.5f);
            CachedTile tile = new CachedTile(){
                @Override
                public void setBlock(Block type){
                    super.setBlock(type);

                    int c = BMapIo.colorFor(block(), Blocks.air, Blocks.air, team());
                    if(c != black){
                        walls.setRaw(x, floors.height - 1 - y, c);
                        floors.set(x, floors.height - 1 - y + 1, shade);
                    }
                }
            };

            ver.region("content", stream, counter, ver::readContentHeader);
            ver.region("preview_map", stream, counter, in -> ver.readMap(in, new WorldContext(){
                @Override public void resize(int width, int height){}
                @Override public boolean isGenerating(){return false;}
                @Override public void begin(){
                    world.setGenerating(true);
                }
                @Override public void end(){
                    world.setGenerating(false);
                }

                @Override
                public void onReadBuilding(){
                    //read team colors
                    if(tile.build != null){
                        int c = tile.build.team.color.rgba8888();
                        int size = tile.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for(int dx = 0; dx < size; dx++){
                            for(int dy = 0; dy < size; dy++){
                                int drawx = tile.x + dx + offsetx, drawy = tile.y + dy + offsety;
                                walls.set(drawx, floors.height - 1 - drawy, c);
                            }
                        }

                        if(tile.build.block instanceof CoreBlock){
                            map.teams.add(tile.build.team.id);
                        }
                    }
                }

                @Override
                public Tile tile(int index){
                    tile.x = (short)(index % map.width);
                    tile.y = (short)(index / map.width);
                    return tile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID){
                    if(overlayID != 0){
                        floors.set(x, floors.height - 1 - y, BMapIo.colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict));
                    }else{
                        floors.set(x, floors.height - 1 - y, BMapIo.colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict));
                    }
                    if(content.block(overlayID) == Blocks.spawn){
                        map.spawns ++;
                    }
                    return tile;
                }
            }));

            floors.draw(walls, true);
            walls.dispose();
            return floors;
        }finally{
            content.setTemporaryMapper(null);
        }
    }
}
