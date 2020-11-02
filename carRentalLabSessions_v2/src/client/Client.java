package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import rental.CarRentalCompany;
import rental.CarType;
import rental.ICarRentalCompany;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import agency.IReservationSession;
import agency.IManagerSession;
import agency.ICarRentalAgency;

public class Client extends AbstractTestManagement<IReservationSession, IManagerSession> {

	/********
	 * MAIN *
	 ********/
	
	private static Logger logger = Logger.getLogger(Client.class.getName());
	private Registry registry = null;
	private ICarRentalAgency rentalAgency = null;
	private static ICarRentalCompany company = null;
	private final static int LOCAL = 0;
	private final static int REMOTE = 1;


	/**
	 * The `main` method is used to launch the client application and run the test
	 * script.
	 */
	public static void main(String[] args) throws Exception {
				
		if (System.getSecurityManager() != null)
			System.setSecurityManager(null);
		

		System.out.println("\n=============== Starting Client Process ===============\n");

		// The first argument passed to the `main` method (if present)
		// indicates whether the application is run on the remote setup or not.
		int localOrRemote = (args.length == 1 && args[0].equals("REMOTE")) ? REMOTE : LOCAL;

		String carRentalAgency = "testagency";
//		String carRentalCompanyName = "Hertz";

		Client client = new Client("trips", carRentalAgency, localOrRemote);
		client.setupManager();	
		client.run();

		
		// An example reservation scenario on car rental company 'Hertz' would be...
//		Client client = new Client("simpleTrips", carRentalCompanyName, localOrRemote);
//		client.run();

		
	}

	/***************
	 * CONSTRUCTOR 
	 * @throws NotBoundException 
	 * @throws RemoteException *
	 ***************/

