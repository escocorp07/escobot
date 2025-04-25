package main.java.bot;

import arc.files.Fi;
import arc.graphics.Pixmap;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import static mindustry.io.MapIO.colorFor;

public class utils {
    /**Получить рендер карт с цветом предмета в конфиге блока(сортер и т.д.)
     * @param tiles Тайлы карты
     * @return Pixmap для сохранения в файл
     * */
    public static Pixmap generatePreview(Tiles tiles){
        Pixmap pixmap = new Pixmap(tiles.width, tiles.height);
        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                Tile tile = tiles.getn(x, y);
                if(tile.build != null) {
                    if(tile.build.config() instanceof Item) {
                        Item item = (Item) tile.build.config();
                        pixmap.set(x, pixmap.height - 1 - y, item.color.rgba());
                        item = null;
                    } else {
                        pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                    }
                } else {
                    pixmap.set(x, pixmap.height - 1 - y, colorFor(tile.block(), tile.floor(), tile.overlay(), tile.team()));
                }
            }
        }
        return pixmap;
    }
    public static Map getMap(Fi file) {
        try{
            return MapIO.createMap(file, true);
        } catch (Exception e) {
            errorLogger.logErr(e);
            return null;
        }
    }
}
