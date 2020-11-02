package rental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class RentalServer {

	private final static int LOCAL = 0;
	private final static int REMOTE = 1;

	public static void main(String[] args) throws ReservationException, NumberFormatException, IOException {

		System.out.println("\n=============== Starting Rental Server Process ===============\n");

		if (System.getSecurityManager() != null) {
			System.setSecurityManager(null);
		}
		// The first argument passed to the `main` method (if present)
		// indicates whether the application is run on the remote setup or not.
		int localOrRemote = (args.length == 1 && args[0].equals("REMOTE")) ? REMOTE : LOCAL;

		CrcData company1 = loadData("hertz.csv");
		setupCarRentalCompany(company1);
		CrcData company2 = loadData("dockx.csv");
		setupCarRentalCompany(company2);

	}

	private static void setupCarRentalCompany(CrcData input) {
		ICarRentalCompany company = new CarRentalCompany(input.name, input.regions, input.cars);

		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			System.exit(-1);
		}
		System.out.println("Registering the Car Rental Compnay <" + input.name + "> now");
		// convert to stub
		ICarRentalCompany stub;
		try {
			stub = (ICarRentalCompany) UnicastRemoteObject.exportObject(company, 0);
			registry.rebind(input.name, stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public static CrcData loadData(String datafile) throws ReservationException, NumberFormatException, IOException {

		CrcData out = new CrcData();
		int nextuid = 0;

		// open file
		InputStream stream = MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(datafile);
		if (stream == null) {
			System.err.println("Could not find data file " + datafile);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		StringTokenizer csvReader;

		try {
			// while next line exists
			while (in.ready()) {
				String line = in.readLine();

				if (line.startsWith("#")) {
					// comment -> skip
				} else if (line.startsWith("-")) {
					csvReader = new StringTokenizer(line.substring(1), ",");
					out.name = csvReader.nextToken();
					out.regions = Arrays.asList(csvReader.nextToken().split(":"));
				} else {
					// tokenize on ,
					csvReader = new StringTokenizer(line, ",");
					// create new car type from first 5 fields
					CarType type = new CarType(csvReader.nextToken(), Integer.parseInt(csvReader.nextToken()),
							Float.parseFloat(csvReader.nextToken()), Double.parseDouble(csvReader.nextToken()),
							Boolean.parseBoolean(csvReader.nextToken()));
					System.out.println(type);
					// create N new cars with given type, where N is the 5th field
					for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
						out.cars.add(new Car(nextuid++, type));
					}
				}
			}
		} finally {
			in.close();
		}

		return out;
	}

	static class CrcData {
		public List<Car> cars = new LinkedList<Car>();
		public String name;
		public List<String> regions = new LinkedList<String>();
	}

}
