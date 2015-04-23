/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimError;

/**
 * Multicopy message router 
 * Author: Akshay Kayastha, Khushveer Kaur, Dilip Yadav
 */
public class MulticopyRouter extends ActiveRouter {
	
	public static final String ALLOWABLE_COPIES = "copies";
	private int copies;
	
	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public MulticopyRouter(Settings s) {
		super(s);
		if (s.contains(ALLOWABLE_COPIES)) {
			this.copies = s.getInt(ALLOWABLE_COPIES);
//			System.out.println(this.copies);
		}
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected MulticopyRouter(MulticopyRouter r) {
		super(r);
		this.copies = r.copies;
	}
			
	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}
		
		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}
//		System.out.println(getHost().getNrofMessages());
//		System.out.println(getHost().toString());
//		for(Message m : this.getMessageCollection())
//		{
//			System.out.println(m.toString()+" == "+m.getFrom()+"-->"+m.getTo());
//		}
//		System.out.println("_______________________________________________");
		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}
	

	@Override
	public int receiveMessage(Message m, DTNHost from) {
		// TODO Auto-generated method stub
		//check if connection has already been made in this instance
		//and deny message transfer if true
		Connection con = getConnection(from);
		if(con.isDone())
			return DENIED_OLD;
		
		//deny message transfer if only one copy exists
		if(m.getCopies()<2) return DENIED_OLD;
		
		//distribute copies between hosts and set connection to be true for the instance
		Message newMessage = distribute(m, from);
		con.setDone(true);
		
		return super.receiveMessage(newMessage, from);
	}

	private Connection getConnection(DTNHost from) {
		// TODO Auto-generated method stub
		List<Connection> cons = from.getConnections();
		for(Connection con : cons) {
			if(con.getOtherNode(from)==getHost())
				return con;
		}
		return null;
	}

	@Override
	public void changedConnection(Connection con) {
		// TODO Auto-generated method stub
		//resets connection status to false after every change
		con.setDone(false);
		super.changedConnection(con);
	}

	private Message distribute(Message m, DTNHost from) {
				
		if(isCommunityCenter(from)) {
			System.out.print(from.toString()+"** ");
			return doHoming(getHost(), from, m);			
		}
		else if(isCommunityCenter(getHost())){
			System.out.print(getHost().toString()+"** ");
			return doHoming(from, getHost(), m);
		}
		else
			return doRoaming(from, m);
	}
	
	private Message doHoming(DTNHost from, DTNHost center, Message m) {
		// TODO Auto-generated method stub
		Message newMessage = m.replicate();
		System.out.print(newMessage.toString()+"[" + existingCopies(center,m) + "],[" + newMessage.getCopies()+"] ["
				+center.getCcap()+"]["+from.getCcap()+"]");
		
		//Host dumps all its copies
		from.reduceCcap(m.getCopies());
		center.addCcap(m.getCopies());
		
		//recreate the messages
		newMessage.setCopies(m.getCopies()-1);
		setCopies(center,newMessage,m.getCopies()-1);
		m.setCopies(1);
		setCopies(from,m,1);
		
		//Center gives host 1 message
		from.addCcap(1);
		center.reduceCcap(1);
		
		System.out.println(" recieved by "
				+getHost().toString()+"["+getHost().getCcap()+"] from "
				+from.toString()+"["+from.getCcap()+"] with "
				+newMessage.getCopies()+" copies, still have "+m.getCopies()+" copies");
		return newMessage;
	}

	private Message doRoaming(DTNHost from, Message m) {
		// TODO Auto-generated method stub
		Message newMessage = m.replicate();
		
		System.out.print(newMessage.toString()+"[" + existingCopies(getHost(),m) + "],[" + newMessage.getCopies()+"] ["
				+getHost().getCcap()+"]["+from.getCcap()+"]");
		
		//calculate total number of message copies present in the connection, and divide the ratio
		int total_copies= newMessage.getCopies() + existingCopies(getHost(),m);
		int oldcopies = total_copies/2;
		int newcopies = total_copies - oldcopies;
		
		//Modify ccap value for receiver and sender
		//reduce number of copies
		from.reduceCcap(m.getCopies());
		getHost().reduceCcap(existingCopies(getHost(),m));
		
		//set divided copies in messages
		newMessage.setCopies(newcopies);
		setCopies(getHost(),newMessage,newcopies);
		m.setCopies(oldcopies);
		setCopies(from,m,oldcopies);
		
		//add new ccap value to nodes
		from.addCcap(oldcopies);
		getHost().addCcap(newcopies);
		
		System.out.println(" recieved by "
				+getHost().toString()+"["+getHost().getCcap()+"] from "
				+from.toString()+"["+from.getCcap()+"] with "
				+newMessage.getCopies()+" copies, still have "+m.getCopies()+" copies");
		return newMessage;

	}

	private boolean isCommunityCenter(DTNHost host) {
		// TODO Auto-generated method stub
		if(host.toString().startsWith("CC"))
			return true;
		return false;
	}

	private int existingCopies(DTNHost host, Message m) {
		for (Message e : host.getMessageCollection()) {
			if(e.getId() == m.getId())
				return e.getCopies();
		}
		return 0;
	}
	
	private void setCopies(DTNHost host, Message m, int copies) {
		for (Message e : host.getMessageCollection()) {
			if(e.getId() == m.getId())
				e.setCopies(copies);
		}
	}
	
	@Override
	public MulticopyRouter replicate() {
		return new MulticopyRouter(this);
	}

	public int getCopies() {
		return this.copies;
	}
	
	void setCopies(int copies) {
		this.copies = copies;
	}
}