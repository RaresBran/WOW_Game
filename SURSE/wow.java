import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.*;
import org.json.*;

class Game {
    Vector <Account> accounts;
    HashMap <Cell.CellType, Vector <String>> story_map;

    private static Game instance = null;
    private Game() {
        accounts = new Vector <Account> ();
        story_map = new HashMap <Cell.CellType, Vector <String>>();
        getAccounts();
        getStories();
    }

    public static Game getInstance() {
        if (instance == null)
            instance = new Game();
        return instance;
    }

    //citeste json-ul accounts
    private void getAccounts() {
        String file;
        try {
            file = Files.readString(Path.of(".\\src\\accounts.json"));
            JSONObject content = new JSONObject(file);
            JSONArray accountArray = content.getJSONArray("accounts");
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject account_json = accountArray.getJSONObject(i);
                Account acc = new Account(account_json);
                accounts.add(acc);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //citeste json-ul stories
    private void getStories() {
        String file;
        try {
            file = Files.readString(Path.of(".\\src\\stories.json"));
            JSONObject content = new JSONObject(file);
            JSONArray storyArray = content.getJSONArray("stories");
            Vector <Vector<String>> stories = new Vector <Vector<String>>();
            for (int i = 0; i < 4; i++)
                stories.add(i, new Vector<String>());
            for (int i = 0; i < storyArray.length(); i++) {
                JSONObject story = storyArray.getJSONObject(i);
                if (story.getString("type").equalsIgnoreCase("EMPTY"))
                    stories.get(0).add(story.getString("value"));
                else if (story.getString("type").equalsIgnoreCase("ENEMY"))
                    stories.get(1).add(story.getString("value"));
                else if (story.getString("type").equalsIgnoreCase("SHOP"))
                    stories.get(2).add(story.getString("value"));
                else if (story.getString("type").equalsIgnoreCase("FINISH"))
                    stories.get(3).add(story.getString("value"));
            }
            story_map.put(Cell.CellType.EMPTY, stories.get(0));
            story_map.put(Cell.CellType.ENEMY, stories.get(1));
            story_map.put(Cell.CellType.SHOP, stories.get(2));
            story_map.put(Cell.CellType.FINISH, stories.get(3));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        int choice = chooseGameType();

        //HARDOCODED
        if ( choice == 0 ) {
            Scanner in = new Scanner(System.in);
            Account loginAccount = chooseAccount();
            Character chosen_character = chooseCharacter(loginAccount);

            Grid game_map = Grid.generateHCGrid(5, 5, chosen_character);
            game_map.current_char.inventory.addCoins(100);

            getStory(game_map, story_map);
            game_map.printMap();
            game_map.current_cell.setVisited(true);

            for (int steps = 8; steps > 0; steps--) {
                String read = in.next();

                if (game_map.current_cell.type == Cell.CellType.EMPTY)
                    game_map.current_char.winCoins("EMPTY");

                if ((read.equalsIgnoreCase("P") && steps > 4)) {
                    game_map.goEast();
                    getStory(game_map, story_map);
                    game_map.printMap();
                }

                if (game_map.current_cell.type == Cell.CellType.SHOP) {
                    Shop thisShop;
                    thisShop = (Shop) game_map.current_cell.celem;
                    System.out.println("Coins: " + game_map.current_char.inventory.coins);
                    thisShop.printPotions();
                    if (in.next().equalsIgnoreCase("P")) {
                        try {
                            game_map.current_char.buyPotion(thisShop.potions.get(0));
                            game_map.current_char.buyPotion(thisShop.potions.get(1));
                            thisShop.getPotion(0);
                            thisShop.getPotion(0);
                        } catch (InvalidCommandException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }

                else if (game_map.current_cell.type == Cell.CellType.ENEMY) {
                    Enemy e;
                    e = (Enemy) game_map.current_cell.celem;
                    while (game_map.current_char.spells.size() > 0 && e.current_hp > 0)
                        if (enemyChoice(game_map) == 0 && chooseAbility(game_map) == 0) {
                            game_map.current_char.use_ability(game_map.current_char.spells.get(0), e);
                            enemyTurn(game_map, e);
                        }
                    while (game_map.current_char.inventory.potions.size() > 0 && e.current_hp > 0)
                        if (enemyChoice(game_map) == 0 && choosePotion(game_map) == 0) {
                            game_map.current_char.inventory.potions.get(0).usePotion(chosen_character);
                            enemyTurn(game_map, e);
                        }
                    while (e.current_hp > 0) {
                        if (enemyChoice(game_map) == 0) {
                            System.out.println("****Normal Attack****\n");
                            e.receiveDamage(game_map.current_char.getDamage());
                            enemyTurn(game_map, e);
                        }
                    }
                    System.out.println("****Enemy Defeated****");
                    game_map.current_char.winCoins("ENEMY");
                    if (in.next().equalsIgnoreCase("P")) {
                        game_map.goSouth();
                        getStory(game_map, story_map);
                        game_map.printMap();
                    }

                }

                else if (read.equalsIgnoreCase("P") && steps <= 4) {
                    game_map.goSouth();
                    getStory(game_map, story_map);
                    game_map.printMap();
                }
                game_map.current_cell.setVisited(true);
            }
        }

        //CLI
        else if ( choice == 1 ) {
            Account loginAccount = chooseAccount();
            Character chosen_character = chooseCharacter(loginAccount);

            Grid game_map = Grid.generateRandomGrid(5, 5, chosen_character);

            while (game_map.current_cell.type != Cell.CellType.FINISH && game_map.current_char.current_hp > 0) {
                if (!game_map.current_cell.visited)
                    getStory(game_map, story_map);
                game_map.current_cell.setVisited(true);
                game_map.printMap();
                nextCommand(game_map);
            }
        }

        //GUI
        else if ( choice == 2 ) {
            Character chosen_character = null;
            UserRegistrationGUI registration = new UserRegistrationGUI(accounts);
            while (true) {
                if (!registration.isDisplayable())
                    break;
            }

            if (registration.loginAccount != null && registration.char_name != null) {
                for (Character c : registration.loginAccount.charList)
                    if (c.name.equals(registration.char_name))
                        chosen_character = c;
                Grid game_map = Grid.generateRandomGrid(3, 6, chosen_character);
                new GameGUI(game_map, story_map);
            }
        }
    }

    //metode private pentru a oferi utilizatorului alegeri in CLI
    private Character chooseCharacter(Account loginAccount) {
        Scanner in = new Scanner(System.in);
        System.out.println("=================\nChoose Your Character:");
        for (int i = 0; i < loginAccount.charList.size(); i++)
            System.out.println(i+1 + " ----------- " + loginAccount.charList.get(i).name);
        System.out.println("=================");
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return loginAccount.charList.get(1);
            else {
                int choice = Integer.parseInt(read);
                if (choice < 1 || choice > loginAccount.charList.size())
                    System.out.println("Incorrect");
                else return loginAccount.charList.get(choice - 1);
            }
        }
    }

    private Account chooseAccount() {
        Scanner in = new Scanner(System.in);
        System.out.println("=================\nChoose Your Account:");
        for (int i = 0; i < accounts.size(); i++)
            System.out.println(i+1 + " ----------- " + accounts.get(i).data.cred.getEmail());
        System.out.println("=================");
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return accounts.get(1);
            else {
                int choice = Integer.parseInt(read);
                if (choice < 1 || choice > accounts.size())
                    System.out.println("Incorrect");
                else return accounts.get(choice - 1);
            }
        }
    }

    private int chooseGameType() {
        System.out.println("""
                =================
                Choose Your Game Type:
                P ----------- PRESET
                1 ----------- CLI
                2 ----------- GUI
                =================""");

        Scanner in = new Scanner(System.in);
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return 0;
            else {
                int choice = Integer.parseInt(read);
                if (choice == 1 || choice == 2)
                    return choice;
                else System.out.println("Incorrect");
            }
        }
    }

    private int enemyChoice(Grid game_map) {
        System.out.println("Current HP: " + game_map.current_char.current_hp);
        System.out.println("Current Mana: " + game_map.current_char.current_mana);
        System.out.println("""
                =================
                1 ----------- Attack Enemy
                2 ----------- Use Ability
                3 ----------- Use Potion
                =================""");

        Scanner in = new Scanner(System.in);
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return 0;
            else {
                int choice = Integer.parseInt(read);
                if (choice >= 1 && choice <= 3)
                    return choice;
                else System.out.println("Incorrect");
            }
        }
    }

    public static void enemyTurn(Grid game_map, Enemy e) {
        if (e.current_hp <= 0)
            return;
        System.out.println("****Enemy's Turn****\n");
        if (Math.random() < 0.2 && e.spells.size() > 0)
            e.use_ability(e.spells.get(0), game_map.current_char);
        else game_map.current_char.receiveDamage(e.getDamage());
        //return true;
    }

    private int chooseAbility(Grid game_map) {
        System.out.println("=================\nChoose Ability:");
        for (int i = 0; i < game_map.current_char.spells.size(); i++)
            System.out.println(i+1 + " ----------- " + game_map.current_char.spells.get(i).toString());
        System.out.println("=================");

        Scanner in = new Scanner(System.in);
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return 0;
            else {
                int choice = Integer.parseInt(read);
                if (choice >= 1 && choice <= game_map.current_char.spells.size())
                    return choice;
                else System.out.println("Incorrect");
            }
        }
    }

