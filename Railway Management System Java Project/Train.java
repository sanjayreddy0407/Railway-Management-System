
public class Train {
    private String trainNumber;
    private String trainName;
    private String departureTime;
    private int availableSeats;

    public Train(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Train(String trainNumber, String trainName, String departureTime, int availableSeats) {// constructor
        // defined to check the train schedules
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
    }

    // implementing all the getter methods for all the private variables and setter
    // method for available seats
    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int seats) {
        this.availableSeats = seats;
    }
}
