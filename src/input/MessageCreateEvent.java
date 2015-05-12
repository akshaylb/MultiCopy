/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import java.util.List;

import routing.MulticopyRouter;
import core.DTNHost;
import core.Message;
import core.World;

/**
 * External event for creating a message.
 */
public class MessageCreateEvent extends MessageEvent {
	private int size;
	private int responseSize;
	
	/**
	 * Creates a message creation event with a optional response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message
	 * @param responseSize Size of the requested response message or 0 if
	 * no response is requested
	 * @param time Time, when the message is created
	 */
	public MessageCreateEvent(int from, int to, String id, int size,
			int responseSize, double time) {
		super(from,to, id, time);
		this.size = size;
		this.responseSize = responseSize;
	}

	public MessageCreateEvent(int from, List<Integer> mto, String id, int size,
			int responseSize, double time) {
		super(from, mto, id, time);
		this.size = size;
		this.responseSize = responseSize;
	}
	/**
	 * Creates the message this event represents. 
	 */
	@Override
	public void processEvent(World world) {
		Message m;
		DTNHost to = null,from;
		List<DTNHost> mto = null;
		if (this.mtoAddr == null) {
			to = world.getNodeByAddress(this.toAddr);
			from = world.getNodeByAddress(this.fromAddr);			
			m = new Message(from, to, this.id, this.size);
		}
		else {
			mto = world.getNodeByAddress(this.mtoAddr);
			from = world.getNodeByAddress(this.fromAddr);			
			m = new Message(from, mto, this.id, this.size);
		}
		m.setResponseSize(this.responseSize);
		int copies;
		if(from.getRouter() instanceof MulticopyRouter) {
			copies = ((MulticopyRouter)from.getRouter()).getCopies();
			m.setCopies(copies);
			from.setCcap(copies);
			System.out.println(from+" creates "+m+" for "+m.getTo()+" with "+copies+" copies");
		}
		from.createNewMessage(m);
	}
	
	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "->" + toAddr + "] " +
		"size:" + size + " CREATE";
	}
}
