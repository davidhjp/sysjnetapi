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
 * // Assuming the output signal in the LCF is specified as follows:
 * // <oSignal Name="Out" Class="com.systemj.ipc.TCPSender" IP="127.0.0.1" Port="2000"/>
 * TCPReceiver s = new TCPReceiver("127.0.0.1", 2000);
 * 
 * // Sets a Consumer that prints the status and the value of the incoming signal
 * s.setConsumer((status, value) -> System.out.println("Received : " + status + " " + value));
 * s.close(); // close when necessary
 * }
 * </pre>
 * 
 */
public class TCPReceiver extends InputSignal {
	private BiConsumer<Boolean, Object> bc;

	/**
	 * Sets a {@link java.util.function.BiConsumer BiConsumer} which
	 * will accept a status and a value of the incoming signal.
	 * 
	 * @param c
	 *            the {@link java.util.function.BiConsumer BiConsumer}
	 *            that accepts both a status and a value of the incoming
	 *            signal.
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
