package buildings;

import static com.raylib.Raylib.*;

public class ArrowTower extends Building {
    public ArrowTower(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public int getUpgradeStoneCost() {
        return switch (level) {
            case 1 -> 120;
            case 2 -> 240;
            default -> 0;
        };
    }

    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 80;
            case 2 -> 200;
            default -> 0;
        };
    }
}
