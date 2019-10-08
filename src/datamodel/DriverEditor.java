package datamodel;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DriverEditor {

	/*
	 * this is a container for editing drivers
	 * the PasswordHash and Salt columns are controlled by the PassowdHandler class
	*/
	private int DriverID;//primary key, not to be changed
	private String DriverName;
	private Date cdlExpires;
	private SQLconnection Database;
	public DriverEditor(SQLconnection db, int driverID) throws SQLException {
		Database=db;
		DriverID=driverID;
		//get data
		ResultSet data=Database.getData("SELECT DriverName, CDL_ExpirationDate FROM Drivers WHERE DriverID="+DriverID);
		data.next();
		DriverName=data.getString(1);
		cdlExpires=data.getDate(2);
	}
	
	
	public static DriverEditor createNewDriver(SQLconnection db, String DriverName) throws SQLException
	{
		ResultSet data=db.getData("SELECT MAX(DriverID) FROM Drivers");
		data.next();
		int driverID=data.getInt(1)+1;
		DriverName=DriverName.replace("'", "").replace("\\", "");
		db.execute("INSERT INTO Drivers (DriverID, DriverName)"
				+ "VALUES ("+driverID+",'"+DriverName+"')");
		return new DriverEditor(db, driverID);
	}
	public String getName() {return DriverName;}
	public boolean setName(String name)
	{
		try {
			Database.execute("UPDATE Drivers SET DriverName="+name+" WHERE DriverID="+DriverID);
			DriverName=name;
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}//end update name

	public Date getExpirationDate() {return cdlExpires;}
	public boolean setExpirationDate(int year, int month, int day)
	{
		@SuppressWarnings("deprecation")//this is the most useful constructor for this class, whoever decided to depreciate it should be beaten to death with a wet noodle
		Date update=new Date(year, month, day);
		try {
			Database.execute("UPDATE Drivers SET CDL_ExpirationDate="+update+" WHERE DriverID="+DriverID);
			cdlExpires=update;
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}//end update expiration date
	
	public void setPassword(String password) throws SQLException
	{
		PasswordHandler handle=Database.getPasswordHandler();
		handle.setPassword(DriverID, password);
	}
	
}
