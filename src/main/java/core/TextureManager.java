package core;

import java.util.HashMap;
import static com.raylib.Raylib.*;

public class TextureManager {
    private static HashMap<String, Texture> textures = new HashMap<>();

    public static void loadTexture(String key, String path) {
        Texture tex = LoadTexture(path);
        SetTextureFilter(tex, TEXTURE_FILTER_POINT);
        textures.putIfAbsent(key, tex);
    }

    public static Texture getTexture(String key) {
        return textures.get(key);
    }

    public static void unloadAll() {
        for (Texture tex : textures.values()) {
            UnloadTexture(tex);
        }
        textures.clear();
    }

    public static void init(){
        // Load all textures

        // Map-related
        TextureManager.loadTexture("background", "src/main/assets/images/map/bg.png");
        TextureManager.loadTexture("stone", "src/main/assets/images/map/stone.png");

        // Player-related
        TextureManager.loadTexture("mining", "src/main/assets/images/player/mining_sprites.png");
        TextureManager.loadTexture("playerNEW", "src/main/assets/images/player/player_sprites.png");

        // Building-related
        TextureManager.loadTexture("Gold Stash", "src/main/assets/images/buildings/goldstash.png");
        TextureManager.loadTexture("Gold Mine", "src/main/assets/images/buildings/goldmine.png");
        TextureManager.loadTexture("Cannon Tower", "src/main/assets/images/buildings/building2.png");
        TextureManager.loadTexture("Arrow Tower", "src/main/assets/images/buildings/building3.png");

        // Enemy-related
        TextureManager.loadTexture("enemy1", "src/main/assets/images/sprites/ZOMBIE1.png");
        TextureManager.loadTexture("enemy2", "src/main/assets/images/sprites/redZombie.png");

        //weapons
        TextureManager.loadTexture("stonepickaxe","src/main/assets/images/items/stonePick.png");
        TextureManager.loadTexture("ironpickaxe", "src/main/assets/images/items/ironPick.png");
        TextureManager.loadTexture("diamondpickaxe", "src/main/assets/images/items/diamondPick.png");
        TextureManager.loadTexture("woodensword","src/main/assets/images/items/woodenSword.png");
        TextureManager.loadTexture("stonesword", "src/main/assets/images/items/stoneSword.png");
        TextureManager.loadTexture("diamondsword", "src/main/assets/images/items/diamondSword.png");
        TextureManager.loadTexture("woodenbow","src/main/assets/images/items/woodenBow.png");
        TextureManager.loadTexture("crossbow", "src/main/assets/images/items/crossBow.png");
        TextureManager.loadTexture("stonebow", "src/main/assets/images/items/stoneBow.png");
    }
}
