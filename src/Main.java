import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.Objects;
import java.util.Scanner;


public class Main {
    static MysqlDataSource dataSource;
    static Scanner scanner;
    static String url = "localhost";
    static int port = 3306;
    static String database = "swosh_db";
    static String username = "root";
    static String password = "1234";

    //Konfigurerar kopplingar mot databasen
    public static void InitializeDatabase() {
        try {
            System.out.print("Configuring data source...");
            dataSource = new MysqlDataSource();
            dataSource.setUser(username);
            dataSource.setPassword(password);
            dataSource.setUrl("jdbc:mysql://" + url + ":" + port + "/" + database +
                    "?serverTimezone=UTC");
            dataSource.setUseSSL(false);
            System.out.print("done!\n");
        } catch (SQLException e) {
            System.out.print("failed!\n");
            // PrintSQLException(e);
            System.exit(0);
        }
    }

    public static boolean checkIfUserAlreadyExists(String citizenID) {
        try {
            Connection connection = GetConnection();

            String query = "SELECT citizenID FROM users WHERE citizenID= ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, citizenID);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                System.out.println("That user already exists");
                connection.close();
                return true;
                //String dbCitizenID = result.getString("citizenID");

            }

            System.out.println(citizenID + " is not already in the system");
            connection.close();

        } catch (SQLException e) {
            System.out.println("Im in the catch");
            //PrintSQLException(e);
        }
        return false;
    }

    public static int getBalance(int accountID) {
        try {
            Connection connection = GetConnection();

            String query = "SELECT id, balance FROM accounts WHERE id= ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, accountID);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                int dbAccountID = result.getInt("id");
                int dbBalance = result.getInt("balance");

                if (dbAccountID == accountID) {
                    return dbBalance;
                }
            }

            System.out.println();
            connection.close();

        } catch (SQLException e) {
            //PrintSQLException(e);
        }
        return -1;
    }


    public static boolean checkCredentials(String citizenID, String password) {

        try {
            Connection connection = GetConnection();

            String query = "SELECT citizenID, password FROM users WHERE citizenID= ? AND password= ?";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, citizenID);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String dbCitizenID = result.getString("citizenID");
                String dbPassword = result.getString("password");

                if (Objects.equals(dbCitizenID, citizenID) && dbPassword.equals(password)) {
                    connection.close();
                    return true;
                }

            }
            connection.close();

        } catch (SQLException e) {
            //PrintSQLException(e);
        }
        return false;
    }

    public static void createTransaction(int fromAccount, int toAccount, int fromAccountRevisedBalance, int amount) {

        //If the account doesn't exist then func getBalance returns -1
        int toAccountCurrentBalance = getBalance(toAccount);
        System.out.println(toAccountCurrentBalance);
        boolean recipientAccountExists = toAccountCurrentBalance >= 0;
        int toAccountRevisedBalance = toAccountCurrentBalance + amount;

        if (recipientAccountExists) {
            try {
                // Koppla upp mot databasen
                Connection connection = GetConnection();

                //Lägga in transaktionen i historiken
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO transactions (fromAccount, toAccount, amount) VALUES (?, ?, ?)"
                );

                statement.setInt(1, fromAccount);
                statement.setInt(2, toAccount);
                statement.setInt(3, amount);

                // Kör SQL-queryn för att skapa en ny användare
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("The transaction has been added to table: transactions");
                } else {
                    System.out.println("something went wrong (line 149)");
                }

                //Ändra kontons saldo
                String query = "UPDATE accounts SET balance = ? WHERE id = ?";
                PreparedStatement statement2 = connection.prepareStatement(query);
                statement2.setInt(1, fromAccountRevisedBalance);
                statement2.setInt(2, fromAccount);

                int oneRowAffected = statement2.executeUpdate();

                statement2.setInt(1, toAccountRevisedBalance);
                statement2.setInt(2, toAccount);
                int anotherRowAffected = statement2.executeUpdate();

                if (oneRowAffected + anotherRowAffected == 2) {
                    System.out.println("Two accounts have had their accounts updated");
                } else {
                    System.out.println("something went wrong (line 165)");
                }

                connection.close();
                //return rowsAffected > 0;
            } catch (SQLException e) {
                //PrintSQLException(e);
            }
        } else {
            System.out.println("the recipient account doesn't exist. No transaction has been made");
        }


    }


    public static void createAccount(String citizenID) {

        try {
            // Koppla upp mot databasen
            Connection connection = GetConnection();

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO accounts (citizenID) VALUES (?)"
            );

            statement.setString(1, citizenID);

            // Kör SQL-queryn för att skapa en ny användare
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("The account was created");
            } else {
                System.out.println("something went wrong (line 198)");
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

    public static void deleteUser(String citizenID) {

        try {
            // Koppla upp mot databasen
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();
            String query = "DELETE FROM users WHERE citizenID = " + citizenID;

            // Kör SQL-queryn för att skapa en ny användare
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                System.out.println("The user with citizenID: " + citizenID + " has been deleted from our systems");
            } else {
                System.out.println("something went wrong line (221)");
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

    public static void deleteAccount(int accountID) {

        try {
            // Koppla upp mot databasen
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();
            String query = "DELETE FROM accounts WHERE id = " + accountID;

            // Kör SQL-queryn för att skapa en ny användare
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                System.out.println("The account with ID: " + accountID + " was deleted");
            } else {
                System.out.println("something went wrong (line 244)");
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

    public static void changePassword(String citizenID, String password, String newPassword) {

        try {
            // Koppla upp mot databasen
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();
            String query = "UPDATE users SET password = " + newPassword + " WHERE citizenID = " + citizenID + " AND password = " + password;

            // Kör SQL-queryn för att skapa en ny användare
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                System.out.println("The password has been changed for the user with citizenID: " + citizenID);
            } else {
                System.out.println("something went wrong (line 267)");
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }


    public static boolean createUser(String citizenID, String password) {

        try {
            Connection connection = GetConnection();

            if (checkIfUserAlreadyExists(citizenID)) {
                return false;
            }
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (citizenID, password) VALUES (?, ?)"
            );

            statement.setString(1, citizenID);
            statement.setString(2, password);

            int rowsAffected = statement.executeUpdate();

            connection.close();
            return rowsAffected > 0;

        } catch (SQLException e) {
            //PrintSQLException(e);
        }

        return false;
    }

    public static void showAccountPage(String accountIdAndBalance) {
        createTransactionsTableIfNotExists();
        String[] account = accountIdAndBalance.split(":");
        int accountID = Integer.parseInt(account[0]);
        System.out.println("accountID: " + account[0] + "   Balance: " + account[1]);
        System.out.println("1. Make a transaction");
        System.out.println("2. See transaction history");
        System.out.println("3. Delete account");
        System.out.println();

        System.out.print("Enter your chosen action: ");
        String choiceStr = scanner.nextLine().trim();

        switch (choiceStr) {
            case "1" -> {
                System.out.println("Enter which account you wish to send money to: ");
                String toAccount = scanner.nextLine().trim();
                System.out.println("Enter how much you would like to send: ");
                String amount = scanner.nextLine().trim();
                int toAccountID = Integer.parseInt(toAccount);
                int currentBalance = Integer.parseInt(account[1]);
                int amountInt = Integer.parseInt(amount);
                if (amountInt <= currentBalance && amountInt != 0) {
                    createTransaction(accountID, toAccountID, currentBalance - amountInt, amountInt);

                } else {
                    System.out.println("Not enough money in the account");

                }
            }
            case "2" -> {
                System.out.println("Which transactions (between which dates) would you like to view?");
                System.out.println("Enter from date (format: YYYYMMDD): ");
                String fromDate = scanner.nextLine().trim();
                System.out.println("Enter to date (format: YYYYMMDD): ");
                String toDate = scanner.nextLine().trim();
                String[] transactionsArr = getTransactionHistory(accountID, fromDate, toDate);
                for (String transaction : transactionsArr) {
                    System.out.println(transaction);
                }
            }
            case "3" -> deleteAccount(accountID);
            default -> {
            }
        }

    }

    public static void showUserMenu(String citizenID, String password) {
        createAccountsTableIfNotExists();
        System.out.println();
        System.out.println("Welcome user with citizenID: " + citizenID);
        System.out.println("To delete your entire user-account: enter 'delete' below");
        System.out.println();//If I want I can add some more options here
        System.out.println("1. create new account");
        System.out.println("2. change password");
        System.out.println("3. Log out");

        //String[] accounts = {"Hello : its me"};

        try {
            String[] accounts = getAccountsOfUser(citizenID);
            for (int i = 4; i < 4 + accounts.length; i++) {
                String[] account = accounts[i - 4].split(":");
                System.out.println(i + ". AccountID: " + account[0] + "  Balance: " + account[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("You don't have any accounts");

        }
        System.out.println();
        System.out.print("Enter your chosen action: ");
        String choiceStr = scanner.nextLine().trim();
        int choiceInt = 0;
        try {
            choiceInt = Integer.parseInt(choiceStr);

        } catch (NumberFormatException e) {
            //hello
        }

        switch (choiceStr) {

            case "1":
                createAccount(citizenID);
                break;

            case "2":
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine().trim();
                changePassword(citizenID, password, newPassword);
                break;

            case "3":
                break;

            case "delete":
                deleteUser(citizenID);
                break;

            default:
                try {
                    String[] accounts = getAccountsOfUser(citizenID);
                    if (choiceInt - 4 < accounts.length && choiceInt > 3) {
                        showAccountPage(accounts[choiceInt - 4]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //hello
                }
                break;
        }
    }

    public static String[] getTransactionHistory(int accountID, String fromDate, String toDate) {
        StringBuilder transactions = new StringBuilder();

        try {
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();

            String query = "SELECT * FROM transactions WHERE (fromAccount = " + accountID + " OR toAccount = " + accountID + ") AND made between '" + fromDate + "' AND '" + toDate + "' ORDER BY made";

            ResultSet result = statement.executeQuery(query);
            boolean neverInWHileLoop = true;
            while (result.next()) {
                neverInWHileLoop = false;
                String transaction = "id: " + result.getInt("id") + ", from: " + result.getInt("fromAccount") + ", to: " + result.getInt("toAccount") + ", amount: " + result.getInt("amount") + ", made: " + result.getDate("made") + "_";
                transactions.append(transaction);
            }
            if (neverInWHileLoop) {
                System.out.println("No transactions made at that time period and with this account");
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
        return transactions.toString().split("_");
    }

    public static String[] getAccountsOfUser(String citizenID) {

        StringBuilder accountsOfUserStr = new StringBuilder();
        try {
            Connection connection = GetConnection();

            String query = "SELECT * FROM accounts WHERE citizenID= ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, citizenID);
            ResultSet result = statement.executeQuery();
            int amountOfTimesInWhileLoop = 0;

            while (result.next()) {
                amountOfTimesInWhileLoop++;
                String accountIdAndBalance = result.getInt("id") + ":" + result.getInt("balance") + ",";
                accountsOfUserStr.append(accountIdAndBalance);
            }

            if (amountOfTimesInWhileLoop == 0) {
                String[] noAccounts = new String[0];
                connection.close();
                return noAccounts;
            }

            connection.close();
        } catch (SQLException e) {
            //PrintSQLException(e);
        }
        return accountsOfUserStr.toString().split(",");
    }

    public static void login() {

            System.out.print("Enter your citizenID (10 digits): ");
            String citizenID = scanner.nextLine().trim().replaceAll("-", "");
            if(citizenID.length() != 10 || !citizenID.matches("\\d+")) {
            System.out.println("The citizenID needs to contain 10 numbers and nothing more (except for a cheeky hyphen if you wish)");
                return;
            }
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();
            boolean correctCredentials = checkCredentials(citizenID, password);
            if (correctCredentials) {
                showUserMenu(citizenID, password);
            }
    }

    public static void taskCreateUser() {

            System.out.print("Enter your citizenID (10 numbers): ");
            String citizenID = scanner.nextLine().trim().replaceAll("-", "");
        if(citizenID.length() != 10 || !citizenID.matches("\\d+")) {
                System.out.println("The citizenID needs to contain 10 numbers and nothing more (except for a cheeky hyphen if you wish)");
                return;
            }
        System.out.print("Enter desired password: ");
            String password = scanner.nextLine().trim();
        boolean result = createUser(citizenID, password);
        if (result) {
            System.out.println("User " + citizenID + " (" + password + ") has been created");
        } else {
            System.out.println("An error occurred, user " + citizenID + " (" + password + ") could not be created");
        }
    }

    //Skapar en tillfällig koppling till databasen
    public static Connection GetConnection() {
        try {
            //System.out.print("Fetching connection to database...");
            Connection connection = dataSource.getConnection();
            //System.out.print("done!\n");
            return connection;
        } catch (SQLException e) {
            //System.out.print("failed!\n");
            //PrintSQLException(e);
            System.exit(0);
            return null;
        }
    }

    public static void main(String[] args) {
        //Här ska vi kalla på en massa funktioner som skapar rätt tabeller ifall dem inte finns
        scanner = new Scanner(System.in);
        InitializeDatabase();
        createUserTableIfNotExists();
        boolean run = true;

        while (run) {
            System.out.println();
            System.out.println();
            System.out.println("Welcome to Swosh! ");
            System.out.println("1. Register new user");
            System.out.println("2. Login");
            System.out.println("3. Avsluta");
            System.out.print("Enter your chosen action: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> taskCreateUser();
                case "2" ->
                    //Login branch
                        login();
                case "3" -> run = false;
                default -> {
                }
            }
        }
    }

    public static void createUserTableIfNotExists() {
        // Koppla upp mot databasen
        try {
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS users (citizenID VARCHAR(10) NOT NULL, password VARCHAR(50) NOT NULL)";

            int result = statement.executeUpdate(query);
            connection.close();

        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

    public static void createAccountsTableIfNotExists() {
        // Koppla upp mot databasen
        try {
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS accounts (id INT PRIMARY KEY AUTO_INCREMENT, citizenID VARCHAR(10) NOT NULL, balance INT DEFAULT 250)";
            int result = statement.executeUpdate(query);
            connection.close();

        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

    public static void createTransactionsTableIfNotExists() {
        // Koppla upp mot databasen
        try {
            Connection connection = GetConnection();

            Statement statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS transactions (id INT PRIMARY KEY AUTO_INCREMENT, fromAccount INT NOT NULL, toAccount INT NOT NULL, amount INT NOT NULL, made DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)";

            int result = statement.executeUpdate(query);
            connection.close();

        } catch (SQLException e) {
            //PrintSQLException(e);
        }
    }

}