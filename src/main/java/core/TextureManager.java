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
}
