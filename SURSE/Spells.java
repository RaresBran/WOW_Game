interface Visitor <T extends Entity> {
    void visit(T entity);
}

abstract class Spell implements Visitor<Entity> {
    int damage;
    int mana_cost;
}

class Fire extends Spell {
    public Fire() {
        damage = 20;
        mana_cost = 15;
    }

    public String toString() {
        return "Fire Ability";
    }

    @Override
    public void visit(Entity entity) {
        if (entity.fire && entity instanceof Enemy)
            System.out.println("****Fire Spell Blocked****\n");
        else  {
            entity.receiveDamage(damage);
            if (entity instanceof Enemy)
                System.out.println("****Fire Spell Used****\n");
        }
    }
}

class Ice extends Spell {
    public Ice() {
        damage = 15;
        mana_cost = 10;
    }

    public String toString() {
        return "Ice Ability";
    }

    @Override
    public void visit(Entity entity) {
        if (entity.ice && entity instanceof Enemy)
            System.out.println("****Ice Spell Blocked****\n");
        else {
            entity.receiveDamage(damage);
            if (entity instanceof Enemy)
                System.out.println("****Ice Spell Used****\n");
        }
    }
}

class Earth extends Spell {
    public Earth() {
        damage = 25;
        mana_cost = 20;
    }

    public String toString() {
        return "Earth Ability";
    }

    @Override
    public void visit(Entity entity) {
        if (entity.earth && entity instanceof Enemy)
            System.out.println("****Earth Spell Blocked****\n");
        else {
            entity.receiveDamage(damage);
            if (entity instanceof Enemy)
                System.out.println("****Earth Spell Used****\n");
        }
    }
}