    private int choosePotion(Grid game_map) {
        System.out.println("=================\nChoose Potion:");
        for (int i = 0; i < game_map.current_char.inventory.potions.size(); i++)
            System.out.println(i+1 + " ----------- " + game_map.current_char.inventory.potions.get(i).toString());
        System.out.println("=================");

        Scanner in = new Scanner(System.in);
        String read;
        while (true) {
            read = in.next();
            if (read.equalsIgnoreCase("P"))
                return 0;
            else {
                int choice = Integer.parseInt(read);
                if (choice >= 1 && choice <= game_map.current_char.inventory.potions.size())
                    return choice;
                else System.out.println("Incorrect");
            }
        }
    }

    private String chooseDirection() {
        System.out.println("""
                =================
                Choose Direction:
                W ----------- North
                A ----------- West
                S ----------- South
                D ----------- East
                =================""");
        Scanner in = new Scanner(System.in);
        return in.next();
    }

    //preia urmatoarea comanda si afiseaza lista de optiuni disponibile in CLI
    public void nextCommand(Grid game_map) {
        Scanner in = new Scanner(System.in);

        String move = chooseDirection();
        if (move.equalsIgnoreCase("W"))
            game_map.goNorth();
        else if (move.equalsIgnoreCase("S"))
            game_map.goSouth();
        else if (move.equalsIgnoreCase("A"))
            game_map.goWest();
        else if (move.equalsIgnoreCase("D"))
            game_map.goEast();

        if (game_map.current_cell.type == Cell.CellType.SHOP) {
            Shop thisShop;
            thisShop = (Shop) game_map.current_cell.celem;
            System.out.println("Coins: " + game_map.current_char.inventory.coins);
            thisShop.printPotions();
            int choice = in.nextInt();
            while (choice != 0) {
                try {
                    if (game_map.current_char.buyPotion(thisShop.potions.get(choice - 1)))
                        thisShop.getPotion(choice);
                } catch (InvalidCommandException e) {
                    System.out.println(e.getMessage());
                }
                thisShop.printPotions();
                choice = in.nextInt();
            }
        }

        else if (game_map.current_cell.type == Cell.CellType.ENEMY && !game_map.current_cell.visited) {
            Enemy e;
            e = (Enemy) game_map.current_cell.celem;

            while (e.current_hp > 0 && game_map.current_char.current_hp > 0) {
                int choice = enemyChoice(game_map);
                if (choice == 1) {
                    System.out.println("****Normal Attack****\n");
                    e.receiveDamage(game_map.current_char.getDamage());
                }
                else if (choice == 2) {
                    int a = chooseAbility(game_map);
                    game_map.current_char.use_ability(game_map.current_char.spells.get(a - 1), e);
                }
                else if (choice == 3) {
                    if (game_map.current_char.inventory.potions.size() == 0) {
                        System.out.println("No Potions");
                        continue;
                    }
                    else {
                        int p = choosePotion(game_map);
                        game_map.current_char.inventory.potions.get(p - 1).usePotion(game_map.current_char);
                    }
                }
                enemyTurn(game_map, e);
            }

            if (game_map.current_char.current_hp <= 0) {
                System.out.println("**** Game Over ****");
                return;
            }

            game_map.current_char.winCoins("ENEMY");
            game_map.current_char.winExperience("ENEMY");
        }

        else if (game_map.current_cell.type == Cell.CellType.EMPTY) {
            game_map.current_char.winCoins("EMPTYCELL");
            game_map.current_char.winExperience("EMPTYCELL");
        }

        else if (game_map.current_cell.type == Cell.CellType.FINISH)
            System.out.println("**** Finish Reached ****");
    }

