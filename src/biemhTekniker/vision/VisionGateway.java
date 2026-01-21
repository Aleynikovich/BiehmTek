package biemhTekniker.vision;

import java.util.concurrent.ConcurrentLinkedQueue;

import biemhTekniker.logger.Logger;

public class VisionGateway {
	private static final Logger log = Logger.getLogger(VisionGateway.class);
    private static final ConcurrentLinkedQueue<String> outbox = new ConcurrentLinkedQueue<String>();
    private static final ConcurrentLinkedQueue<String> inbox = new ConcurrentLinkedQueue<String>();

    // Outgoing
    public static void send(String msg)
    {
    	log.info("Sending " + msg);
    	outbox.add(msg); 
    }
    public static String pollOutbox() { return outbox.poll(); }

    // Incoming
    public static void depositResponse(String msg) { inbox.add(msg); }
    public static String getResponse() { return inbox.poll(); }
}