import java.util.Random;
import java.util.Vector;

interface Element <T extends Entity> {
    void accept(Visitor<T> visitor);
}

abstract class Entity implements Element<Entity> {
    Vector<Spell> spells;
    int current_hp, max_hp, current_mana, max_mana;
    boolean fire, ice, earth;

    public Entity(boolean fire, boolean earth, boolean ice) {
        spells = new Vector<Spell>();
        current_hp = max_hp = current_mana = max_mana = 0;
        this.fire = fire;
        this.ice = ice;
        this.earth = earth;
    }

    public void regenHP(int amount) {
        current_hp += amount;
        if (current_hp > max_hp)
            current_hp = max_hp;
    }

    public void regenMana(int amount) {
        current_mana += amount;
        if (current_mana > max_mana)
            current_mana = max_mana;
    }

    public boolean use_ability(Spell ability, Entity e) {
        if (current_mana >= ability.mana_cost) {
            current_mana -= ability.mana_cost;
            spells.remove(ability);
            e.accept(ability);
            return true;
        }
        else if (e instanceof Enemy) {
            e.receiveDamage(this.getDamage());
            System.out.println("****Insufficient Mana...Used Normal Attack****\n");
            return  false;
        }
        return false;
    }

    abstract public void receiveDamage(int amount);
    abstract public int getDamage();
}

//Sablon FACTORY
class CharacterFactory {
    public static Character factory(String type, String name, int experience, int level) {
        if (type.equalsIgnoreCase("WARRIOR"))
            return new Warrior(name, experience, level);
        if (type.equalsIgnoreCase("ROGUE"))
            return new Rogue(name, experience, level);
        if (type.equalsIgnoreCase("MAGE"))
            return new Mage(name, experience, level);
        return null;
    }
}

abstract class Character extends Entity {
    String name;
    int x, y;
    Inventory inventory;
    int experience, level;
    int strenght, charisma, dexterity;

    public Character(int hp, int mana, boolean fire, boolean earth, boolean ice,
                     String name, int x, int y, int experience, int level, int weight,
                     int strenght, int charisma, int dexterity) {
        super(fire, earth, ice);
        spells.add(new Earth());
        spells.add(new Fire());
        spells.add(new Ice());
        inventory = new Inventory(weight);
        this.name = name;
        this.x = x;
        this.y = y;
        this.experience = experience;
        this.level = level;
        int multiplier = level/5;
        if (multiplier == 0)
            multiplier = 1;
        current_hp = max_hp = hp * multiplier;
        current_mana = max_mana = mana * multiplier;
        this.strenght = strenght * multiplier;
        this.charisma = charisma * multiplier;
        this.dexterity = dexterity * multiplier;
    }

    public boolean buyPotion(Potion pot) {
        if (inventory.coins >= pot.getPrice() && inventory.getWeightLeft() >= pot.getWeight()) {
            inventory.addPotion(pot);
            inventory.coins -= pot.getPrice();
            System.out.println("****" + pot.toString() + " Acquired****");
            return true;
        } else {
            System.out.println("****Not Enough Coins/Space for Potion****");
            return false;
        }
    }

    //Adauga coins in inventarul caracterului
    public void winCoins(String winType) {
        Random rd = new Random();
        if (winType.equalsIgnoreCase("ENEMY")) {    //infreangerea unui inamic
            if (Math.random() < 0.2)
                inventory.addCoins(rd.nextInt(10) + 5);
        }
        else if (winType.equalsIgnoreCase("EMPTYCELL")) {   //degeaba
            if (Math.random() < 0.8)
                inventory.addCoins(rd.nextInt(10) + 20);
        }
    }

    //Adauga coins in inventarul caracterului
    public void winExperience(String winType) {
        if (winType.equalsIgnoreCase("ENEMY"))  //infreangerea unui inamic
            experience += 50;
        else if (winType.equalsIgnoreCase("EMPTYCELL"))     //degeaba
            experience += 5;

        if (experience >= 100) {
            level++;
            experience -= 100;
        }
    }
}

class Inventory {
    Vector <Potion> potions;
    int max_weight;
    int coins;

