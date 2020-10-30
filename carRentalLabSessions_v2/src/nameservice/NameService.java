package nameservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


import rental.ICarRentalCompany;
import rental.CarRentalCompany;
import agency.ICarRentalAgency;
import agency.CarRentalAgency;


public class NameService implements INameService{
	
	private List<String> carRentalCompanyList;
	private Registry registry = null;
	private List<String> clientList;

	public NameService() {
		this.carRentalCompanyList = new ArrayList<String>();
		this.clientList = new ArrayList<String>();
		try {
			this.registry = LocateRegistry.getRegistry();
		} catch (RemoteException e) {
			System.exit(-1);
		}
	}

	public List<String> getAllClients() {
		return this.clientList.copy();
	}

	public boolean isNewClient(String client) {
		return this.clientList.contains(client);
	}

	public void addNewClient(String client) {
		if (this.isNewClient(client)) {
			this.clientList.add(client);
		}
	}

	@Override
	public void registerCRC(String company) {
		if (!this.carRentalCompanyList.contains(company)) {
			this.carRentalCompanyList.add(company);
		}
	}

	@Override
	public void unregisterCRC(String company) {
		this.carRentalCompanyList.remove(company);
	}

	@Override
	public List<String> getRegisteredCRCList() {
		return this.carRentalCompanyList;
	}

	@Override
	public ICarRentalCompany getRegisteredCRCStub(String company) {
		if (this.carRentalCompanyList.contains(company)) {
			try {
				ICarRentalCompany companyStub = (ICarRentalCompany) this.registry.lookup(company);
				return companyStub;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public List<String> getAllRegisteredCRCNames() {
		return new ArrayList<>(this.carRentalCompanyList);
	}
	
	@Override
	public List<ICarRentalCompany> getAllRegisteredCRCStubs() {
		List<ICarRentalCompany> result = new ArrayList<ICarRentalCompany>();
		for (String name : this.carRentalCompanyList) {
			result.add(this.getRegisteredCRCStub(name));
		}
		return result;
	}
}
