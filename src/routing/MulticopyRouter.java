/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.Message;
import core.Settings;

/**
 * Epidemic message router with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class MulticopyRouter extends ActiveRouter {
	
	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public MulticopyRouter(Settings s) {
		super(s);
		//TODO: read&use epidemic router specific settings (if any)
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected MulticopyRouter(MulticopyRouter r) {
		super(r);
		//TODO: copy epidemic settings here (if any)
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
		System.out.println(getHost().toString());
		for(Message m : this.getMessageCollection())
		{
			System.out.println(m.toString()+" == "+m.getFrom()+"-->"+m.getTo());
		}
		System.out.println("_______________________________________________");
		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}
	
	
	/* (non-Javadoc)
	 * @see routing.ActiveRouter#tryAllMessagesToAllConnections()
	 */
	@Override
	protected Connection tryAllMessagesToAllConnections() {
		// TODO Auto-generated method stub
		return super.tryAllMessagesToAllConnections();
	}

	@Override
	public MulticopyRouter replicate() {
		return new MulticopyRouter(this);
	}

}