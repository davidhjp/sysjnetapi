package com.systemj.netapi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.function.BiConsumer;

import com.systemj.util.Tuple;

/**
 * This class is used to receive data from the output signal that
 * implements {@code com.systemj.ipc.TCPSender}.
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code
 * TCPReceiver s = new TCPReceiver("127.0.0.1", 2000);
 * 
 * // Sets a Consumer that prints the status and value of the signal O emitted from the clock-domain CD1
 * s.setConsumer("CD1", "O", (status, value) -> System.out.println("Received : "+status+" "+value)); 
 * s.close(); // close when necessary
 * }
 * </pre>
 * 
 */
public class TCPReceiver extends InputSignal {
	private BiConsumer<Boolean, Object> bc;

	/**
	 * Sets a {@link java.util.function.BiConsumer BiConsumer} which
	 * will be applied whenever the status of the output signal for
	 * {@code sigName} is updated by the clock-domain {@code cdName}.
	 * 
	 * @param c
	 *            the {@link java.util.function.BiConsumer BiConsumer}
	 *            that accepts both a status and a value of this signal.
	 */
	public synchronized final void setConsumer(BiConsumer<Boolean, Object> c) {
		bc = c;
	}
	
	private synchronized BiConsumer<Boolean, Object> getListener() {
		return bc;
	}

	/**
	 * Create a TCPReceiver.
	 * 
	 * @param ip
	 *            IP address of the server created by this
	 *            {@link #TCPReceiver}.
	 * @param port
	 *            the port number of the server.
	 */
	public TCPReceiver(String ip, int port) {
		super(ip, port, (s, f) -> {
			try {
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				BiConsumer<Boolean, Object> bc = ((TCPReceiver) f).getListener();
				return new Tuple<ObjectInputStream, BiConsumer<Boolean, Object>>(ois, bc);
			} catch (IOException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
	}
}
