package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import rental.CarType;
import rental.RentalStore;
import rental.CarRentalCompany;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class ReservationSession implements ReservationSessionRemote {
    
    private Set<Quote> quotes;
    private String     guest;
    
    
    @Override
    public void initialize(String guest_name)
    {
        if (this.guest == null)
        {
            this.guest = guest_name;
            this.quotes = new HashSet<Quote>();
        }
    }

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet<String>(RentalStore.getRentals().keySet());
    }
    
    @Override
    public Set<CarType> getAvailableCarTypes(Date start, Date end)
    {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (CarRentalCompany comp : RentalStore.getRentals().values())
        {
            availableCarTypes.addAll(comp.getAvailableCarTypes(start, end));
        }
        return availableCarTypes;
    }
    
    
    @Override
    public Quote createQuote(ReservationConstraints constraint)
    {
        List<CarRentalCompany> companies = new LinkedList<CarRentalCompany>(); 
        for (CarRentalCompany comp : RentalStore.getRentals().values())
        {
            if(comp.quoteCanBeCreated(constraint))
            {
                companies.add(comp);
            }
        }
        try 
        {
            Quote quote = companies.get((int)(Math.random()*companies.size())).createQuote(constraint, this.guest);
            this.quotes.add(quote);
            return quote; 
            
        }
        catch(ReservationException e)
        {
            return null;
        }
    }
    
    @Override
    public Set<Quote> getCurrentQuotes()
    {
        return new HashSet<Quote>(this.quotes);
    }
    
    @Override
    public Set<Reservation> confirmQuotes() throws ReservationException
    {
        Set<Reservation> reservations = new HashSet<Reservation>();
        for (Quote quote : this.quotes)
        {
            CarRentalCompany comp = RentalStore.getRental(quote.getRentalCompany());
            try
            {
                reservations.add(comp.confirmQuote(quote));
            }
            catch(ReservationException e)
            {
               for (Reservation resv :reservations)
               {
                   RentalStore.getRental(resv.getRentalCompany()).cancelReservation(resv);
               }
               throw e ; 
            }
        }
        return reservations;
    }
}
