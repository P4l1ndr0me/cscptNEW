package core;

import java.util.HashMap;
import static com.raylib.Raylib.*;

public class TextureManager {
    private static final HashMap<String, Texture> textures = new HashMap<>();

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
        TextureManager.loadTexture("mining1", "src/main/assets/images/player/mining1.png");
        TextureManager.loadTexture("mining2", "src/main/assets/images/player/mining2.png");
        TextureManager.loadTexture("mining3", "src/main/assets/images/player/mining3.png");
        TextureManager.loadTexture("mining4", "src/main/assets/images/player/mining4.png");
        TextureManager.loadTexture("sword1", "src/main/assets/images/player/sword1.png");
        TextureManager.loadTexture("sword2", "src/main/assets/images/player/sword2.png");
        TextureManager.loadTexture("sword3", "src/main/assets/images/player/sword3.png");
        TextureManager.loadTexture("playerNEW", "src/main/assets/images/player/player_sprites.png");

        // Building-related

        // Goldstash
        TextureManager.loadTexture("Gold Stash 1", "src/main/assets/images/buildings/goldstash/goldstash_lvl1.png");
        TextureManager.loadTexture("Gold Stash 2", "src/main/assets/images/buildings/goldstash/goldstash_lvl2.png");
        TextureManager.loadTexture("Gold Stash 3", "src/main/assets/images/buildings/goldstash/goldstash_lvl3.png");

        // Goldmine
        TextureManager.loadTexture("Gold Mine 1", "src/main/assets/images/buildings/goldmine/goldmine_lvl1.png");
        TextureManager.loadTexture("Gold Mine 2", "src/main/assets/images/buildings/goldmine/goldmine_lvl2.png");
        TextureManager.loadTexture("Gold Mine 3", "src/main/assets/images/buildings/goldmine/goldmine_lvl3.png");

        TextureManager.loadTexture("Tower Base", "src/main/assets/images/buildings/towerbase.png");

        // Cannon
        TextureManager.loadTexture("Cannon Tower 1", "src/main/assets/images/buildings/cannontower/cannon_tower_lvl1.png");
        TextureManager.loadTexture("Cannon Tower 2", "src/main/assets/images/buildings/cannontower/cannon_tower_lvl2.png");
        TextureManager.loadTexture("Cannon Tower 3", "src/main/assets/images/buildings/cannontower/cannon_tower_lvl3.png");
        TextureManager.loadTexture("Cannon Tower Combined", "src/main/assets/images/buildings/cannontower/cannon_combined.png");
        TextureManager.loadTexture("Cannon Bullet", "src/main/assets/images/buildings/cannontower/cannon_bullet.png");

        // Arrow
        TextureManager.loadTexture("Arrow Tower 1", "src/main/assets/images/buildings/arrowtower/arrow_tower_lvl1.png");
        TextureManager.loadTexture("Arrow Tower 2", "src/main/assets/images/buildings/arrowtower/arrow_tower_lvl2.png");
        TextureManager.loadTexture("Arrow Tower 3", "src/main/assets/images/buildings/arrowtower/arrow_tower_lvl3.png");
        TextureManager.loadTexture("Arrow Tower Combined", "src/main/assets/images/buildings/arrowtower/arrow_combined.png");
        TextureManager.loadTexture("Arrow Bullet", "src/main/assets/images/buildings/arrowtower/arrow_bullet.png");

        // Enemy-related
        TextureManager.loadTexture("Zombie Tier 1", "src/main/assets/images/zombie/zombie_tier1.png");
        TextureManager.loadTexture("Zombie Tier 2", "src/main/assets/images/zombie/zombie_tier2.png");
        TextureManager.loadTexture("Zombie Tier 3", "src/main/assets/images/zombie/zombie_tier3.png");
        TextureManager.loadTexture("Zombie Tier 4", "src/main/assets/images/zombie/zombie_tier4.png");

        //weapons
        TextureManager.loadTexture("stonepickaxe","src/main/assets/images/items/ironPick.png");
        TextureManager.loadTexture("ironpickaxe", "src/main/assets/images/items/stonePick.png");
        TextureManager.loadTexture("goldpickaxe", "src/main/assets/images/items/goldPick.png");
        TextureManager.loadTexture("diamondpickaxe", "src/main/assets/images/items/diamondPick.png");

        TextureManager.loadTexture("goldsword","src/main/assets/images/items/goldSword.png");
        TextureManager.loadTexture("woodensword", "src/main/assets/images/items/woodenSword.png");
        TextureManager.loadTexture("diamondsword", "src/main/assets/images/items/diamondSword.png");
        TextureManager.loadTexture("woodenbow","src/main/assets/images/items/woodenBow.png");
        TextureManager.loadTexture("crossbow", "src/main/assets/images/items/crossBow.png");
        TextureManager.loadTexture("stonebow", "src/main/assets/images/items/stoneBow.png");

        //attacking motions
        TextureManager.loadTexture("attackWest", "src/main/assets/images/player/attackWest.png");
        TextureManager.loadTexture("attackEast", "src/main/assets/images/player/attackEast.png");
    }
}
