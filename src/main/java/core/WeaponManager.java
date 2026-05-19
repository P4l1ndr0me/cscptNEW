package core;

import entities.Weapon;
import java.util.*;
import java.io.*;

public class WeaponManager {
    public static HashMap<String, Weapon> weapons = new HashMap<>();
    public static ArrayList<Weapon> allWeapons = new ArrayList<>();
    public static HashMap<String, Integer> unlockedWeapons = new HashMap<>();

    public static void init(){

        // init all pickaxes
        // PICKAXES
        allWeapons.add(new Weapon("stone_pickaxe", "Stone Pickaxe", "stonepickaxe",
                100, 15, 1.0f, "Basic Mining Tool", 100, 1, "pickaxe"));

        allWeapons.add(new Weapon("iron_pickaxe", "Iron Pickaxe", "ironpickaxe",
                300, 20, 1.5f, "Basic Mining Tool", 120, 2, "pickaxe"));

        allWeapons.add(new Weapon("diamond_pickaxe", "Diamond Pickaxe", "diamondpickaxe",
                500, 25, 2.0f, "Basic Mining Tool", 150, 3, "pickaxe"));


        // SWORDS
        allWeapons.add(new Weapon("wooden_sword", "Wooden Sword", "woodensword",
                100, 30, 1.5f, "Melee Tool", 0, 1, "sword"));

        allWeapons.add(new Weapon("stone_sword", "Stone Sword", "stonesword",
                600, 40, 1.5f, "Melee Tool", 0, 2, "sword"));

        allWeapons.add(new Weapon("diamond_sword", "Diamond Sword", "diamondsword",
                800, 40, 1.5f, "Melee Tool", 0, 3, "sword"));


        // BOWS
        allWeapons.add(new Weapon("wooden_bow", "Wooden Bow", "woodenbow",
                300, 30, 2.0f, "range weapon", 0, 1, "bow"));

        allWeapons.add(new Weapon("cross_bow", "Cross Bow", "crossbow",
                1000, 60, 3.0f, "range weapon", 0, 2, "bow"));

        allWeapons.add(new Weapon("stone_bow", "Stone Bow", "stonebow",
                500, 40, 1.5f, "range weapon", 0, 3, "bow"));

        unlockedWeapons.put("pickaxe", 1);
        unlockedWeapons.put("sword", 1);
        unlockedWeapons.put("bow", 1);


        // Put in map for easy lookup
        for (Weapon w : allWeapons) {
            weapons.put(w.getId(), w);
        }
    }
    public static Weapon getWeapon(String id) {
        return weapons.get(id);
    }

    // Progression methods
    public static int getCurrentTier(String weaponType) {
        return unlockedWeapons.getOrDefault(weaponType, 1);
    }

    public static void unlockNextTier(String weaponType) {
        int currentTier = getCurrentTier(weaponType);
        unlockedWeapons.put(weaponType, currentTier + 1);
        //System.out.println(weaponType + " upgraded to tier " + (currentTier + 1));
    }

    public static boolean isMaxTier(String weaponType) {
        int currentTier = getCurrentTier(weaponType);
        return getWeaponByTypeAndTier(weaponType, currentTier + 1) == null;
    }

    public static Weapon getCurrentWeapon(String weaponType) {
        int currentTier = getCurrentTier(weaponType);
        return getWeaponByTypeAndTier(weaponType, currentTier);
    }

    public static Weapon getNextWeapon(String weaponType) {
        int currentTier = getCurrentTier(weaponType);
        return getWeaponByTypeAndTier(weaponType, currentTier + 1);
    }

    public static Weapon getWeaponByTypeAndTier(String type, int tier) {
        for (Weapon w : allWeapons) {
            if (w.getType().equals(type) && w.getTier() == tier) {
                return w;
            }
        }
        return null;
    }
}
