package datamodel;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHandler {

	
	private static final String algorithm="PBKDF2WithHmacSHA256";//SHA algorithm that generates 64 bytes
	private static final int keyBitLength=512, iterations=1000;
	private SQLconnection Database;
	public PasswordHandler(SQLconnection db) {
		Database=db;
		
		
	}
	public void setPassword(int DriverID, String password) throws SQLException
	{
		byte[] passHash = getPassHash(DriverID, password);
		Database.execute("UPDATE Drivers SET PasswordHash="+passHash+" WHERE DriverID="+DriverID);
	}
	
	public int login(String driverName, String password) throws SQLException
	{
		/*
		 * this function will return the driver ID if the driver is an authorized user with correct password
		 * it will instead return -1 if given an invalid driver name or password
		 */
		ResultSet drivers=Database.getData("SELECT DriverID, DriverName, PasswordHash FROM Drivers");
		//we check driver names in the application layer as a protection against injection
		String dName;
		int dID;
		while (drivers.next())
		{
			dID=drivers.getInt("DriverID");
			dName=drivers.getString("DriverName");
			if(dName.equals(driverName));
			{
				//valid name, now check password
				byte[] passwordHash=drivers.getBytes("PasswordHash");
				if ( Arrays.equals(passwordHash, getPassHash(dID, password)) )
				{
					return dID;//valid password, return statement will break the loop
				}else {
					return -1;//wrong password, return statement will breka the loop
				}//check password
			}//check driver name
		}//loop through drivers
		return -1;//we have iterated through all driver names, there is no match
	}//end login
	
	
	private byte[] getPassHash(int DriverID, String Password) throws SQLException
	{
		byte[] salt=getSalt(DriverID);
		KeySpec spec = new PBEKeySpec(Password.toCharArray(), salt, iterations, keyBitLength);
		SecretKeyFactory f;
		byte[] passHash=null;
		try {
			f = SecretKeyFactory.getInstance(algorithm);
			passHash= f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			//library failure
			e.printStackTrace();
		}
		return passHash;
	}//get password hash
	
	private byte[] getSalt(int DriverID) throws SQLException
	{
		//generate salt if null, else save in database
		ResultSet saltSet=Database.getData("SELECT Salt FROM Drivers WHERE DriverID="+DriverID);
		
		saltSet.next();
		byte[] salt=saltSet.getBytes(1);
		if (salt==null)
		{
			//if one doesn't exist, create a salt and save it to the db
			salt = generateSalt(DriverID);
		}
		return salt;
	}//end get salt
	
	private byte[] generateSalt(int DriverID) throws SQLException
	{
		byte[] salt=new byte[8];
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

			random.nextBytes(salt);
			//save salt
			Database.execute("UPDATE Drivers SET Salt="+salt+" WHERE DriverID="+DriverID);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return salt;
	}
}//end class