	public Client(String scriptFile, String carRentalAgency, int localOrRemote) throws NotBoundException, RemoteException {
		super(scriptFile);
		
		
		logger.log(Level.INFO, "Connecting with <{0}> ", carRentalAgency);
		
		try {
			this.registry = LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			System.exit(-1);
		}

		try {
			this.rentalAgency = (ICarRentalAgency) this.registry.lookup(carRentalAgency);
//			ICarRentalCompany  test= (ICarRentalCompany) registry.lookup("Dockx");
			logger.log(Level.INFO, "Connecting success");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		
		
	}
	
	

	// @Override
	protected IReservationSession getNewReservationSession(String client) throws Exception {
		logger.log(Level.INFO, "Inside getNewReservationSession");
		return this.rentalAgency.openReservationSession(client);
	}

	// @Override
	protected IManagerSession getNewManagerSession(String manager) throws Exception {
		logger.log(Level.INFO, "Inside getNewManagerSession");
		return this.rentalAgency.openManagerSession(manager);
	}

	// @Override
	protected void checkForAvailableCarTypes(IReservationSession session, Date start, Date end) throws Exception {
		System.out.println("************Available cars ***********");
		Map<String, Set<CarType>> cartypes = session.getAvailableCarTypes(start, end);
		System.out.println("		CRC     -   CarType");
		for (String company : cartypes.keySet()) {
			for (CarType carType : cartypes.get(company)) {
				System.out.println("		" + company + "   -   " + carType.getName());
			}
		}

		// TODO Auto-generated method stub
		
//		Set<CarType> availablecartypesdata= company.getAvailableCarTypes(start,end);
//		System.out.println("************Available cars 2***********");
//		for (CarType value : availablecartypesdata) { 
//            System.out.println(value); 
//        }		
		//throw new UnsupportedOperationException("TODO");
	}
        
    
	private void setupManager() throws Exception {
		System.out.println("Setting up manager");
		IManagerSession session = getNewManagerSession("manager");
		session.registerCompany("Hertz");
        session.registerCompany("Dockx");
	}
	
    //@Override
	protected void addQuoteToSession(IReservationSession session, String client, Date start, Date end, String carType, String region) throws Exception{
		ReservationConstraints rconstraints = new ReservationConstraints(start, end, carType, region);
		session.createQuote(rconstraints, client);	
	}
	
	//@Override
	protected List<Reservation> confirmQuotes(IReservationSession session, String client) throws Exception{
		return session.confirmQuotes();
	}

	// @Override
	protected int getNumberOfReservationsByRenter(IManagerSession ms, String clientName) throws Exception {
		return 0;
	}

	// @Override
	protected int getNumberOfReservationsForCarType(IManagerSession ms, String carRentalName, String carType)
			throws Exception {
		return 0;
	}

	protected Set<String> getBestClients(IManagerSession session) throws Exception {
		System.out.println("will get best clients");
		return session.getBestClients();
	}

	protected String getCheapestCarType(IReservationSession session, Date start, Date end, String region)
			throws Exception {
		return session.getCheapestCarType(start, end, region).getName();
	}

	protected CarType getMostPopularCarTypeInCRC(IManagerSession session, String carRentalCompanyName, int year)
			throws Exception {
		return null;
	}

//	/**
//	 * Retrieve a quote for a given car type (tentative reservation).
//	 * 
//	 * @param clientName name of the client
//	 * @param start      start time for the quote
//	 * @param end        end time for the quote
//	 * @param carType    type of car to be reserved
//	 * @param region     region in which car must be available
//	 * @return the newly created quote
//	 * 
//	 * @throws Exception if things go wrong, throw exception
//	 */
//	@Override
//	protected Quote createQuote(String clientName, Date start, Date end, String carType, String region)
//			throws Exception {
//		 System.out.println("************Creating Quote for <"+clientName+">***********");
//		 ReservationConstraints args = new ReservationConstraints(start, end, carType, region);
//		
//		 Quote quote_value= company.createQuote(args, clientName);
//		 System.out.println(quote_value);		 
//
//		return quote_value;
//		// TODO Auto-generated method stub
//		//throw new UnsupportedOperationException("TODO");
//	}
//
//	/**
//	 * Confirm the given quote to receive a final reservation of a car.
//	 * 
//	 * @param quote the quote to be confirmed
//	 * @return the final reservation of a car
//	 * 
//	 * @throws Exception if things go wrong, throw exception
//	 */
//	@Override
//	protected Reservation confirmQuote(Quote quote) throws Exception {
//		// TODO Auto-generated method stub
//		System.out.println("************Confirmation Quote Data ***********");
//		Reservation confirmation_data= company.confirmQuote(quote);
//		System.out.println(confirmation_data);
//		return confirmation_data;
////		throw new UnsupportedOperationException("TODO");
//	}
//
//	/**
//	 * Get all reservations made by the given client.
//	 *
//	 * @param clientName name of the client
//	 * @return the list of reservations of the given client
//	 * 
//	 * @throws Exception if things go wrong, throw exception
//	 */
//	@Override
////	protected List<Reservation> getReservationsByRenter(String clientName) throws Exception {
//	protected Set<Reservation> getReservationsByRenter(String clientName) throws Exception {	
//		
//		System.out.println("************Getting reservation data for <"+clientName+">***********");
//		Set<Reservation> getreservations_data = company.getReservationsFromRenter(clientName);				
//		return getreservations_data;
//	}
//
//	/**
//	 * Get the number of reservations for a particular car type.
//	 * 
//	 * @param carType name of the car type
//	 * @return number of reservations for the given car type
//	 * 
//	 * @throws Exception if things go wrong, throw exception
//	 */
//	@Override
//	protected int getNumberOfReservationsForCarType(String carType) throws Exception {
//		System.out.println("************Getting the number of reservations for <"+carType+">***********");
//		Set<Reservation> getreservationsforcar_data = company.getReservationsForCarType(carType);
//		//System.out.println(getreservationsforcar_data.size());
//		return getreservationsforcar_data.size();
//		//throw new UnsupportedOperationException("TODO");
//	}
}
