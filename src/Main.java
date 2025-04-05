import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String CONN_STRING = "jdbc:mysql://localhost:3306/FoodOrder";
    private static final Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        var dataSource = new MysqlDataSource();
        dataSource.setURL(CONN_STRING);

        try (Connection connection = dataSource.getConnection(System.getenv("USER"),
                System.getenv("PASS"))) {

//            addCustomer(connection,"Trong", "trong@gmail.com");
//            allDishes(connection, "Dishes");
//            createOrder(connection, 3);
            getOrder(connection, "Trong");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printRecord(ResultSet resultSet) throws SQLException {
        var meta = resultSet.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.out.printf("%-30s", meta.getColumnName(i).toUpperCase());
        }
        System.out.println();
        while (resultSet.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.printf("%-30s", resultSet.getString(i));
            }
            System.out.println();
        }
    }
    private static void allDishes(Connection connection, String tableName) {
        String query = "select * from " + tableName;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            var result = statement.executeQuery();
            printRecord(result);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addCustomer(Connection connection, String name, String email) throws SQLException {
        String query = "insert into Customers(name,email) values (?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            connection.setAutoCommit(false);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
            System.out.println("Add successful");
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        }
    }

    private static void createOrder(Connection connection, int customerId) throws SQLException {
        String insertOrderSQL = "INSERT INTO Orders (customer_id, order_date) VALUES (?, CURDATE())";
        String insertItemSQL = "INSERT INTO OrderItems (order_id, dish_id, quantity) VALUES (?, ?, ?)";

        try (
                PreparedStatement orderStmt = connection.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement itemStmt = connection.prepareStatement(insertItemSQL)
        ) {
            connection.setAutoCommit(false);

            orderStmt.setInt(1, customerId);
            orderStmt.executeUpdate();

            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Create Fail!");
            }
            int orderId = generatedKeys.getInt(1);

            Map<Integer, Integer> dishQuantities = new HashMap<>();
            System.out.println("=== CREATE ORDER ===");
            while (true) {
                System.out.print("Enter id dish (0 to exit) : ");
                int dishId = sc.nextInt();
                if (dishId == 0) break;

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                dishQuantities.merge(dishId, quantity, Integer::sum);
            }

            for (Map.Entry<Integer, Integer> entry : dishQuantities.entrySet()) {
                int dishId = entry.getKey();
                int quantity = entry.getValue();

                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, dishId);
                itemStmt.setInt(3, quantity);
                itemStmt.addBatch();
            }

            itemStmt.executeBatch();

            System.out.println("Create successful");
            connection.commit();

        }

    }

    private static void getOrder(Connection connection, String customerName) throws SQLException {
        String query = "SELECT D.name, D.price, O.order_date, OI.quantity " +
                "FROM OrderItems OI " +
                "JOIN Dishes D ON OI.dish_id = D.id " +
                "JOIN Orders O ON OI.order_id = O.id " +
                "JOIN Customers C ON O.customer_id = C.id " +
                "WHERE C.name = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            connection.setAutoCommit(false);
            statement.setString(1, customerName);
            var result = statement.executeQuery();
            printRecord(result);
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        }
    }
}
