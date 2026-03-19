import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class ExpenseTrackerGUI extends JFrame {

    JTextField titleField, amountField, dateField;
    JComboBox<String> categoryBox;
    JTextArea descriptionArea;
    JTable expenseTable;
    DefaultTableModel tableModel;

    double monthlyBudget = 0;
    double totalExpense = 0;

    JTextField budgetField;
    JLabel totalLabel;
    JLabel remainingLabel;

    ArrayList<Expense> expenseList = new ArrayList<>();

    public ExpenseTrackerGUI() {

        setTitle("Expense Tracker and Budget Manager");
        setSize(950,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        getContentPane().setBackground(new Color(245,245,245));

        // ===== TOP PANEL =====
        JPanel inputPanel = new JPanel(new GridLayout(5,2,10,10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));

        inputPanel.add(new JLabel("Expense Title"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Category"));
        categoryBox = new JComboBox<>(new String[]{
                "Food","Travel","Bills","Shopping","Entertainment","Others"
        });
        inputPanel.add(categoryBox);

        inputPanel.add(new JLabel("Amount"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        inputPanel.add(new JLabel("Date"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        inputPanel.add(new JLabel("Description"));
        descriptionArea = new JTextArea(2,20);
        inputPanel.add(descriptionArea);

        add(inputPanel,BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"Title","Category","Amount","Date","Description"};
        tableModel = new DefaultTableModel(columns,0);
        expenseTable = new JTable(tableModel);

        loadExpensesFromFile();

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane,BorderLayout.CENTER);

        // ===== BUDGET PANEL (FIXED UI) =====
        JPanel budgetPanel = new JPanel();
        budgetPanel.setLayout(new GridLayout(3,1,10,10));
        budgetPanel.setBorder(BorderFactory.createTitledBorder("Budget Manager"));

        JPanel topRow = new JPanel();

        topRow.add(new JLabel("Set Budget:"));

        budgetField = new JTextField(8);
        topRow.add(budgetField);

        JButton setBudgetButton = new JButton("Set Budget");
        setBudgetButton.setBackground(new Color(100,149,237));
        setBudgetButton.setForeground(Color.WHITE);
        setBudgetButton.addActionListener(e -> setBudget());
        topRow.add(setBudgetButton);

        totalLabel = new JLabel("Total Expense: 0");
        remainingLabel = new JLabel("Remaining Budget: 0");

        budgetPanel.add(topRow);
        budgetPanel.add(totalLabel);
        budgetPanel.add(remainingLabel);

        add(budgetPanel, BorderLayout.EAST);

        // ===== BUTTON PANEL =====
        JButton addButton = new JButton("Add Expense");
        addButton.setBackground(new Color(60,179,113));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addExpense());

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.setBackground(new Color(220,20,60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteExpense());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // ===== SEARCH PANEL =====
        JPanel searchPanel = new JPanel();

        JLabel searchLabel = new JLabel("Search Category");

        JComboBox<String> searchCategory = new JComboBox<>(new String[]{
                "All","Food","Travel","Bills","Shopping","Entertainment","Others"
        });

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(255,140,0));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchExpense((String)searchCategory.getSelectedItem()));

        searchPanel.add(searchLabel);
        searchPanel.add(searchCategory);
        searchPanel.add(searchButton);

        add(searchPanel,BorderLayout.WEST);
        add(buttonPanel,BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addExpense(){

        String title = titleField.getText();
        String category = (String) categoryBox.getSelectedItem();
        double amount = Double.parseDouble(amountField.getText());
        String date = dateField.getText();
        String description = descriptionArea.getText();

        Expense expense = new Expense(title,category,amount,date,description);

        expenseList.add(expense);
        saveExpensesToFile();

        tableModel.addRow(new Object[]{
                expense.getTitle(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getDate(),
                expense.getDescription()
        });

        totalExpense += amount;
        updateBudgetStatus();

        titleField.setText("");
        amountField.setText("");
        dateField.setText("");
        descriptionArea.setText("");
    }

    private void setBudget(){

        try{
            monthlyBudget = Double.parseDouble(budgetField.getText());
            updateBudgetStatus();
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this,"Enter valid budget!");
        }
    }

    private void updateBudgetStatus(){

        totalLabel.setText("Total Expense: " + totalExpense);

        double remaining = monthlyBudget - totalExpense;

        remainingLabel.setText("Remaining Budget: " + remaining);

        if(totalExpense > monthlyBudget){
            JOptionPane.showMessageDialog(this,"⚠ Budget Limit Exceeded!");
        }
    }

    private void deleteExpense(){

        int selectedRow = expenseTable.getSelectedRow();

        if(selectedRow == -1){
            JOptionPane.showMessageDialog(this,"Select an expense to delete!");
            return;
        }

        Expense removedExpense = expenseList.remove(selectedRow);

        tableModel.removeRow(selectedRow);

        totalExpense -= removedExpense.getAmount();

        updateBudgetStatus();

        saveExpensesToFile();
    }

    private void searchExpense(String category){

        tableModel.setRowCount(0);

        for(Expense e : expenseList){

            if(category.equals("All") || e.getCategory().equals(category)){

                tableModel.addRow(new Object[]{
                        e.getTitle(),
                        e.getCategory(),
                        e.getAmount(),
                        e.getDate(),
                        e.getDescription()
                });
            }
        }
    }

    private void saveExpensesToFile(){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("expenses.csv"));

            for(Expense e : expenseList){

                writer.write(
                        e.getTitle() + "," +
                        e.getCategory() + "," +
                        e.getAmount() + "," +
                        e.getDate() + "," +
                        e.getDescription()
                );

                writer.newLine();
            }

            writer.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadExpensesFromFile(){

        try{
            BufferedReader reader = new BufferedReader(new FileReader("expenses.csv"));

            String line;

            while((line = reader.readLine()) != null){

                String[] data = line.split(",");

                String title = data[0];
                String category = data[1];
                double amount = Double.parseDouble(data[2]);
                String date = data[3];
                String description = data[4];

                Expense expense = new Expense(title,category,amount,date,description);

                expenseList.add(expense);

                tableModel.addRow(new Object[]{
                        title,category,amount,date,description
                });

                totalExpense += amount;
            }

            reader.close();

            updateBudgetStatus();

        } catch(Exception e){
            System.out.println("No previous data found.");
        }
    }

    public static void main(String[] args) {
        new ExpenseTrackerGUI();
    }
}
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class ExpenseTrackerGUI extends JFrame {

    JTextField titleField, amountField, dateField;
    JComboBox<String> categoryBox;
    JTextArea descriptionArea;
    JTable expenseTable;
    DefaultTableModel tableModel;

    double monthlyBudget = 0;
    double totalExpense = 0;

    JTextField budgetField;
    JLabel totalLabel;
    JLabel remainingLabel;

    ArrayList<Expense> expenseList = new ArrayList<>();

    public ExpenseTrackerGUI() {

        setTitle("Expense Tracker and Budget Manager");
        setSize(950,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        getContentPane().setBackground(new Color(245,245,245));

        // ===== TOP PANEL =====
        JPanel inputPanel = new JPanel(new GridLayout(5,2,10,10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));

        inputPanel.add(new JLabel("Expense Title"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Category"));
        categoryBox = new JComboBox<>(new String[]{
                "Food","Travel","Bills","Shopping","Entertainment","Others"
        });
        inputPanel.add(categoryBox);

        inputPanel.add(new JLabel("Amount"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        inputPanel.add(new JLabel("Date"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        inputPanel.add(new JLabel("Description"));
        descriptionArea = new JTextArea(2,20);
        inputPanel.add(descriptionArea);

        add(inputPanel,BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"Title","Category","Amount","Date","Description"};
        tableModel = new DefaultTableModel(columns,0);
        expenseTable = new JTable(tableModel);

        loadExpensesFromFile();

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane,BorderLayout.CENTER);

        // ===== BUDGET PANEL (FIXED UI) =====
        JPanel budgetPanel = new JPanel();
        budgetPanel.setLayout(new GridLayout(3,1,10,10));
        budgetPanel.setBorder(BorderFactory.createTitledBorder("Budget Manager"));

        JPanel topRow = new JPanel();

        topRow.add(new JLabel("Set Budget:"));

        budgetField = new JTextField(8);
        topRow.add(budgetField);

        JButton setBudgetButton = new JButton("Set Budget");
        setBudgetButton.setBackground(new Color(100,149,237));
        setBudgetButton.setForeground(Color.WHITE);
        setBudgetButton.addActionListener(e -> setBudget());
        topRow.add(setBudgetButton);

        totalLabel = new JLabel("Total Expense: 0");
        remainingLabel = new JLabel("Remaining Budget: 0");

        budgetPanel.add(topRow);
        budgetPanel.add(totalLabel);
        budgetPanel.add(remainingLabel);

        add(budgetPanel, BorderLayout.EAST);

        // ===== BUTTON PANEL =====
        JButton addButton = new JButton("Add Expense");
        addButton.setBackground(new Color(60,179,113));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addExpense());

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.setBackground(new Color(220,20,60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteExpense());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // ===== SEARCH PANEL =====
        JPanel searchPanel = new JPanel();

        JLabel searchLabel = new JLabel("Search Category");

        JComboBox<String> searchCategory = new JComboBox<>(new String[]{
                "All","Food","Travel","Bills","Shopping","Entertainment","Others"
        });

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(255,140,0));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchExpense((String)searchCategory.getSelectedItem()));

        searchPanel.add(searchLabel);
        searchPanel.add(searchCategory);
        searchPanel.add(searchButton);

        add(searchPanel,BorderLayout.WEST);
        add(buttonPanel,BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addExpense(){

        String title = titleField.getText();
        String category = (String) categoryBox.getSelectedItem();
        double amount = Double.parseDouble(amountField.getText());
        String date = dateField.getText();
        String description = descriptionArea.getText();

        Expense expense = new Expense(title,category,amount,date,description);

        expenseList.add(expense);
        saveExpensesToFile();

        tableModel.addRow(new Object[]{
                expense.getTitle(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getDate(),
                expense.getDescription()
        });

        totalExpense += amount;
        updateBudgetStatus();

        titleField.setText("");
        amountField.setText("");
        dateField.setText("");
        descriptionArea.setText("");
    }

    private void setBudget(){

        try{
            monthlyBudget = Double.parseDouble(budgetField.getText());
            updateBudgetStatus();
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this,"Enter valid budget!");
        }
    }

    private void updateBudgetStatus(){

        totalLabel.setText("Total Expense: " + totalExpense);

        double remaining = monthlyBudget - totalExpense;

        remainingLabel.setText("Remaining Budget: " + remaining);

        if(totalExpense > monthlyBudget){
            JOptionPane.showMessageDialog(this,"⚠ Budget Limit Exceeded!");
        }
    }

    private void deleteExpense(){

        int selectedRow = expenseTable.getSelectedRow();

        if(selectedRow == -1){
            JOptionPane.showMessageDialog(this,"Select an expense to delete!");
            return;
        }

        Expense removedExpense = expenseList.remove(selectedRow);

        tableModel.removeRow(selectedRow);

        totalExpense -= removedExpense.getAmount();

        updateBudgetStatus();

        saveExpensesToFile();
    }

    private void searchExpense(String category){

        tableModel.setRowCount(0);

        for(Expense e : expenseList){

            if(category.equals("All") || e.getCategory().equals(category)){

                tableModel.addRow(new Object[]{
                        e.getTitle(),
                        e.getCategory(),
                        e.getAmount(),
                        e.getDate(),
                        e.getDescription()
                });
            }
        }
    }

    private void saveExpensesToFile(){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("expenses.csv"));

            for(Expense e : expenseList){

                writer.write(
                        e.getTitle() + "," +
                        e.getCategory() + "," +
                        e.getAmount() + "," +
                        e.getDate() + "," +
                        e.getDescription()
                );

                writer.newLine();
            }

            writer.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadExpensesFromFile(){

        try{
            BufferedReader reader = new BufferedReader(new FileReader("expenses.csv"));

            String line;

            while((line = reader.readLine()) != null){

                String[] data = line.split(",");

                String title = data[0];
                String category = data[1];
                double amount = Double.parseDouble(data[2]);
                String date = data[3];
                String description = data[4];

                Expense expense = new Expense(title,category,amount,date,description);

                expenseList.add(expense);

                tableModel.addRow(new Object[]{
                        title,category,amount,date,description
                });

                totalExpense += amount;
            }

            reader.close();

            updateBudgetStatus();

        } catch(Exception e){
            System.out.println("No previous data found.");
        }
    }

    public static void main(String[] args) {
        new ExpenseTrackerGUI();
    }
}