    public static String getStory(Grid game_map, HashMap <Cell.CellType, Vector <String>> story_map) {
        Random rd = new Random();
        Vector<String> stories;
        stories = story_map.get(game_map.current_cell.type);
        int rand = rd.nextInt(stories.size());
        System.out.println(stories.get(rand));
        return stories.get(rand);
    }
}

class InformationIncompleteException extends Exception {
    public InformationIncompleteException(String errorMessage) {
        super(errorMessage);
    }
}

//creeza un account din JSONObject si construieste clasa Information
class Account {
    Information data;
    Vector <Character> charList;
    String gamesPlayed;

    public Account(JSONObject obj) {
        charList = new Vector<Character>();
        JSONArray char_arr = obj.getJSONArray("characters");
        for (int i = 0; i < char_arr.length(); i++) {
            JSONObject character = char_arr.getJSONObject(i);
            String name = character.getString("name");
            int level = Integer.parseInt(character.getString("level"));
            int experience = character.getInt("experience");
            if (character.getString("profession").equalsIgnoreCase("WARRIOR"))
                charList.add(CharacterFactory.factory("WARRIOR", name, experience, level));
            else if (character.getString("profession").equalsIgnoreCase("MAGE"))
                charList.add(CharacterFactory.factory("MAGE", name, experience, level));
            else if (character.getString("profession").equalsIgnoreCase("ROGUE"))
                charList.add(CharacterFactory.factory("ROGUE", name, experience, level));
        }

        gamesPlayed = obj.getString("maps_completed");

        JSONObject cred = obj.getJSONObject("credentials");
        String email = cred.getString("email");
        String pass = cred.getString("password");
        String name = obj.getString("name");
        String country = obj.getString("country");
        JSONArray games = obj.getJSONArray("favorite_games");
        Vector <String> favourite_games = new Vector<String>();
        for (int i = 0; i < games.length(); i++)
            favourite_games.add(games.getString(i));
        try {
            data = new Information.InfoBuilder()
                    .email(email)
                    .password(pass)
                    .name(name)
                    .country(country)
                    .favouriteGames(favourite_games)
                    .build();
        } catch (InformationIncompleteException e) {
            System.out.println(e.getMessage());
        }
    }

