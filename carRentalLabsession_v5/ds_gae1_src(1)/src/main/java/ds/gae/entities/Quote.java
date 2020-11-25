package ds.gae.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

public class Quote {

    private Date startDate;
    private Date endDate;
    private String renter;
    private String rentalCompany;
    private String carType;
    private double rentalPrice;

    /***************
     * CONSTRUCTOR *
     ***************/

    Quote(String renter, Date start, Date end, String rentalCompany, String carType, double rentalPrice) {
        this.renter = renter;
        this.startDate = start;
        this.endDate = end;
        this.rentalCompany = rentalCompany;
        this.carType = carType;
        this.rentalPrice = rentalPrice;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getRenter() {
        return renter;
    }

    public String getRentalCompany() {
        return rentalCompany;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public String getCarType() {
        return carType;
    }
    
    public Entity persist(Datastore ds)
    {
    	KeyFactory kf = ds.newKeyFactory().setKind("Quote")
    			          .addAncestors(PathElement.of("CarRentalCompany", this.rentalCompany));
    	Key k         = ds.allocateId(kf.newKey());
    	Entity q    = Entity.newBuilder(k)
    			        .set("renter", this.renter)
    			        .set("startDate", Timestamp.of(this.startDate))
    			        .set("endDate", Timestamp.of(this.endDate))
    			        .set("carType", this.carType)
    			        .set("rentalPrice", this.rentalPrice)
    			        .build();
    	ds.put(q);
    	return q;    	
    }

    /*************
     * TO STRING *
     *************/

    @Override
    public String toString() {
        return String.format(
                "Quote for %s from %s to %s at %s\nCar type: %s\tTotal price: %.2f",
                getRenter(),
                getStartDate(),
                getEndDate(),
                getRentalCompany(),
                getCarType(),
                getRentalPrice()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getRenter(),
                getStartDate(),
                getEndDate(),
                getRentalCompany(),
                getCarType(),
                getRentalPrice()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Quote other = (Quote) obj;
        if (!Objects.equals(renter, other.renter)) {
            return false;
        }
        if (!Objects.equals(startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(endDate, other.endDate)) {
            return false;
        }
        if (!Objects.equals(rentalCompany, other.rentalCompany)) {
            return false;
        }
        if (!Objects.equals(carType, other.carType)) {
            return false;
        }
        if (Double.doubleToLongBits(rentalPrice) != Double.doubleToLongBits(other.rentalPrice)) {
            return false;
        }
        return true;
    }
}
