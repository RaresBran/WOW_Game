import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Vector;

//fereastra de log-in
class UserRegistrationGUI extends JFrame implements ActionListener, MouseListener {
    private final Vector<Account> accounts;
    Account loginAccount = null;    //retine contul de log-in
    String char_name = null;        //retine caracterul ales
    private final JButton button;
    private final JButton cancel_button;
    private final JTextField email;
    private final JPasswordField pass;
    private final JList<String> char_list;

    public UserRegistrationGUI(Vector<Account> accounts) {

        this.accounts = accounts;
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 2));

        JLabel emailLabel = new JLabel("E-mail Address");
        JLabel passLabel = new JLabel("Password");
        email = new JTextField(20);
        pass = new JPasswordField(20);
        JPanel char_panel = new JPanel();

        char_list = new JList<>();
        char_list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        char_list.addMouseListener(this);
        JScrollPane char_scroll = new JScrollPane(char_list);
        char_scroll.setPreferredSize(new Dimension(200, 200));
        char_panel.add(char_scroll);


        button = new JButton("Submit");
        cancel_button = new JButton("Cancel");

        button.addActionListener(this);
        cancel_button.addActionListener(this);

        JPanel emailPanel = new JPanel();
        JPanel passPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(100, 100));
        JPanel loginPanel = new JPanel(new GridLayout(3, 1));
        emailPanel.add(emailLabel);
        emailPanel.add(email);
        passPanel.add(passLabel);
        passPanel.add(pass);
        buttonPanel.add(button);
        buttonPanel.add(cancel_button);
        loginPanel.add(emailPanel);
        loginPanel.add(passPanel);
        loginPanel.add(buttonPanel);
        add(loginPanel);
        add(char_panel);
        add(buttonPanel);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            for (Account a : accounts)
                if (a.data.cred.getEmail().equals(email.getText()) && a.data.cred.getPassword().equals(String.valueOf(pass.getPassword())))
                    loginAccount = a;
            if (loginAccount == null) {
                email.setText("Account does not exist");
                pass.setText("");
            } else {
                DefaultListModel<String> model = new DefaultListModel<>();
                for (Character c : loginAccount.charList)
                    model.addElement(c.name);
                char_list.setModel(model);
            }
        } else if (e.getSource() == cancel_button)
            dispose();
    }

    public void mouseClicked(MouseEvent e) {
        JList theList = (JList) e.getSource();
        if (e.getClickCount() == 2) {
            int index = theList.locationToIndex(e.getPoint());
            if (index >= 0) {
                char_name = (String) theList.getModel().getElementAt(index);
                dispose();
            }
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

//clasa ce implementeaza o celula grafica de pe harta jocului
class MyButton extends JButton {
    //coordonate
    public int x;
    public int y;

    MyButton(ImageIcon icon, int x, int y) {
        super(icon);
        this.x = x;
        this.y = y;
    }
}

//clasa ce implementeaza fereastra principala de joc si asculta algerile utilizatorului
class GameGUI extends JFrame implements ActionListener {
    JButton normal_attack;
    JPanel sidePanel;
    JPanel fightPanel;
    JPanel infoPanel;
    JPanel buttonPanel;
    JList<String> potionList;
    JScrollPane potion_scroll;
    JList<String> abilityList;
    JScrollPane ability_scroll;
    JList<String> usePotionList;
    JScrollPane use_potion_scroll;
    Grid game_map;
    HashMap<String, ImageIcon> icons;
    HashMap <Cell.CellType, Vector <String>> story_map;

    public GameGUI(Grid game_map, HashMap <Cell.CellType, Vector <String>> story_map) {
        sidePanel = new JPanel(new GridLayout(3, 1));
        fightPanel = new JPanel(new GridLayout(2, 3));
        infoPanel = new JPanel();
        buttonPanel = new JPanel();
        this.game_map = game_map;
        this.story_map = story_map;
        icons = new HashMap<String, ImageIcon>();
        icons.put("QUESTION", new ImageIcon("src\\question.png"));
        icons.put("EMPTY", new ImageIcon("src\\grip-horizontal-line.png"));
        icons.put("ENEMY", new ImageIcon("src\\angry.png"));
        icons.put("SHOP", new ImageIcon("src\\shop.png"));
        icons.put("PLAYER", new ImageIcon("src\\user.png"));
        icons.put("FINISH", new ImageIcon("src\\x.png"));
        createGame(game_map);
    }

    //creeaza GUI-ul
    public void createGame(Grid game_map) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel containerPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setLayout(new GridLayout(game_map.lenght, game_map.width));
        for (int i = 0; i < game_map.lenght; i++)
            for (int j = 0; j < game_map.width; j++) {
                MyButton newb;
                if (game_map.get(i).get(j).equals(game_map.current_cell))
                    newb = new MyButton(icons.get("PLAYER"), i, j);
                else newb = new MyButton(icons.get("QUESTION"), i, j);
                if (j % 2 == 0)
                    newb.setBackground(Color.YELLOW);
                else newb.setBackground(Color.GREEN);
                newb.setForeground(Color.RED);
                newb.addActionListener(this);
                buttonPanel.add(newb);
            }
        buttonPanel.setPreferredSize(new Dimension(600, 300));
        containerPanel.add(buttonPanel);

        //informatii despre caracterul curent si inamic
        infoPanel.setLayout(new GridLayout(8, 1));
        infoPanel.add(new JLabel());
        infoPanel.add(new JLabel("Current HP: " + game_map.current_char.current_hp));
        infoPanel.add(new JLabel("Current Mana: " + game_map.current_char.current_mana));
        infoPanel.add(new JLabel("Coins: " + game_map.current_char.inventory.coins));
        infoPanel.add(new JLabel("Experience: " + game_map.current_char.experience));
        infoPanel.add(new JLabel("Level: " + game_map.current_char.level));
        infoPanel.add(new JLabel());
        infoPanel.add(new JLabel());
        sidePanel.add(infoPanel);

        //lista de unde se pot cumpara potiuni in magazin
        potionList = new JList<String>();
        potionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        potionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        //listener pentru elementul selectat din lista
        potionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList theList = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = theList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Shop thisShop = (Shop) game_map.current_cell.celem;
                        try {
                            if (game_map.current_char.buyPotion(thisShop.potions.get(index))) {
                                ((JLabel) infoPanel.getComponent(7)).setText("Message: " +
                                        thisShop.potions.get(index).toString() + "Acquired");
                                thisShop.getPotion(index);
                                DefaultListModel model = (DefaultListModel) theList.getModel();
                                model.remove(index);
                                theList.setModel(model);
                                JLabel coins = (JLabel) infoPanel.getComponent(3);
                                coins.setText("Coins: " + game_map.current_char.inventory.coins);
                            }
                            else ((JLabel) infoPanel.getComponent(7)).setText("Message: Insufficient Coins");
                        } catch (InvalidCommandException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });
        potion_scroll = new JScrollPane(potionList);
        potion_scroll.setVisible(false);
        sidePanel.add(potion_scroll);

        //panel ce apare doar cand caracterul este pe o casuta ENEMY
        fightPanel.add(new JLabel("Use Ability:"));
        fightPanel.add(new JLabel("Use Potion:"));
        fightPanel.add(new JLabel("Attack:"));
        fightPanel.setVisible(false);

        //list pentru alegerea unei abilitati in lupta
        abilityList = new JList<String>();
        abilityList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        abilityList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (Spell s : game_map.current_char.spells)
            model.addElement(s.toString());
        abilityList.setModel(model);
        //listener pentru elementul selectat din lista
        abilityList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList theList = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = theList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Spell spell = game_map.current_char.spells.get(index);
                        if (game_map.current_char.use_ability(spell, ((Enemy)game_map.current_cell.celem)))
                            ((JLabel) infoPanel.getComponent(7)).setText("Message: Spell Used");
                        else ((JLabel) infoPanel.getComponent(7)).setText("****Insufficient Mana...Used Normal Attack****");
                        DefaultListModel model = (DefaultListModel) theList.getModel();
                        model.remove(index);
                        theList.setModel(model);
                        ((JLabel) infoPanel.getComponent(6)).setText("Enemy HP: " +
                                ((Enemy) game_map.current_cell.celem).current_hp);
                        Game.enemyTurn(game_map, (Enemy) game_map.current_cell.celem);
                        ((JLabel) infoPanel.getComponent(1)).setText("Current HP: " +
                                game_map.current_char.current_hp);
                        ((JLabel) infoPanel.getComponent(2)).setText("Current Mana: " +
                                game_map.current_char.current_mana);

                        if (((Enemy) game_map.current_cell.celem).current_hp <= 0) {
                            fightPanel.setVisible(false);
                            infoPanel.getComponent(6).setVisible(false);
                            game_map.current_char.winCoins("ENEMY");
                            ((JLabel) infoPanel.getComponent(7)).setText("Message: Enemy Defeated");
                        }
                        if (game_map.current_char.current_hp <= 0) {
                            ((JLabel) infoPanel.getComponent(7)).setText("Message: Defeat");
                            buttonPanel.setVisible(false);
                        }
                    }
                }
            }
        });
        ability_scroll = new JScrollPane(abilityList);
        fightPanel.add(ability_scroll);

        //lista pentru folosirea unei potiuni in lupta
        usePotionList = new JList<String>();
        usePotionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        usePotionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        //listener pentru elementul selectat din lista
        usePotionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList theList = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = theList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Potion potion = game_map.current_char.inventory.potions.get(index);
                        potion.usePotion(game_map.current_char);
                        ((JLabel) infoPanel.getComponent(7)).setText("Message: Potion Used");
                        DefaultListModel model = (DefaultListModel) theList.getModel();
                        model.remove(index);
                        theList.setModel(model);
                        Game.enemyTurn(game_map, (Enemy) game_map.current_cell.celem);
                        ((JLabel) infoPanel.getComponent(1)).setText("Current HP: " + game_map.current_char.current_hp);
                        ((JLabel) infoPanel.getComponent(2)).setText("Current Mana: " + game_map.current_char.current_mana);

                        if (((Enemy) game_map.current_cell.celem).current_hp <= 0) {
                            fightPanel.setVisible(false);
                            infoPanel.getComponent(6).setVisible(false);
                            game_map.current_char.winCoins("ENEMY");
                            game_map.current_char.winExperience("ENEMY");
                            ((JLabel) infoPanel.getComponent(7)).setText("Message: Enemy Defeated");
                        }
                        if (game_map.current_char.current_hp <= 0) {
                            buttonPanel.setVisible(false);
                            ((JLabel) infoPanel.getComponent(7)).setText("Message: Defeat");
                        }
                    }
                }
            }
        });
        use_potion_scroll = new JScrollPane(usePotionList);
        fightPanel.add(use_potion_scroll);

        //buton pentru atac normal
        normal_attack = new JButton("Normal Attack");
        fightPanel.add(normal_attack);
        normal_attack.addActionListener(this);

        sidePanel.add(fightPanel);

        containerPanel.add(sidePanel);
        getContentPane().add(containerPanel);
        pack();
        setVisible(true);
    }

    //listener pentru butoane
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == normal_attack) {
            Enemy enemy = (Enemy) game_map.current_cell.celem;
            enemy.receiveDamage(game_map.current_char.getDamage());
            ((JLabel) infoPanel.getComponent(6)).setText("Enemy HP: " + ((Enemy) game_map.current_cell.celem).current_hp);
            Game.enemyTurn(game_map, (Enemy) game_map.current_cell.celem);
            ((JLabel) infoPanel.getComponent(1)).setText("Current HP: " + game_map.current_char.current_hp);
            ((JLabel) infoPanel.getComponent(2)).setText("Current Mana: " + game_map.current_char.current_mana);
            if (enemy.current_hp <= 0) {    //inamicul a murit
                fightPanel.setVisible(false);
                infoPanel.getComponent(6).setVisible(false);
                game_map.current_char.winCoins("ENEMY");
                game_map.current_char.winExperience("ENEMY");
                ((JLabel) infoPanel.getComponent(7)).setText("Message: Enemy Defeated");
            }
            if (game_map.current_char.current_hp <= 0) {    //caracterul a murit
                ((JLabel) infoPanel.getComponent(7)).setText("Message: Defeat");
                buttonPanel.setVisible(false);
            }
        }
        else if (!fightPanel.isVisible()){  //nu lasa caracterul sa se miste in lupta
            MyButton b = (MyButton) e.getSource();
            Cell old_cell = game_map.current_cell;

            if (game_map.get(b.x).get(b.y).type != Cell.CellType.SHOP)
                potion_scroll.setVisible(false);

            //deplasarea pe harta
            if (b.x - game_map.current_cell.x == 1 && b.y == game_map.current_cell.y)
                game_map.goSouth();
            else if (b.x - game_map.current_cell.x == -1 && b.y == game_map.current_cell.y)
                game_map.goNorth();
            else if (b.x == game_map.current_cell.x && b.y - game_map.current_cell.y == -1)
                game_map.goWest();
            else if (b.x == game_map.current_cell.x && b.y - game_map.current_cell.y == 1)
                game_map.goEast();
            if (!old_cell.equals(game_map.current_cell))
                updateMap(b, old_cell);
        }
    }

    //actualizeaza harta si gui-ul in cazul unui inamic sau magazin
    private void updateMap(MyButton b, Cell old_cell) {
        if (game_map.get(b.x).get(b.y).type == Cell.CellType.FINISH)
            b.setIcon(icons.get("FINISH"));
        else b.setIcon(icons.get("PLAYER"));
        MyButton old_pos = (MyButton) buttonPanel.getComponent(game_map.width * old_cell.x + old_cell.y);

        JLabel story = (JLabel) infoPanel.getComponent(0);
        if (!game_map.current_cell.visited) {
            story.setText("Story: " + Game.getStory(game_map, story_map));
        } else story.setText("Story: ");

        if (old_cell.type == Cell.CellType.SHOP)
            old_pos.setIcon(icons.get("SHOP"));
        else if (old_cell.type == Cell.CellType.ENEMY)
            old_pos.setIcon(icons.get("ENEMY"));
        else if (old_cell.type == Cell.CellType.EMPTY)
            old_pos.setIcon(icons.get("EMPTY"));
        else if (old_cell.type == Cell.CellType.FINISH)
            old_pos.setIcon(icons.get("FINISH"));

        if (game_map.current_cell.type == Cell.CellType.ENEMY && !game_map.current_cell.visited)
            enemyFight();
        else if (game_map.current_cell.type == Cell.CellType.SHOP)
            shop(b);
        else if (game_map.current_cell.type == Cell.CellType.EMPTY && !game_map.current_cell.visited) {
            game_map.current_char.winExperience("EMPTYCELL");
            game_map.current_char.winCoins("EMPTYCELL");
            ((JLabel) infoPanel.getComponent(3)).setText("Coins: " + game_map.current_char.inventory.coins);
        }
        else if (game_map.current_cell.type == Cell.CellType.FINISH)
            buttonPanel.setVisible(false);

        game_map.current_cell.setVisited(true);
    }

    //initiaza lupta
    private void enemyFight() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (Potion p : game_map.current_char.inventory.potions)
            model.addElement(p.toString());
        usePotionList.setModel(model);
        fightPanel.setVisible(true);
        JLabel enemyHP = (JLabel) infoPanel.getComponent(6);
        enemyHP.setVisible(true);
        enemyHP.setText("Enemy HP: " + ((Enemy) game_map.current_cell.celem).current_hp);
    }

    //initiaza lista cu potiuni din magazin
    private void shop(MyButton b) {
        Shop thisShop = (Shop) game_map.get(b.x).get(b.y).celem;
        DefaultListModel<String> model = new DefaultListModel<String>();
        Vector<Potion> potions = thisShop.potions;
        for (Potion p : potions)
            model.addElement(p.toString());
        potionList.setModel(model);
        potion_scroll.setVisible(true);
    }
}
