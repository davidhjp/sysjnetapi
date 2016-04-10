package com.systemj.netapi;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * <p>
 * This class is used to transmit data to the input signal that
 * implements {@code com.systemj.ipc.TCPReceiver}.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code
 * TCPSender s = new TCPSender("127.0.0.1", 2000);
 * s.emit("val", 2000); // Emitting a String "val" for 2 seconds
 * s.close(); // close when necessary
 * }
 * </pre>
 * 
 */
public class TCPSender extends OutputSignal {

	/**
	 * Create a TCPSender.
	 * 
	 * @param ip
	 *            IP Address of the {@link com.systemj.ipc.TCPReceiver}.
	 * @param port
	 *            the port number of the
	 *            {@link com.systemj.ipc.TCPReceiver}.
	 * @throws IOException
	 *             if an I/O error occurs when creating a socket.
	 */
	public TCPSender(String ip, int port) throws IOException {
		super(ip, port, (s, f) -> {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				return oos;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
	}
}
