package datamodel;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RouteEditor {

	/*
	 * this is a cache of the route and stops used for editing and creating routes
	 * updates the database using the sync method
	 */
	private int maxStopID, RouteID;
	private String RouteName;
	private SQLconnection Database;
	private Hashtable<Integer, Stop> Stops;
	private LinkedList<StopListing> Route;
	private boolean structureChanged=false;//indicates if rows have been deleted or inserted in the Route.
	
	public RouteEditor(SQLconnection db, int routeID) throws SQLException {
		//create local data storage
		RouteID=routeID;
		Database=db;
		Stops=new Hashtable<Integer, Stop>();
		Route=new LinkedList<StopListing>();
		//get name
		ResultSet rname=db.getData("SELECT RouteName FROM RouteNames WHERE RouteID="+routeID);
		rname.next();
		RouteName=rname.getString(1);
		rname.close();
		//populate local data storage
		maxStopID=0;
		ResultSet stops=db.getData("SELECT * FROM stops");
		int id; String name, adrs;//local variables
		while (stops.next())
		{
			id=stops.getInt("StopID");
			name=stops.getString("StopName");
			adrs=stops.getString("Address");
			Stops.put(id, new Stop(id,name,adrs));
			if (maxStopID<id)
				maxStopID=id;
		}
		stops.close();
		ResultSet route=db.getData("SELECT StopID FROM Routes WHERE RouteID="+RouteID+" ORDER BY StopNumber");
		while(route.next())//StopNumber is stored using the structure of Route, the ORDER BY clause is critical
		{
			id=route.getInt(1);
			Route.add(new StopListing(id));
		}
	}
	public void sync() throws SQLException
	{
		//https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
		Database.beginTransaction();
		try {

			//updates the SQL database with the current contents of the route editor
			for (Stop s: Stops.values())
			{
				//check if the stop needs inserted or updated
				if(s.insert)
				{
					Database.execute("INSERT INTO Stops (StopID, StopName, Address)"
							+ "VALUES ( "+s.StopID+",'"+s.StopName+"','"+s.Address+"')");
					s.update=false;
					s.insert=false;
				}else if(s.update) {
					Database.execute("UPDATE Stops"
							+ "SET StopName='"+s.StopName+"', Address=\'"+s.Address+"' "
							+ "WHERE StopID="+s.StopID);
					s.update=false;
				}
			}//end stops loop
			//update the Route
			if (this.structureChanged)
			{
				//clear and populate route in database
				Database.execute("DELETE FROM Routes WHERE RouteID="+RouteID);
				for (int stopNumber=0;stopNumber<Route.size();stopNumber++)
				{
					Database.execute("INSERT INTO Routes (RouteID, StopNumber, StopID) "
							+ "VALUES ("+RouteID+","+stopNumber+","+Route.get(stopNumber).StopID+")" );
					Route.get(stopNumber).update=false;
				}//end populate route 
			}else {
				for (int stopNumber=0;stopNumber<Route.size();stopNumber++)
				{
					StopListing l=Route.get(stopNumber);
					if(l.update)
					{
						Database.execute("UPDATE Routes "
								+ "SET StopID="+l.StopID+" "
										+ "WHERE RouteID="+RouteID+" AND StopNumber="+stopNumber);
						l.update=false;
					}
				}//end populate route 
			}//end update route
			//Commit
			Database.comitTransaction();
		} catch (SQLException e) {
			Database.rollbackTransaction();
			throw e;//push the exception up to the next exception handler
		}finally {
			Database.afterTransaction();
		}
	}//end update
	//pass full contents by iterators, any changes must be monitored
	public Iterator<Stop> getStops()
	{
		return Stops.values().iterator();
	}
	public Iterator<Stop> getRoute()
	{
		LinkedList<Stop> route=new LinkedList<Stop>();
		for (StopListing l:Route)
		{
			route.add(Stops.get(l.StopID));
		}
		return route.iterator();
	}
	//getters
	public Stop getStopByID(int id)
	{
		return Stops.get(id);
	}
	public Stop getStopAt(int index)
	{
		int id=Route.get(index).StopID;
		return getStopByID(id);
	}
	//setters
	public void setStopAt(int index, Stop s)
	{
		StopListing sl=Route.get(index);
		sl.StopID=s.StopID;
		sl.update=true;
		if (! Stops.containsValue(s))
		{
			Stops.put(s.StopID, s);
			s.update=true;
		}
	}
	public void swapStops(int index1, int index2)
	{
		StopListing sl1=Route.get(index1);
		StopListing sl2=Route.get(index2);
		int id=sl1.StopID;
		sl1.StopID=sl2.StopID;
		sl2.StopID=id;
		sl1.update=true;
		sl2.update=true;
	}
	public Stop createStop(String name, String address)
	{
		Stop s= new Stop(name,address);
		Stops.put(s.StopID, s);
		return s;
	}
	public void insertStop(int index, Stop s)
	{
		Route.add(index, new StopListing(s.StopID));
		this.structureChanged=true;
	}
	public void deleteStopAt(int index)
	{
		Route.remove(index);
		this.structureChanged=true;
	}
	
	private int getNewStopID()
	{
		maxStopID++;
		return maxStopID;
	}
	private class StopListing
	{
		//represents a row of the Routes table. 
		private StopListing(int id)
		{
			StopID=id;
			update=false;
		}
		int StopID; 
		boolean update;
	}
	public class Stop
	{
		private boolean insert, update;//indicated if the database needs updated or inserted
		private int StopID;
		private String StopName, Address;
		
		private Stop(int id, String name, String address)
		{
			StopID=id;
			StopName=name;
			Address=address;
			insert=false;
			update=false;
		}
		private Stop(String name, String address)
		{
			StopID=getNewStopID();
			StopName=name;
			Address=address.replace("\\", "").replace("'", "");//protect sql from escape characters
			insert=true;
		}
		public void setName(String name)
		{
			StopName=name;
			update=true;
		}
		public void setAddress(String address)
		{
			Address=address.replace("\\", "").replace("\"", "");//protect sql from escape characters
			update=true;
		}
		public String getName()
		{
			return StopName;
		}
		public String getAddress()
		{
			return Address;
		}
	}//end stop
}
