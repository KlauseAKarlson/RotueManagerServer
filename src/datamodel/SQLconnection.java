package datamodel;
/*
 * this package should contain all classes that use SQL statements, 
 * all other classes should use methods of these classes to manipulate the database
 */

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.microsoft.sqlserver.jdbc.*;//SQL server driver, requires JDK 1.8

import datamodel.ActiveRoute.BusStop;

public class SQLconnection {

	/*
	 * this class serves to encapsulate an sql connection
	 * 
	 * for multi line/method call transactions
	 * use beginTransaction(),commitTrnsaction(),rollbackTrnsaction(), and afterTransaction()
	 * afterTransaction() should always be called after a multi line transaction as it re-enables autocommit
	 */
	private Connection SQLServer=null;
	
	public static void main(String[] args) 
	{
		
		try {
			SQLconnection con=new SQLconnection();
			ActiveRoute r=con.getActiveRoute(1);
			ActiveRoute r2=new ActiveRoute(r.getCSV());
			Iterator<BusStop> iter=r2.getIterator();
			while (iter.hasNext())
			{
				System.out.println(iter.next().getAddress());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	private static final String URL="jdbc:sqlserver://localhost;"
			+ "portNumber=53356;"
			+ "database=RouteManager;"
			+ "instance=SQLEXPRESS;";
	
	public SQLconnection() throws Exception {
		File serverInfo=new File("serverInfo.txt");
		BufferedReader login=new BufferedReader(new FileReader(serverInfo));
		String uname=login.readLine();
		String pswd=login.readLine();
		login.close();
		
		SQLServer=DriverManager.getConnection(URL, uname,pswd);
	}
	/*
	 * 	transaction template for copy and paste
	 	this.beginTransaction();
		try {
			//Do transactions
			this.comitTransaction();
		}catch(SQLException e) {
			this.rollbackTransaction();
		}finally {
			this.afterTransaction();
		}
	 */
	
	public ActiveRoute getActiveRoute(int RouteID) throws SQLException
	{
		return new ActiveRoute(this, RouteID);
	}
	public RouteEditor editRoute(int RouteID)throws SQLException
	{
		return new RouteEditor(this, RouteID);
	}
	public RouteEditor newRoute(String routeName) throws SQLException
	{
		/*
		 * create a new route and return an editor
		 * returns a null route if the the transaction fails
		 */
	 	this.beginTransaction();
	 	RouteEditor nRoute=null;
		try {
			//create new RouteID
			ResultSet max=getData("SELECT Max(RouteID) FROM RouteNames");
			max.next();
			int RouteID=max.getInt(1);
			//create route
			execute("INSERT INTO RouteNames (RouteID, RouteName)"
					+ "VALUES ("+RouteID+",'"+routeName+"')");
			
			nRoute=new RouteEditor(this, RouteID);
			this.comitTransaction();
		}catch(SQLException e) {
			this.rollbackTransaction();
			e.printStackTrace();
		}finally {
			this.afterTransaction();
		}
		return nRoute;
	}
	
	public void deleteRoute(int routeID) throws SQLException
	{
		this.beginTransaction();
		try {
			//first remove any references in busses
			this.execute("UPDATE RouteAssignment SET RouteID=NULL WHERE RouteID="+routeID);
			//delete route
			this.execute("DELETE FROM Routes WHERE RouteID="+routeID);
			this.execute("DELETE FROM RouteNames WHERE RouteID="+routeID);
			
			this.comitTransaction();
		}catch(SQLException e) {
			this.rollbackTransaction();
		}finally {
			this.afterTransaction();
		}
	}
	public void deleteStop(int stopID) throws SQLException
	{
		this.beginTransaction();
		try {
			//first remove any references in Routes
			this.execute("DELETE FROM Routes WHERE StopID="+stopID+";");
			//delete stop
			this.execute("DELETE FROM Stops WHERE StopID="+stopID+";");
			this.comitTransaction();
		}catch(SQLException e) {
			this.rollbackTransaction();
		}finally {
			this.afterTransaction();
		}
	}

	public ResultSet getData(String query) throws SQLException
	{

		return SQLServer.createStatement().executeQuery(query);

	}
	public void execute(String sql) throws SQLException
	{
		SQLServer.createStatement().execute(sql);
	}
	
	public void beginTransaction() throws SQLException
	{
		SQLServer.setAutoCommit(false);
	}
	public void comitTransaction() throws SQLException
	{
		SQLServer.commit();
	}
	public void rollbackTransaction() throws SQLException
	{
		SQLServer.rollback();
	}
	public void afterTransaction() throws SQLException
	{
		SQLServer.setAutoCommit(true);
	}

}
