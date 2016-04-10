package com.systemj.netapi;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * <p>
 * This class is used to transmit data to the input signal that
 * implements {@link com.systemj.ipc.SimpleServer}
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * <pre>
 * {@code
 * SimpleClient s = new SimpleClient("127.0.0.1", 2000, "CD1", "I");
 * s.emit("val", 2000); // Emitting a String "val" for 2 seconds
 * s.close(); // close when necessary
 * }
 * </pre>
 *
 */
public class SimpleClient extends OutputSignal {
	private String cdName;
	private String sigName;

	/**
	 * 
	 * @param ip
	 *            the hostname specified for the
	 *            {@link com.systemj.ipc.SimpleServer}.
	 * @param port
	 *            the port number for the
	 *            {@link com.systemj.ipc.SimpleServer}.
	 * @param cdName
	 *            the name of the receiving clock-domain.
	 * @param sigName
	 *            the name of the input signal.
	 * @throws IOException
	 *             if an I/O error occurs when creating a socket.
	 */
	public SimpleClient(String ip, int port, String cdName, String sigName) throws IOException {
		super(ip, port, (s, f) -> {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(cdName + "." + sigName);
				int ack = s.getInputStream().read();
				if(ack >= 0)
					return oos;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		this.cdName = cdName;
		this.sigName = sigName;
	}
	
}
