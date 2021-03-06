package rental;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarRentalCompany implements ICarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());

	private List<String> regions;
	private String name;
	private List<Car> cars;
	private Map<String, CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
		logger.log(Level.INFO, "Car Rental Company <{0}> starting up...", name);
		setName(name);
		this.cars = cars;
		setRegions(regions);
		for (Car car : cars)
			carTypes.put(car.getType().getName(), car.getType());
		logger.log(Level.INFO, this.toString());
	}

	/********
	 * NAME *
	 ********/

	@Override
	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/***********
	 * Regions *
	 **********/
	private void setRegions(List<String> regions) {
		this.regions = regions;
	}

	public List<String> getRegions() {
		return this.regions;
	}

	public boolean operatesInRegion(String region) {
		return this.regions.contains(region);
	}

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		return carTypes.values();
	}

	public CarType getCarType(String carTypeName) {
		if (carTypes.containsKey(carTypeName))
			return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}

	// mark
	@Override
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { name, carTypeName });

		if (carTypes.containsKey(carTypeName)) {
			// logger.log(Level.INFO, "< Checking availability for car type {0}",
			// getAvailableCarTypes(start, end));
			return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
		} else {
			return false;
		}
	}

	@Override
	public CarType getCheapestCarType(Date start, Date end) {
		double price = 10000;
		CarType cheapest = null;
		for (CarType type : this.getAvailableCarTypes(start, end)) {
			if (price > type.getRentalPricePerDay()) {
				price = type.getRentalPricePerDay();
				cheapest = type;
			}
		}
		return cheapest;
	}

	public int getNumberOfReservationsFOrType(String type) {
		int result = 0;
		for (Car car : this.cars) {
			if (car.getType().getName().equals(type)) {
				result = result + car.getReservations().size();
			}
		}
		return result;
	}

	@Override
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}
		return availableCarTypes;
	}

	/*********
	 * CARS *
	 *********/

	private Car getCar(int uid) {
		for (Car car : cars) {
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}

	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (Car car : cars) {
			if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
				availableCars.add(car);
			}
		}
		return availableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public boolean canReserve(ReservationConstraints constraints) {
		System.out.println("checking for cartype: " + constraints.getCarType() + " in "+name);
		boolean result = operatesInRegion(constraints.getRegion());
		result = result && isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate());
		return result;
	}

	public double getRentalPricePerDay(String typeName) {
		if (this.carTypes.containsKey(typeName)) {
			return this.carTypes.get(typeName).getRentalPricePerDay();
		}
		return 0;
	}

	public void cancelReservation(Reservation reservation) {
		int carID = reservation.getCarId();
		for (Car car : this.cars) {
			if (car.getId() == carID) {
				synchronized (car) {
					car.removeReservation(reservation);
				}
			}
		}
	}

	public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}\n",
				new Object[] { name, client, constraints.toString() });

		if (!operatesInRegion(constraints.getRegion())
				|| !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name + "> No cars available to satisfy the given constraints.");

		CarType type = getCarType(constraints.getCarType());

		double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(),
				constraints.getEndDate());

		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(),
				constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
	}

	@Override
	public synchronized Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}\n", new Object[] { name, quote.toString() });
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if (availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int) (Math.random() * availableCars.size()));

		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	public Set<Reservation> getReservationsFromRenter(String client) {
		Set<Reservation> reservations = new HashSet<Reservation>();
		for (Car car : this.cars) {
			for (Reservation reservation : car.getReservationsFromClient(client)) {
				System.out.println("reservation " + client + "  " + reservation.getCarType());
				reservations.add(reservation);
			}
		}
		return reservations;
	}

	public int getNumberOfReservationsFromRenter(String client) {
		return this.getReservationsFromRenter(client).size();
	}

	public Set<Reservation> getReservationsForCarType(String type) {
		Set<Reservation> reservations = new HashSet<Reservation>();
		for (Car car : this.cars) {
			if (car.getType().getName().equals(type)) {
				for (Reservation reservation : car.getReservations()) {
					reservations.add(reservation);
				}
			}
		}
		return reservations;
	}

	public int getNumberOfReservationsForType(String client) {
		return this.getReservationsForCarType(client).size();
	}

	// @SuppressWarnings("deprecation")
	public CarType getMostPopularCarTypeInYear(int year) throws RemoteException {
		int number = 0;
		CarType cartype = null;

		for (CarType car : getAllCarTypes()) {
			Set<Reservation> reservations = getReservationsForCarType(car.getName());
			for (Reservation res : reservations) {
				if (res.getEndDate().getYear() + 1900 != year) {
					reservations.remove(res);
				}
			}
			if (number < reservations.size()) {
				number = reservations.size();
				cartype = car;
			}
		}
		return cartype;
	}

	@Override
	public String toString() {
		return String.format("<%s> CRC is active in regions %s and serving with %d car types\n", name,
				listToString(regions), carTypes.size());
	}

	private static String listToString(List<? extends Object> input) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < input.size(); i++) {
			if (i == input.size() - 1) {
				out.append(input.get(i).toString());
			} else {
				out.append(input.get(i).toString() + ", ");
			}
		}
		return out.toString();
	}

}
