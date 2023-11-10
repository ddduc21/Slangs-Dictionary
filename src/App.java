import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private ArrayList<String> slangs = new ArrayList<>();
    private ArrayList<String> definitions = new ArrayList<>();
    private ArrayList<String> searchHistory = new ArrayList<>();
    private ArrayList<String> changeHistory = new ArrayList<>();

    String searchWord(String word) {
        if (!searchHistory.contains(word))
            searchHistory.add(word);
        return slangToDef.get(word);
    }

    ArrayList<String> searchDefinition(String definition) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = 0; i < definitions.size(); i++) {
            if (definitions.get(i).indexOf(definition) >= 0) {
                arr.add(slangs.get(i));
            }
        }
        return arr;
    }

    void add(String word, String definition) {
        slangToDef.put(word, definition);
        slangs.add(word);
        definitions.add(definition);
        changeHistory.add("add`" + word + "`" + definition);
    }

    void edit(String word, String definition) {
        changeHistory.add("edit`" + word + "`" + slangToDef.get(word));
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
        changeHistory.add("remove`" + word + "`" + definition);
    }

    void resetDictionary() {
        for (int index = changeHistory.size() - 1; index >= 0 ; index--) {
            String[] tokens = changeHistory.get(index).split("`");
            switch (tokens[0]) {
                case "add": {
                    slangToDef.remove(tokens[1], tokens[2]);
                    int addIndex = slangs.indexOf(tokens[1]);
                    slangs.remove(addIndex);
                    definitions.remove(addIndex);
                    break;
                }
                case "edit": {
                    slangToDef.replace(tokens[1], tokens[2]);
                    int editIndex = slangs.indexOf(tokens[1]);
                    slangs.set(editIndex, tokens[1]);
                    definitions.set(editIndex, tokens[2]);
                    break;
                }
                case "remove": {
                    slangToDef.put(tokens[1], tokens[2]);
                    int removeIndex = slangs.indexOf(tokens[1]);
                    slangs.add(removeIndex, tokens[1]);
                    definitions.add(removeIndex, tokens[2]);
                    break;
                }
                default:
                    break;
            }
        }
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
        Random random = new Random();
        String[] randomSlang = new String[8];
        int[] randomNumber = new int[4];
        for (int i = 0; i < 4; i++) {
            boolean duplicated = false;
            do {
                duplicated = false;
                int index = random.nextInt(slangs.size());
                for (int j = 0; j < i; j++) {
                    if (index == randomNumber[j]) {
                        duplicated = true;
                    }
                }
                if (!duplicated) {
                    randomNumber[i] = index;
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

    ArrayList<String> getSearchHistory() {
        return searchHistory;
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

    public JPanel menuNavigate(Controler controler) {
        JPanel panel = new JPanel();
        JMenuBar menu = new JMenuBar();
        JMenuItem mainMenu = new JMenuItem("Home");
        JMenu editMenu = new JMenu("Edit");
        JMenu quizMenu = new JMenu("Quiz");
        JMenuItem historyMenu = new JMenuItem("History");

        editMenu.add("Add slang");
        editMenu.add("Edit slang");
        editMenu.add("Delete slang");
        editMenu.add("Reset slang");

        quizMenu.add("Guess definition by slang").addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JPanel navTo = View.this.quizView(controler, "Guess definition by slang");
                    View.this.setView(navTo);
                }
                
            }
        );

        quizMenu.add("Guess slang by definition").addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JPanel navTo = View.this.quizView(controler, "Guess slang by definition");
                    View.this.setView(navTo);
                }
                
            }
        );

        menu.add(mainMenu);
        menu.add(editMenu);
        menu.add(quizMenu);
        menu.add(historyMenu);

        panel.add(menu);
        panel.setMaximumSize(panel.getPreferredSize());

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                View.this.setView(View.this.mainMenuView(controler));
            }
        });

        historyMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                View.this.setView(View.this.historyView(controler));
            }
            
        });
        return panel;
    }

    public JPanel mainMenuView(Controler controler) {
        JPanel mainPanel = new JPanel();
        BoxLayout menuLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(menuLayout);
        
        JPanel navPanel = menuNavigate(controler);
        
        String[] searchChoices = { "Search by word", "Search by definition" };
        JPanel searchPanel = new JPanel();
        JButton searchButton = new JButton("Search");
        JComboBox<String> searchMethod = new JComboBox<String>(searchChoices);
        JTextField searchTextField = new JTextField("search...", 30);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        searchPanel.add(searchMethod);
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
                String choseWord = list.getSelectedValue();
                if (choseWord != null)
                    detailsTextArea.setText(controler.searchWord(choseWord));
                else
                    detailsTextArea.setText("");
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchString = searchTextField.getText();

                if (searchString != "") {
                    if (searchMethod.getSelectedItem() == "Search by word") {
                        if (slangs.contains(searchString))
                            list.setSelectedIndex(slangs.indexOf(searchString));
                            list.ensureIndexIsVisible(list.getSelectedIndex());
                    }
                    else if (searchMethod.getSelectedItem() == "Search by definition") {
                        ArrayList<String> searchResult = controler.searchDefinition(searchString);
                        if (searchResult.size() > 0) {
                            list.setListData(searchResult.toArray(new String[searchResult.size()]));
                        }
                        else {
                            list.setListData(new String[0]);
                        }
                    }
                }
                else {
                    list.setListData(slangs.toArray(new String[slangs.size()]));
                }
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

    public JPanel historyView(Controler controler) {
        JPanel mainPanel = new JPanel();
        BoxLayout menuLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(menuLayout);
        
        JPanel navPanel = menuNavigate(controler);
        
        String[] searchChoices = { "Search by word", "Search by definition" };
        JPanel searchPanel = new JPanel();
        JButton searchButton = new JButton("Search");
        JComboBox<String> searchMethod = new JComboBox<String>(searchChoices);
        JTextField searchTextField = new JTextField("search...", 30);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        searchPanel.add(searchMethod);
        searchPanel.setMaximumSize(searchPanel.getPreferredSize());

        ArrayList<String> slangs = controler.getSearchHistory();

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
                String choseWord = list.getSelectedValue();
                if (choseWord != null)
                    detailsTextArea.setText(controler.getDefinition(choseWord));
                else
                    detailsTextArea.setText("");
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchString = searchTextField.getText();

                if (searchString != "") {
                    if (searchMethod.getSelectedItem() == "Search by word") {
                        if (slangs.contains(searchString))
                            list.setSelectedIndex(slangs.indexOf(searchString));
                            list.ensureIndexIsVisible(list.getSelectedIndex());
                    }
                    else if (searchMethod.getSelectedItem() == "Search by definition") {
                        ArrayList<String> searchResult = controler.searchDefinition(searchString);
                        if (searchResult.size() > 0) {
                            list.setListData(searchResult.toArray(new String[searchResult.size()]));
                        }
                        else {
                            list.setListData(new String[0]);
                        }
                    }
                }
                else {
                    list.setListData(slangs.toArray(new String[slangs.size()]));
                }
            }
        });

        mainPanel.add(navPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(detailsPanel);
        mainPanel.add(Box.createHorizontalGlue());

        return mainPanel;
    }

    public JPanel quizView(Controler controler, String quizType) {
        JPanel mainPanel = new JPanel();
        BoxLayout mainLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(mainLayout);
        
        JPanel navPanel = menuNavigate(controler);

        int questionNumber = 1;

        JTextArea questionTextArea = new JTextArea("");
        ButtonGroup answerGroup = new ButtonGroup();
        JRadioButton[] answers = new JRadioButton[4];
        for (int i = 0; i < 4; i++) {
            answers[i] = new JRadioButton();
            answerGroup.add(answers[i]);
        }

        JTextArea resultTextArea = new JTextArea(); 

        JButton submitButton = new JButton("Confirm answer");
        JButton nextButton = new JButton("Next");

        class WorkAroundInt {
            int a;
            public WorkAroundInt(int a) {
                this.a = a;
            }
            public int get() {
                return a;
            }

            public void set(int a) {
                this.a = a;
            }  
        }
        Random random = new Random();
        WorkAroundInt correctAnswerIndex = new WorkAroundInt(random.nextInt(4));

        if (quizType == "Guess definition by slang") {
            String[] quizData = controler.random4();
            questionTextArea.setText(quizData[2*correctAnswerIndex.get()]);
            for (int i = 0; i < 4; i++) {
                answers[i].setText(quizData[2*i + 1]);
            }

            nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] quizData = controler.random4();
                correctAnswerIndex.set(random.nextInt(4));
                questionTextArea.setText(quizData[2*correctAnswerIndex.get()]);
                for (int i = 0; i < 4; i++) {
                    answers[i].setText(quizData[2*i + 1]);
                    answers[i].setEnabled(true);
                }
                resultTextArea.setText("");
                answerGroup.clearSelection();
            }});
        }
        if (quizType == "Guess slang by definition") {
            String[] quizData = controler.random4();
            questionTextArea.setText(quizData[2*correctAnswerIndex.get() + 1]);
            for (int i = 0; i < 4; i++) {
                answers[i].setText(quizData[2*i]);
            }

            nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] quizData = controler.random4();
                correctAnswerIndex.set(random.nextInt(4));
                questionTextArea.setText(quizData[2*correctAnswerIndex.get() + 1]);
                for (int i = 0; i < 4; i++) {
                    answers[i].setText(quizData[2*i]);
                    answers[i].setEnabled(true);
                }
                resultTextArea.setText("");
                answerGroup.clearSelection();
            }});
        }

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (answers[correctAnswerIndex.get()].isSelected()) {
                    resultTextArea.setText("Correct!");
                }
                else {
                    resultTextArea.setText("Incorrect!");
                }
                for (int i = 0; i < answers.length; i++) {
                    answers[i].setEnabled(false);
                }
            }
        });

        mainPanel.add(navPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(questionTextArea);
        for (int i = 0; i < 4; i++) {
            mainPanel.add(answers[i]);   
        }
        mainPanel.add(resultTextArea);
        mainPanel.add(submitButton);
        mainPanel.add(nextButton);
        mainPanel.add(Box.createHorizontalGlue());

        return mainPanel;
    }

    public void addDebugBorder(JComponent comp) {
        comp.setBorder(BorderFactory.createCompoundBorder(
                   BorderFactory.createLineBorder(Color.red),
                   comp.getBorder()));
    }

    public void setView(JPanel view) {
        this.setContentPane(view);
        view.setVisible(true);
        this.pack();
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