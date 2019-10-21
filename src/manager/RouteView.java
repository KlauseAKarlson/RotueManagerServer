package manager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import datamodel.RouteEditor;
import datamodel.RouteEditor.Stop;
import datamodel.SQLconnection;

public class RouteView extends DataView {
	
	private int RouteID;
	private RouteEditor Route;
	private Stop heldStop;//for drag and drop
	private Box RouteBox, StopBox;
	
	public RouteView(SQLconnection db, int routeID) throws SQLException {
		super(db);
		RouteID=routeID;
		//create gui
		this.setLayout(new BorderLayout());
		
		//create side panel to show all available stops
		Box sidepanel=Box.createVerticalBox();
		sidepanel.add(new JLabel("All Stops"));
		JPanel StopDisplay =new JPanel();//will contain all 
		StopBox =Box.createVerticalBox();
		StopDisplay.add(RouteBox);
		JScrollPane stopScroller=new JScrollPane(StopDisplay);
		stopScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		stopScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sidepanel.add(stopScroller);
		this.add(sidepanel, BorderLayout.EAST);
		//create main panel to show the current route
		JPanel RouteDisplay=new JPanel();
		RouteBox=Box.createHorizontalBox();
		RouteDisplay.add(RouteBox);
		JScrollPane routeScroller=new JScrollPane(RouteDisplay);
		routeScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		routeScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(routeScroller, BorderLayout.CENTER);
		//load data
		Refresh();
		//TODO create action listeners
	}

	@Override
	public void Save() {
		try {
			Route.save();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}//end save

	public void Swap(int stop1, int stop2)
	{
		
	}
	public void Insert (Stop stop, int index)
	{
		
	}
	
	public void DeleteStop (int index)
	{
		
	}
	
	@Override
	public void Refresh() {
		//clear containers
		RouteBox.removeAll();
		StopBox.removeAll();
		//try to load a route editor
		try {
			Route=DataBase.editRoute(RouteID);
			//load stops into side panel
			Iterator<Stop> stops=Route.getStops();
			while (stops.hasNext())
			{
				//TODO
			}
			//load stops into route editor
			Iterator<Stop> routestops=Route.getRoute();
			while (routestops.hasNext())
			{
				//TODO
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//provide fillers from display
			RouteBox.add(new JLabel("ERROR: No Data"));
			StopBox.add(new JLabel("ERROR: No Data"));
		}
	}//end refresh

	//private classes to represent stops in side panel and on route for users
	private class StopHolder extends JPanel
	{
		private Stop S;
		private StopHolder(Stop s)
		{
			super(new GridLayout(0,1));
			S=s;
			this.add(new JLabel(S.getName()));
			this.add(new JLabel(S.getAddress()));
		}
		public Stop getStop()
		{
			return S;
		}
	}
	
	private class RouteStop extends JPanel
	{
		private RouteStop(Stop S)
		{
			///TODO create gui elements
		}
	}
}//end class
