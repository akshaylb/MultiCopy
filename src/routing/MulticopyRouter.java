/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

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
		if(m.getCopies()<2) return DENIED_OLD;
		Message newMessage = distribute(m, from);
		return super.receiveMessage(newMessage, from);
	}

	private Message distribute(Message m, DTNHost from) {
		Message newMessage = m.replicate();
		System.out.println(newMessage.toString()+"[" + newMessage.getCopies() + "],[" + existingCopies(getHost(),m)+"]");
		int total_copies= m.getCopies() + existingCopies(getHost(),m);
		int newcopies = total_copies/2;
		int oldcopies = total_copies - newcopies;
		newMessage.setCopies(newcopies);
		m.setCopies(oldcopies);
		System.out.println(newMessage.toString()+" recieved by "
				+getHost().toString()+" from "+from.toString()+" with "
				+newMessage.getCopies()+" copies, still have "+m.getCopies()+" copies");
		return newMessage;
	}
	
	private int existingCopies(DTNHost host, Message m) {
		for (Message e : host.getMessageCollection()) {
			if(e.getId() == m.getId())
				return e.getCopies();
		}
		return 0;
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