    public Inventory(int weight) {
        potions = new Vector<Potion>();
        max_weight = weight;
        coins = 20;
    }

    public void addPotion(Potion newPotion) {
        potions.add(newPotion);
    }

    public void removePotion(Potion remPotion) {
        potions.remove(remPotion);
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public int getWeightLeft() {
        int weight = max_weight;
        for (Potion p : potions)
            weight -= p.getWeight();
        return weight;
    }
}

class Warrior extends Character {
    public Warrior(String name, int experience, int level) {
        super(1000, 100, true, false, false, name, 0 ,0, experience, level, 18, 10, 8, 5);
        //Vector <Spell> spells = new Vector<Spell>();
    }

    public void receiveDamage(int amount) {
        double chance = (charisma + dexterity) / 100d;
        int damage = amount - charisma/5 - dexterity/5;
        double rand = Math.random();
        if (rand < chance)
            damage = damage / 2;
        current_hp -= damage;
        if (current_hp < 0)
            current_hp = 0;
    }

    public int getDamage() {
        double chance = strenght / 100d;
        int damage = strenght;
        double rand = Math.random();
        if (rand < chance)
            damage = damage * 2;
        return damage;
    }

    @Override
    public void accept(Visitor<Entity> visitor) {
        visitor.visit(this);
    }
}

class Rogue extends Character {
    public Rogue(String name, int experience, int level) {
        super(1000, 100, false, true, false, name, 0 ,0, experience, level, 12, 8, 5, 10);
        //Vector <Spell> spells = new Vector<Spell>();
    }

    public void receiveDamage(int amount) {
        double chance = (charisma + strenght) / 100d;
        int damage = amount - charisma/5 - strenght/5;
        double rand = Math.random();
        if (rand < chance)
            damage = damage / 2;
        current_hp -= damage;
        if (current_hp < 0)
            current_hp = 0;
    }

    public int getDamage() {
        double chance = dexterity / 100d;
        int damage = dexterity;
        double rand = Math.random();
        if (rand < chance)
            damage = damage * 2;
        return damage;
    }

    @Override
    public void accept(Visitor<Entity> visitor) {
        visitor.visit(this);
    }
}

class Mage extends Character {
    public Mage(String name, int experience, int level) {
        super(1000, 100, false, false, true, name, 0 ,0, experience, level, 6, 5 ,10, 8);
        //Vector <Spell> spells = new Vector<Spell>();
    }

    public void receiveDamage(int amount) {
        double chance = (strenght + dexterity) / 100d;
        int damage = amount - strenght/5 - dexterity/5;
        double rand = Math.random();
        if (rand < chance)
            damage = damage / 2;
        current_hp -= damage;
        if (current_hp < 0)
            current_hp = 0;
    }

    public int getDamage() {
        double chance = charisma / 100d;
        int damage = charisma;
        double rand = Math.random();
        if (rand < chance)
            damage = damage * 2;
        return damage;
    }

    @Override
    public void accept(Visitor<Entity> visitor) {
        visitor.visit(this);
    }
}

class Enemy extends Entity implements CellElement {

    public Enemy() {
        super(false, false, false);
        Random rd = new Random();
        int nr_spells = rd.nextInt(3) + 2;
        for (int i = 0; i < nr_spells; i++) {
            int spll = rd.nextInt(3);
            if (spll == 0)
                spells.add(new Fire());
            else if (spll == 1)
                spells.add(new Ice());
            else spells.add(new Earth());
        }
        max_hp = current_hp = rd.nextInt(90 - 70) + 70;
        max_mana = current_mana = rd.nextInt(50 - 30) + 30;
        fire = rd.nextBoolean();
        earth = rd.nextBoolean();
        ice = rd.nextBoolean();
    }

    public char toCharacter() { return 'E'; }

    public void receiveDamage(int amount) {
        if (Math.random() < 0.5)
            amount = 0;
        current_hp -= amount;
        if (current_hp < 0)
            current_hp = 0;
    }

    public int getDamage() {
        int damage = 10;
        if (Math.random() < 0.5)
            damage *= 2;
        return damage;
    }

    @Override
    public void accept(Visitor<Entity> visitor) {
        visitor.visit(this);
    }
}
