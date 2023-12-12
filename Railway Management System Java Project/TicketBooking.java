import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TicketBooking {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/railway_reservation";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root1234";

    private Map<String, Train> trainMap;
    private List<Passenger> passengers;

    public TicketBooking() {
        trainMap = new HashMap<>();
        passengers = new ArrayList<>();
    }

    public void addTrain(Train train) {
        trainMap.put(train.getTrainNumber(), train);
    }

    public void bookTicket(String trainNumber, Passenger passenger) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            connection.setAutoCommit(false);

            if (trainExists(connection, trainNumber)) {
                String insertPassengerQuery = "INSERT INTO passenger (first_name, last_name, dob) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertPassengerQuery,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, passenger.getFirstname());
                    statement.setString(2, passenger.getLastname());
                    statement.setString(3, passenger.getDob());
                    statement.executeUpdate();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int passengerId = generatedKeys.getInt(1);

                            String insertTicketQuery = "INSERT INTO ticket (passenger_id, train_number) VALUES (?, ?)";
                            try (PreparedStatement ticketStatement = connection.prepareStatement(insertTicketQuery)) {
                                ticketStatement.setInt(1, passengerId);
                                ticketStatement.setString(2, trainNumber);
                                ticketStatement.executeUpdate();
                            }

                            String updateTrainQuery = "UPDATE train SET available_seats = available_seats - 1 WHERE train_number = ?";
                            try (PreparedStatement updateTrainStatement = connection
                                    .prepareStatement(updateTrainQuery)) {
                                updateTrainStatement.setString(1, trainNumber);
                                updateTrainStatement.executeUpdate();
                            }

                            connection.commit();
                            System.out.println(
                                    "Ticket booked for " + passenger.getFirstname() + " " + passenger.getLastname() +
                                            " on train " + trainNumber);
                        } else {
                            connection.rollback();
                            System.out.println("Error getting passenger ID, transaction rolled back.");
                        }
                    }
                }
            } else {
                System.out.println("Invalid train number.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database or booking the ticket. Transaction rolled back.");
        }
    }

    private boolean trainExists(Connection connection, String trainNumber) throws SQLException {
        String query = "SELECT 1 FROM train WHERE train_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, trainNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int getAvailableSeats(String trainNumber) {
        Train train = trainMap.get(trainNumber);
        if (train != null) {
            return train.getAvailableSeats();
        } else {
            System.out.println("Invalid train number.");
            return -1;
        }
    }

    public void displayTrainSchedules() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT train_number, train_name, departure_time, available_seats FROM train";
            try (PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery()) {

                System.out.println("---------------------------------------");
                System.out.println("Train Schedules:");

                while (resultSet.next()) {
                    String trainNumber = resultSet.getString("train_number");
                    String trainName = resultSet.getString("train_name");
                    String departureTime = resultSet.getString("departure_time");
                    int availableSeats = resultSet.getInt("available_seats");

                    System.out.println(trainNumber + " - " + trainName +
                            " (Departure Time: " + departureTime + ")" +
                            " Available seats: " + availableSeats);
                }

                System.out.println("---------------------------------------");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error executing the query.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database.");
        }
    }

    public void displayPassengerDetails() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT p.first_name, p.last_name, p.dob, t.train_number " +
                    "FROM passenger p JOIN ticket t ON p.id = t.passenger_id";
            try (PreparedStatement statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery()) {

                System.out.println("---------------------------------------");
                System.out.println("Passenger Details:");
                int i = 1;
                while (resultSet.next()) {
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String dob = resultSet.getString("dob");
                    String trainNumber = resultSet.getString("train_number");

                    System.out.println(i + ". Name: " + firstName + " " + lastName +
                            "\n   Date Of Birth: " + dob +
                            "\n   Train Number: " + trainNumber);
                    System.out.println();
                    i++;
                }
                System.out.println("---------------------------------------");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error executing the query.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database.");
        }
    }

    public void cancelTicket(String firstName, String lastName, String trainNumber) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String deleteTicketQuery = "DELETE FROM ticket WHERE passenger_id = " +
                    "(SELECT id FROM passenger WHERE first_name = ? AND last_name = ?) AND train_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteTicketQuery)) {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, trainNumber);
                statement.executeUpdate();
            }

            String updateTrainQuery = "UPDATE train SET available_seats = available_seats + 1 WHERE train_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateTrainQuery)) {
                statement.setString(1, trainNumber);
                statement.executeUpdate();
            }

            System.out.println("Ticket canceled for " + firstName + " " + lastName);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the database or canceling the ticket.");
        }
    }
}
