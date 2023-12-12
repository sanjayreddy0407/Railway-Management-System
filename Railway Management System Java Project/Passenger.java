import java.util.Date;

public class Passenger {
    private String firstname;
    private String lastname;
    private String dob; // all these variables are private to not be modified by members of other
                        // classes

    public Passenger(String firstname, String lastname, String dob) {// this constructor is defined to
        // retrieve the passenger details for displaying the passengerDetails
        this.firstname = firstname;
        this.lastname = lastname;
        this.dob = dob;
    }

    public Passenger(String firstname, String lastname) {// this constructor is defined to access the firstname
        // and lastname for ticket cancellation
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }// implementing all the getter methods.

    public String getLastname() {
        return lastname;
    }

    public String getDob() {
        return dob;
    }
}
