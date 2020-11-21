package rental;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Car {

    @Id
    @GeneratedValue
    private int id;
    @ManyToOne(cascade = ALL, fetch = FetchType.EAGER)
    private CarType type;
    @OneToMany(cascade = ALL, mappedBy = "Car", fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<Reservation>();

    /**
     * *************
     * CONSTRUCTOR *
     **************
     */
    public Car() {
    }

    public Car(CarType type) {
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }

    /**
     * ****
     * ID *
     *****
     */
    public int getId() {
        return id;
    }

    /**
     * **********
     * CAR TYPE *
     ***********
     */
    public CarType getType() {
        return type;
    }

    public void setType(CarType type) {
        this.type = type;
    }

    /**
     * **************
     * RESERVATIONS *
     ***************
     */

    public boolean isAvailable(Date start, Date end) {
        if (!start.before(end)) {
            throw new IllegalArgumentException("Illegal given period");
        }

        for (Reservation reservation : reservations) {
            if (reservation.getEndDate().before(start) || reservation.getStartDate().after(end)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public void addReservation(Reservation res) {
        reservations.add(res);
    }

    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

}
