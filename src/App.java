import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class Controler {
    final String filename = "slang.txt";
    private Map<String, String> slangToDef;
    private ArrayList<String> slangs;
    private ArrayList<String> definitions;

    String searchWord(String definition) {
        return slangToDef.get(definition);
    }

    ArrayList<String> searchDefinition(String word) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < definitions.size(); i++) {
            if (definitions.get(i).indexOf(word) >= 0) {
                arr.add(slangs.get(i));
            }
        }
        return arr;
    }

    void add(String word, String definition) {
        slangToDef.put(word, definition);
        slangs.add(word);
        definitions.add(definition);
    }

    void edit(String word, String definition) {
        slangToDef.replace(word, definition);
        int index = slangs.indexOf(word);
        slangs.set(index, word);
        definitions.set(index, definition);
    }

    void remove(String word, String definition) {
        slangToDef.remove(word, definition);
        int index = slangs.indexOf(word);
        slangs.remove(index);
        definitions.remove(index);
    }

    boolean contain(String word) {
        return slangToDef.keySet().contains(word);
    }

    void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < slangs.size(); i++) {
                writer.write(slangs.get(i) + definitions.get(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            slangToDef = new TreeMap<String, String>();
            slangs = new ArrayList<>();
            definitions = new ArrayList<>();
            String line = "";
            while (line != null) {
                line = reader.readLine();
                if (line == null) break;
                String[] temp = line.split("`");
                if (temp.length > 1) {
                    slangs.add(temp[0]);
                    definitions.add(temp[1]);
                    slangToDef.put(temp[0], temp[1]);
                }
                else {
                    String slang = slangs.get(slangs.size() - 1);
                    slangs.add(slang);
                    definitions.add(temp[0]);
                    slangToDef.replace(slang, slangToDef.get(slang) + '`' + temp[0]);
                }
            }
        } catch (IOException e) {
            System.out.println(slangs.get(slangs.size() - 1));
            e.printStackTrace();
        }
    }

    String[] random() {
        Random random = new Random();
        int index = random.nextInt(slangs.size());
        String[] randomSlang = {slangs.get(index), definitions.get(index)};
        return randomSlang;
    }

    String[] random4() {
        String[] randomSlang = new String[8];
        int[] randomNumber = new int[4];
        for (int i = 0; i < 4; i++) {
            boolean duplicated = false;
            do {
                duplicated = false;
                Random random = new Random();
                int index = random.nextInt(slangs.size());
                for (int j = 0; j < i; j++) {
                    if (index == randomNumber[j]) {
                        duplicated = true;
                    }
                }
            } while (duplicated);
            randomSlang[i*2] = slangs.get(randomNumber[i]);
            randomSlang[i*2 + 1] = definitions.get(randomNumber[i]);
        }
        return randomSlang;   
    }

    String getDefinition(String slang) {
        return slangToDef.get(slang);
    }

    ArrayList<String> getSlangs() {
        return slangs;
    }
    
    ArrayList<String> getDefinitions() {
        return definitions;
    }
}

class View extends JFrame {
    private Container contentPanel;
    private int width = 800;
    private int height = 600;

    public View() {
        super("Slangword collection!");
        contentPanel = this.getContentPane();
        this.setPreferredSize(new Dimension(width, height));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public JPanel menuNavigate() {
        JPanel panel = new JPanel();
        JMenuBar menu = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");
        JMenu quizMenu = new JMenu("Quiz");

        editMenu.add("Add slang");
        editMenu.add("Edit slang");
        editMenu.add("Delete slang");
        editMenu.add("Reset slang");
        editMenu.add("History");

        quizMenu.add("Guess definition by slang");
        quizMenu.add("Guess slang by definition");

        menu.add(editMenu);
        menu.add(quizMenu);

        panel.add(menu);
        panel.setMaximumSize(panel.getPreferredSize());
        return panel;
    }

    public JPanel mainMenuView(Controler controler) {
        JPanel mainPanel = new JPanel();
        BoxLayout menuLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(menuLayout);
        
        JPanel navPanel = menuNavigate();
        
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JTextField("search...", 30));
        searchPanel.add(new JButton("Search"));
        String[] searchChoices = { "Search by word", "Search by definition" };
        searchPanel.add(new JComboBox<String>(searchChoices));
        searchPanel.setMaximumSize(searchPanel.getPreferredSize());

        ArrayList<String> slangs = controler.getSlangs();

        JPanel detailsPanel = new JPanel();

        JList<String> list = new JList<>();
        list.setListData(slangs.toArray(new String[slangs.size()]));

        JTextArea detailsTextArea = new JTextArea(10, 30);
        detailsTextArea.setEditable(false);
        detailsTextArea.setText("");
        detailsTextArea.setLineWrap(true);
        detailsPanel.add(new JScrollPane(list));
        detailsPanel.add(detailsTextArea);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // TODO Auto-generated method stub
                String choseWord = list.getSelectedValue();
                detailsTextArea.setText(controler.getDefinition(choseWord));
            }
        });

        mainPanel.add(navPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(detailsPanel);
        mainPanel.add(Box.createHorizontalGlue());

        addDebugBorder(navPanel);
        addDebugBorder(searchPanel);
        addDebugBorder(detailsPanel);
        addDebugBorder(mainPanel);

        return mainPanel;
    }

    public void addDebugBorder(JComponent comp) {
        comp.setBorder(BorderFactory.createCompoundBorder(
                   BorderFactory.createLineBorder(Color.red),
                   comp.getBorder()));
    }

    public void setView(JPanel view) {
        contentPanel.removeAll();
        contentPanel.add(view);
    }
}

public class App {
    public static void main(String[] args) throws Exception {
        Controler controler = new Controler();
        controler.load();
        View uiView = new View();

        uiView.setView(uiView.mainMenuView(controler));
    }
}

/*
1.Chức năng tìm kiếm theo slang word.
2.Chức năng tìm kiếm theo definition, hiển thị ra tất cả các slang words mà trong defintion có chứa keyword gõ vào.
3.Chức năng hiển thị history, danh sách các slang word đã tìm kiếm.
4.Chức năng add 1 slang words mới. Nếu slang words trùng thì thông báo cho người dùng, confirm có overwrite hay duplicate ra 1 slang word mới.
5.Chức năng edit 1 slang word.
6.Chức năng delete 1 slang word. Confirm trước khi xoá.
7.Chức năng reset danh sách slang words gốc.
8.Chức năng random 1 slang word (On this day slang word).
9.Chức năng đố vui, chương trình hiển thị 1 random slang word, với 4 đáp án cho người dùng chọn.
10.Chức năng đố vui, chương trình hiển thị 1 definition, với 4 slang words đáp án cho người dùng chọn.
*/