    public static class Information {
        Credentials cred;
        Vector <String> fav_games;
        String name;
        String country;

        public Information(InfoBuilder builder) {
            cred = new Credentials();
            cred.setEmail(builder.cred.getEmail());
            cred.setPassword(builder.cred.getPassword());
            fav_games = builder.fav_games;
            name = builder.name;
            country = builder.country;
        }

        public static class InfoBuilder {
            private final Credentials cred;
            private Vector <String> fav_games;
            private String name;
            private String country;

            public InfoBuilder() {
                cred = new Credentials();
                fav_games = null;
                name = country = "";
            }

            public InfoBuilder email(String email) { cred.setEmail(email); return this; }
            public InfoBuilder password(String password) { cred.setPassword(password); return this; }
            public InfoBuilder name(String name) { this.name = name; return this; }
            public InfoBuilder country(String country) { this.country = country; return this; }
            public InfoBuilder favouriteGames(Vector <String> favouriteGames) {
                this.fav_games = favouriteGames;
                return this;
            }
            public Information build() throws InformationIncompleteException {
                if (cred.getEmail().equals("") || cred.getPassword().equals("") || name.equals(""))
                    throw new InformationIncompleteException("Incomplete information for generating an Account");
                else return new Information(this);
            }
        }
    }
}

class Credentials {
    private String email;
    private String pass;

    public Credentials() { email = pass = ""; }

    public void setEmail (String email) { this.email = email; }

    public String getEmail() { return email; }

