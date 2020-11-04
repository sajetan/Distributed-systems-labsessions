package client;

import javax.ejb.EJB;
import session.ReservationSessionRemote;
import java.util.Date;
import rental.ReservationConstraints;
import rental.ReservationException;
import session.ManagerSessionRemote;

public class Main {
    
    @EJB
    static ReservationSessionRemote session;
    
    @EJB
    static ManagerSessionRemote      sessionM;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        session.initialize("Yoshi");
        System.out.println("found rental companies: "+session.getAllRentalCompanies());
        System.out.println("found available car types: "+session.getAvailableCarTypes(new Date(2020,10,3), new Date(2020,10,20)));
        System.out.println("created this quote:" + session.createQuote(new ReservationConstraints(new Date(2020,10,3), new Date(2020,10,20),"Top","FlemishBrabant")) );
        System.out.println("created this quote:" + session.createQuote(new ReservationConstraints(new Date(2020,11,3), new Date(2020,11,20),"Top","FlemishBrabant")) );
                try 
        {
            System.out.println("Reservations:      " + session.confirmQuotes());
        }
        catch(ReservationException e )
        {
            System.out.println("Reservations: problem");
        }
     }
    
}