package entities;

public class Weapon {
    private String id;
    private String name;
    private String textureName;
    private int cost;
    private int damage;
    private float attackSpeed;
    private String description;
    private int efficiency;
    private int tier;
    private String type;

    public Weapon(String id, String name, String textureName, int cost, int damage, float attackSpeed, String description, int efficiency, int tier, String type) {
        this.id = id;
        this.name = name;
        this.textureName = textureName;
        this.cost = cost;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.description = description;
        this.efficiency = efficiency;
        this.tier = tier;
        this.type = type;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getTextureName() { return textureName; }
    public int getCost() { return cost; }
    public int getDamage() { return damage; }
    public float getAttackSpeed() { return attackSpeed; }
    public String getDescription() { return description; }
    public int getTier() { return tier; }
    public String getType() { return type; }
    public int getEfficiency(){return efficiency;}

    // Setters (if needed)
    public void setCost(int cost) { this.cost = cost; }
    public void setDamage(int damage) { this.damage = damage; }
}
