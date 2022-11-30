import java.util.Random;
import java.util.Vector;

class Cell {
    enum CellType {
        EMPTY,
        ENEMY,
        SHOP,
        FINISH
    }
    int x, y;
    CellType type;
    CellElement celem;
    boolean visited;

    public Cell(int x, int y, boolean visited, CellType type, CellElement c) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.visited = visited;
        this.celem = c;
    }

    public void setVisited(boolean set) { visited = set; }
}

interface CellElement {
    char toCharacter();
}

class InvalidCommandException extends Exception {
    public InvalidCommandException(String errorMessage) {
        super(errorMessage);
    }
}

class Shop implements CellElement {
    Vector<Potion> potions;

    public Shop (boolean setShop) {
        if (!setShop) {
            potions = new Vector<Potion>();
            Random rd = new Random();
            int nr_pots = rd.nextInt(3) + 2;
            for (int i = 0; i < nr_pots; i++) {
                int pot = rd.nextInt(2);
                if (pot == 0)
                    potions.add(new HealthPotion());
                else potions.add(new ManaPotion());
            }
        } else {
            potions = new Vector<Potion>();
            potions.add(new HealthPotion());
            potions.add(new ManaPotion());
            potions.add(new HealthPotion());
        }
    }

    public char toCharacter() { return 'S'; }

    public Potion getPotion(int index) throws InvalidCommandException {
        if (index < 0 || index > potions.size() - 1)
            throw new InvalidCommandException("Wrong Choice");
        Potion pot = potions.get(index);
        potions.remove(index);
        return pot;
    }

    public void printPotions() {
        System.out.println("=================\n0 ----------- Exit\nBuy a Potion:");
        int number = 1;
        for (Potion p : potions)
            System.out.println(number++ + " ----------- " + p.toString());
        System.out.println("=================");
    }
}

class Empty implements CellElement {
    public char toCharacter() { return 'N'; }
}

class Finish implements CellElement {
    public char toCharacter() { return 'F'; }
}