interface Potion {
    void usePotion(Character e);
    int getPrice();
    int getRegenAmount();
    int getWeight();
}

class HealthPotion implements Potion {
    private final int price = 15;
    private final int weight = 2;
    private final int regen_amount = 30;

    public void usePotion(Character e) {
        e.regenHP(regen_amount);
        e.inventory.removePotion(this);
        System.out.println("Used Health Potion");
    }

    public int getPrice() {
        return price;
    }

    public int getRegenAmount() {
        return regen_amount;
    }

    public String toString() {
        return "Health Potion";
    }

    public int getWeight() {
        return weight;
    }
}

class ManaPotion implements Potion {
    private final int price = 10;
    private final int weight = 3;
    private final int regen_amount = 30;

    public void usePotion(Character e) {
        e.regenMana(regen_amount);
        e.inventory.removePotion(this);
        System.out.println("Used Mana Potion");
    }

    public int getPrice() {
        return price;
    }

    public int getRegenAmount() {
        return regen_amount;
    }

    public int getWeight() {
        return weight;
    }

    public String toString() {
        return "Mana Potion";
    }
}
