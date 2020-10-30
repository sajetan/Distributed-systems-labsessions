package agency;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import rental.CarType;

public interface IManagerSession extends Remote{
	
	public void registerCompany(String company) throws RemoteException;
	public void unregisterCompany(String company) throws RemoteException;
	public Set<String> getRegisteredCompanies() throws RemoteException;
	public int getNumberOfReservations(String company, String type);
	public Set<String> getBestClients() throws RemoteException;
	public int getNumberOfReservationsBy(String client) throws RemoteException;
	public CarType getMostPopularCarTypeInCRC(String Company, int year) throws RemoteException;
	
}
