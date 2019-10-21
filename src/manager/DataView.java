package manager;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import datamodel.SQLconnection;

public abstract class DataView extends JPanel {

	protected SQLconnection DataBase;
	
	public DataView(SQLconnection db) {
		super();
		DataBase=db;
	}

	public abstract void Save();//this function will save the current state of the current state of the data view to the SQL server
	
	public abstract void Refresh();//this will load the current state of the database into the view, discarding any changes since the last save
}
