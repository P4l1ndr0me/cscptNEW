package systems;

import core.WeaponManager;
import entities.Player;
import entities.Weapon;

public class PurchaseSystem {

    public static boolean purchaseWeapon(String weaponType, Weapon weapon) {
        int cost = weapon.getCost();

        if (Player.numGold < cost) {
            return false;
        }

        // Deduct gold
        Player.numGold -= cost;

        // Advance shop progression (so next tier appears)
        WeaponManager.unlockNextTier(weaponType);

        // Upgrade player's stats and texture based on the weapon's properties
        switch (weaponType) {
            case "pickaxe":
                Player.upgradePickaxe(weapon);
                break;
            case "sword":
                Player.upgradeSword(weapon);
                break;
            case "bow":
                Player.upgradeBow(weapon);
                break;
        }

        return true;
    }
}