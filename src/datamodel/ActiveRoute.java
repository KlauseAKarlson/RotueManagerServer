package datamodel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActiveRoute {

	/*
	 * this class acts as a cached copy of a bus route
	 * it is designed for the safe serialization of bus route over network with maximum data control and encapsulation
	 */
	private LinkedList<BusStop> Route;
	public ActiveRoute(SQLconnection con, int RouteID) throws SQLException {
		//query to get route data
		ResultSet stops=con.getData("SELECT StopID, StopName, Address FROM ActiveRoutes WHERE RouteID="+RouteID+" ORDER BY StopNumber");
		//populate stops into linked list
		Route=new LinkedList<BusStop>();
		int ID;
		String Name, Address;
		while (stops.next())
		{
			ID=stops.getInt("StopID");
			Name=stops.getString("StopName");
			Address=stops.getString("Address");
			Route.add(new BusStop( Name==null?"Stop #"+ID : Name ,Address));
		}
	}
	public ActiveRoute(String CSV)
	{
		Route=new LinkedList<BusStop>();
		String Line, Name, Address;
		int seperator;
		try {
			BufferedReader input=new BufferedReader(new StringReader(CSV));
			Line=input.readLine();
			while (Line!=null)
			{
				System.out.println(Line);
				seperator=Line.indexOf(';');
				Name=Line.substring(0, seperator);
				Address=Line.substring(seperator+1);
				Route.add(new BusStop(Name,Address));
				Line=input.readLine();
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public BusStop getStop(int index)
	{
		return Route.get(index);
	}
	
	public String getStopName(int index)
	{
		return Route.get(index).getName();
	}

	public String getStopAddress(int index)
	{
		return Route.get(index).getAddress();
	}
	public int size()
	{
		return Route.size();
	}

	public Iterator<BusStop> getIterator()
	{
		return Route.iterator();
	}
	public String getCSV()
	{
		//returns a serialization of the route as a semicolon separated CSV string
		StringBuffer CSV=new StringBuffer();
		for (BusStop stop:Route)
		{
			CSV.append(stop.getName()+";"+stop.getAddress()+"\n");
		}
		return CSV.toString();
	}
	public class BusStop{
		private String StopName, Address;
		private BusStop(String Name, String Address)
		{
			this.StopName=Name;
			this.Address=Address;
		}
		public String getName()
		{
			return StopName;
		}
		public String getAddress()
		{
			return Address;
		}
	}
}
