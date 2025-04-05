import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    private static final String CONN_STRING = "jdbc:mysql://localhost:3306/FoodOrder";
    public static void main(String[] args) {
        var dataSource = new MysqlDataSource();
        dataSource.setURL(CONN_STRING);

        try (Connection connection = dataSource.getConnection(System.getenv("USER"),
                System.getenv("PASS"))) {

//            addCustomer(connection,"Trong", "trong@gmail.com");
//            allDishes(connection, "Customers");
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

    private static void createOrder(Connection connection) {

    }

//    private static void getOrder(Connection connection, String customerName) throws SQLException {
//        String query = "select D.name, D.price , O.order_date, OI.quantity" +
//                "from OrderItems OI" +
//                "join Dishes D on OI.dish_id = D.id" +
//                "join Orders O on OI.order_id" +
//                "where Customers.name = ?";
//
//        try {
//            PreparedStatement statement = connection.prepareStatement(query);
//            connection.setAutoCommit(false);
//            statement.setString(1, customerName);
//            var result = statement.executeQuery();
//            printRecord(result);
//            connection.commit();
//
//        } catch (SQLException e) {
//            connection.rollback();
//            throw new RuntimeException(e);
//        }


    }

}
