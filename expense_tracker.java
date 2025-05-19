package com.mycompany.dbms1;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ExpenseTrackerApp {

    // Database connection details
    public static final String DB_URL = "jdbc:mysql://localhost:3306/student";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "dbms";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }

    // Database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Login Frame
    static class LoginFrame extends JFrame {
        JTextField usernameField;
        JPasswordField passwordField;

        public LoginFrame() {
            setTitle("Login");
            setSize(300, 200);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(3, 2));

            add(new JLabel("Username:"));
            usernameField = new JTextField();
            add(usernameField);

            add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            add(passwordField);

            JButton loginBtn = new JButton("Login");
            loginBtn.addActionListener(e -> loginUser());
            add(loginBtn);

            JButton registerBtn = new JButton("Register");
            registerBtn.addActionListener(e -> new RegisterFrame());
            add(registerBtn);

            setVisible(true);
        }

        // Login functionality
        void loginUser() {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
                stmt.setString(1, user);
                stmt.setString(2, pass);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    dispose();
                    new DashboardFrame(rs.getInt("id"));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Register Frame
    static class RegisterFrame extends JFrame {
        JTextField usernameField;
        JPasswordField passwordField;

        public RegisterFrame() {
            setTitle("Register");
            setSize(300, 200);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new GridLayout(3, 2));

            add(new JLabel("Username:"));
            usernameField = new JTextField();
            add(usernameField);

            add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            add(passwordField);

            JButton registerBtn = new JButton("Register");
            registerBtn.addActionListener(e -> registerUser());
            add(registerBtn);

            setVisible(true);
        }

        // Register functionality
        void registerUser() {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                stmt.setString(1, user);
                stmt.setString(2, pass);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Dashboard Frame
    static class DashboardFrame extends JFrame {
        int userId;
        JTable transactionTable;
        DefaultTableModel tableModel;

        public DashboardFrame(int userId) {
            this.userId = userId;
            setTitle("Dashboard");
            setSize(600, 400);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Table for displaying transactions
            String[] columns = {"Date", "Category", "Type", "Amount", "Description"};
            tableModel = new DefaultTableModel(columns, 0);
            transactionTable = new JTable(tableModel);
            add(new JScrollPane(transactionTable), BorderLayout.CENTER);

            // Button Panel
            JPanel buttonPanel = new JPanel();
            JButton addTransactionBtn = new JButton("Add Transaction");
            addTransactionBtn.addActionListener(e -> addTransaction());
            buttonPanel.add(addTransactionBtn);

            JButton refreshBtn = new JButton("Refresh");
            refreshBtn.addActionListener(e -> loadTransactions());
            buttonPanel.add(refreshBtn);

            add(buttonPanel, BorderLayout.SOUTH);

            loadTransactions();

            setVisible(true);
        }

        // Add transaction to the database
        void addTransaction() {
            String category = JOptionPane.showInputDialog("Category:");
            String type = JOptionPane.showInputDialog("Type (income/expense):");
            double amount = Double.parseDouble(JOptionPane.showInputDialog("Amount:"));
            String description = JOptionPane.showInputDialog("Description:");

            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transactions (user_id, amount, category, type, date, description) VALUES (?, ?, ?, ?, CURDATE(), ?)"
                );
                stmt.setInt(1, userId);
                stmt.setDouble(2, amount);
                stmt.setString(3, category);
                stmt.setString(4, type);
                stmt.setString(5, description);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Transaction Added!");
                loadTransactions();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Load transactions from the database
        void loadTransactions() {
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transactions WHERE user_id = ?");
                stmt.setInt(1, userId);

                ResultSet rs = stmt.executeQuery();
                tableModel.setRowCount(0); // Clear table

                while (rs.next()) {
                    Object[] row = {
                        rs.getDate("date"),
                        rs.getString("category"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    };
                    tableModel.addRow(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
