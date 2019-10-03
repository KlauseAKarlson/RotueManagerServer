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
	 * maintian database consistency wihtotu throwing exceptions
	 */
	private Hashtable<Integer,Route> RouteNames;
	private Hashtable<Integer,Driver> Drivers;
	public LinkedList<Assignment> Assignments;
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
				a = new Assignment(RouteID);
				//load and localize start/end times
				Time start=DriverAssignments.getTime("StartTime");
				a.Start = start==null?null:start.toLocalTime();
				Time end =DriverAssignments.getTime("EndTime");
				a.End=end==null?null:end.toLocalTime();
			}
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
				d.add(a);
			}
		}
	}//end ctor

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
		
		public boolean CDLValid()
		{
			//return's false if the CTL is expired
			return CDL_Exp.isAfter(Instant.now());
		}
		public String getName() {return DriverName;}
		public int getID() {return DriverID;}
		public Instant getCDLExpirationDate() {return CDL_Exp;}
		public LinkedList<Assignment> getRoutes(){return Routes;}
		public void add(Assignment a)
		{
			if (a==null) return;//protect from null exceptions
			a.remove();
			a.driver=this;
			Routes.add(a);
		}
	}///end driver
	public class Assignment{
		private int RouteID;
		private String RouteName;
		private Driver driver;
		private LocalTime Start,End;

		public Assignment(int routeID)
		{
			RouteID=routeID;
			RouteName=RouteNames.get(RouteID).getName();
		}
		
		public void remove()
		{
			//removes this from its current driver
			if (driver!=null)
			{
				driver.Routes.remove(this);
			}
			driver=null;
		}
		
		public int getID() {return RouteID;}
		public String getName() {return RouteName;}
		public LocalTime getStart() {return Start;}
		public void setStart(LocalTime s) {Start=s;}
		public LocalTime getEnd(){return End;}
		public void setEnd(LocalTime e) {End=e;}
		
	}//end assignment
}
