package datamodel;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Hashtable;
import java.util.LinkedList;

public class AssignmentEditor {

	/*
	 * the only variables that should be by using this class are those in the RouteAssignment table. 
	 * Everything else is used to contextualize information or provide a limited number of choices 
	 * maintian database consistency without throwing exceptions
	 */
	private Hashtable<Integer,Route> RouteNames;
	private Hashtable<Integer,Driver> Drivers;
	private LinkedList<Assignment> Assignments;//used to save all assginments back into the database
	private LinkedList<Assignment> Unassigned;//used to hold assignments without drivers in a side bar
	private SQLconnection Database;
	
	public AssignmentEditor(SQLconnection db) throws SQLException {
		Database=db;
		//load route names, then Driver assignements
		RouteNames=new Hashtable<Integer,Route>();
		ResultSet rnames=Database.getData("SELECT * FROM RouteNames");
		while (rnames.next())
		{
			Route r=new Route();
			r.RouteID=rnames.getInt("RouteID");
			r.name=rnames.getString("RouteName");
			RouteNames.put(r.RouteID, r);
		}
		rnames.close();
		//load assignements into memory
		Drivers =new Hashtable<Integer,Driver>();
		Assignments =new LinkedList<Assignment> ();
		Unassigned =new LinkedList<Assignment> ();
		ResultSet DriverAssignments = Database.getData("SELECT * FROM DriverAssignment ORDER BY DriverID");
		while (DriverAssignments.next())
		{
			int driverID=DriverAssignments.getInt("DriverID");
			int RouteID=DriverAssignments.getInt("RouteID");
			//JDBC returns 0 on null integers
			//we will check if each side of the join is null using the above integers
			Assignment a=null;
			if (RouteID != 0)
			{
				a = new Assignment(RouteID);//Assignment ctor adds it to linked lists
				//load and localize start/end times
				Time start=DriverAssignments.getTime("StartTime");
				a.Start = start==null?null:start.toLocalTime();
				Time end =DriverAssignments.getTime("EndTime");
				a.End=end==null?null:end.toLocalTime();
			}//end if route not null
			if (driverID != 0)
			{
				Driver d;
				if (! Drivers.containsKey(driverID))
				{
					d=new Driver(
							driverID, 
							DriverAssignments.getString("Name"),
							DriverAssignments.getDate("CDL_ExpirationDate")
							);
				}else {
					d=Drivers.get(driverID);
				}

				if (a!=null)
					a.assign(d);
			}//end if driver not null
		}//end while loop
	}//end ctor
	
	public void delete(Assignment a)
	{
		//removes the assignemnt from all containers, it will not be saved even if it persists in the GUI
		Assignments.remove(a);
		if (a.driver==null)
		{
			Unassigned.remove(a);
		}else {
			a.driver.Routes.remove(a);
		}
	}
	public Assignment createAssignment(int RouteID)
	{
		return new Assignment(RouteID);
	}
	
	
 	public class Route{
		private String name;
		private int RouteID;
		public String getName() {return name;}
		public String toString() {return name;}
		public int getRID() {return RouteID;}
	}//end route
	public class Driver {
		private String DriverName;
		private int DriverID;
		private Instant CDL_Exp;//expiration date of the driver's CDL
		private LinkedList<Assignment> Routes=new LinkedList<Assignment> ();
		
		private Driver (int id, String name, Date CDLE)
		{
			DriverID=id;
			DriverName=name;
			CDL_Exp= CDLE==null?null:CDLE.toInstant();
		}
		
		public boolean ExpiredCDL()
		{
			//return's false if the CTL is expired
			return CDL_Exp.isBefore(Instant.now());
		}
		public String getName() {return DriverName;}
		public int getID() {return DriverID;}
		public Instant getCDLExpirationDate() {return CDL_Exp;}
		public LinkedList<Assignment> getRoutes(){return Routes;}

	}///end driver
	public class Assignment{
		private int RouteID;
		private String RouteName;
		private Driver driver;
		private LocalTime Start,End;

		private Assignment(int routeID)
		{
			RouteID=routeID;
			RouteName=RouteNames.get(RouteID).getName();
			
			Assignments.add(this);
			Unassigned.add(this);
		}
		
		public void remove()
		{
			//removes this from its current driver
			if (driver!=null)
			{
				driver.Routes.remove(this);
				Unassigned.add(this);
			}
			driver=null;
		}
		public void assign(Driver d)
		{
			if(driver != null)
				driver.Routes.remove(this);
			else
				Unassigned.remove(this);
			
			driver=d;
			driver.Routes.add(this);
		}
		
		public int getID() {return RouteID;}
		public String getName() {return RouteName;}
		public LocalTime getStart() {return Start;}
		public void setStart(LocalTime s) {Start=s;}
		public LocalTime getEnd(){return End;}
		public void setEnd(LocalTime e) {End=e;}
		
	}//end assignment
}
