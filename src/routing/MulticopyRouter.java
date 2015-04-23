/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimError;

/**
 * Epidemic message router with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class MulticopyRouter extends ActiveRouter {
	
	/* The total number of message copies allowed to transmit throughout 
	 * the network
	 * Author: Akshay Kayastha, Khushveer Kaur, Dilip Yadav */
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
		return super.receiveMessage(m, from);
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