    public void setPassword (String pass) { this.pass = pass; }

    public String getPassword() { return pass; }
}

//harta de joc
class Grid extends ArrayList<ArrayList<Cell>> {
    int lenght;
    int width;
    Character current_char;
    Cell current_cell;

    //genereaza o harta cu casute goale si plaseaza caracterul in coltul din stanga sus
    private Grid(int lenght, int width, Character current_char) {
        super(lenght);
        for (int i = 0; i < lenght; i++)
            this.add(new ArrayList<Cell>(width));
        for (int i = 0; i < lenght; i++)
            for (int j = 0; j < width; j++)
                this.get(i).add(j, new Cell(i, j, false, Cell.CellType.EMPTY, new Empty()));
        this.lenght = lenght;
        this.width = width;
        this.current_char = current_char;
        current_cell = this.get(0).get(0);
    }

    //genereaza o harta HARDCODED
    public static Grid generateHCGrid(int lenght, int width, Character current_char) {
        Grid game_map = new Grid(lenght, width, current_char);
        game_map.get(0).set(3, new Cell(0, 3, false, Cell.CellType.SHOP, new Shop(true)));
        game_map.get(1).set(3, new Cell(1, 3, false, Cell.CellType.SHOP, new Shop(true)));
        game_map.get(2).set(0, new Cell(2, 0, false, Cell.CellType.SHOP, new Shop(true)));
        game_map.get(3).set(4, new Cell(3, 4, false, Cell.CellType.ENEMY, new Enemy()));
        game_map.get(4).set(4, new Cell(4, 4, false, Cell.CellType.FINISH, new Finish()));
        return game_map;
    }

    //genereaza o harta random
    public static Grid generateRandomGrid(int lenght, int width, Character current_char) {
        Grid game_map = new Grid(lenght, width, current_char);
        int enemy_nr = (lenght + width) / 2;
        int shop_nr = (lenght + width) / 3;
        Random rd = new Random();
        while (shop_nr > 0) {
            int x = rd.nextInt(lenght);
            int y = rd.nextInt(width);
            Cell.CellType type = game_map.get(x).get(y).type;
            if (type != Cell.CellType.SHOP) {
                game_map.get(x).set(y, new Cell(x, y, false, Cell.CellType.SHOP, new Shop(false)));
                shop_nr--;
            }
        }
        while (enemy_nr > 0) {
            int x = rd.nextInt(lenght);
            int y = rd.nextInt(width);
            Cell.CellType type = game_map.get(x).get(y).type;
            if (type != Cell.CellType.SHOP && type != Cell.CellType.ENEMY) {
                game_map.get(x).set(y, new Cell(x, y, false, Cell.CellType.ENEMY, new Enemy()));
                enemy_nr--;
            }
        }
        int x = rd.nextInt(lenght);
        int y = rd.nextInt(width);
        game_map.get(x).set(y, new Cell(x, y, false, Cell.CellType.FINISH, new Finish()));
        return game_map;
    }

    //printeaza harta in CLI
    public void printMap() {
        for (ArrayList<Cell> arr : this) {
            for (Cell c : arr)
                if (c.equals(current_cell)) {
                    if (c.type == Cell.CellType.EMPTY)
                        System.out.print("P" + " ");
                    else System.out.print("P" + c.celem.toCharacter() + " ");
                }
                else if (!c.visited)
                    System.out.print("? ");
                else System.out.print(c.celem.toCharacter() + " ");
            System.out.print("\n");
        }
    }

    //deplasari pe harta
    public void goNorth() {
        if (current_cell.x == 0)
            cannotMove();
        else current_cell = this.get(current_cell.x - 1).get(current_cell.y);
    }

    public void goSouth() {
        if (current_cell.x == lenght)
            cannotMove();
        else current_cell = this.get(current_cell.x + 1).get(current_cell.y);
    }

    public void goWest() {
        if (current_cell.y == 0)
            cannotMove();
        else current_cell = this.get(current_cell.x).get(current_cell.y - 1);
    }

    public void goEast() {
        if (current_cell.y == width)
            cannotMove();
        else current_cell = this.get(current_cell.x).get(current_cell.y + 1);
    }

    public void cannotMove() {
         System.out.println("Cannot Move in That Direction\n");
    }
}