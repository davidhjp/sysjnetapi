package com.systemj.netapi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.systemj.util.Tuple;

/**
 * This class is used to receive data from the output signal that
 * implements {@code com.systemj.ipc.SimpleClient}.
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code
 * // Assuming the output signal in the LCF is specified as follows:
 * // <oSignal Name="Out" To="CD1.I" Class="com.systemj.ipc.SimpleClient" IP="127.0.0.1" Port="2000"/>
 * SimpleServer s = new SimpleServer("127.0.0.1", 2000);
 * 
 * // Adding a Consumer for the input signal "I" of the clock-domain "CD1", which are specified by the "To" attribute.
 * s.addConsumer("CD1", "I", (status, value) -> System.out.println("Received : " + status + " " + value));
 * s.close(); // close when necessary
 * }
 * </pre>
 *
 */
public class SimpleServer extends InputSignal {
	private Map<String, BiConsumer<Boolean, Object>> map = new HashMap<>();

	/**
	 * Adds a {@link java.util.function.BiConsumer BiConsumer} for the
	 * input signal {@code sigName} of the clock-domain {@code cdName}.
	 * The {@link java.util.function.BiConsumer BiConsumer} will accept
	 * an updated status and a value of the signal which are specified
	 * by the {@code To} attribute of the
	 * {@link com.systemj.ipc.SimpleClient}.
	 * 
	 * @param cdName
	 *            the receiving clock-domain name.
	 * @param sigName
	 *            the input signal name.
	 * @param c
	 *            the {@link java.util.function.BiConsumer BiConsumer}
	 *            that accepts both a status and a value of the signal.
	 */
	public synchronized final void addConsumer(String cdName, String sigName, BiConsumer<Boolean, Object> c) {
		map.put(cdName + "." + sigName, c);
	}

	private synchronized final BiConsumer<Boolean, Object> getSignalHandler(String cdsigname) {
		return map.get(cdsigname);
	}

	/**
	 * Create a SimpleServer.
	 * 
	 * @param ip
	 *            the local hostname that this server will bind to and
	 *            accept an incoming signal transmitted from the
	 *            {@link com.systemj.ipc.SimpleClient}.
	 * @param port
	 *            the port number of the server.
	 */
	public SimpleServer(String ip, int port) {
		super(ip, port, (s, f) -> {
			try {
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				String sn = (String) ois.readObject();
				BiConsumer<Boolean, Object> bc = ((SimpleServer) f).getSignalHandler(sn);
				if (bc != null) {
					s.getOutputStream().write(0);
					return new Tuple<ObjectInputStream, BiConsumer<Boolean, Object>>(ois, bc);
				} else
					return null;
			} catch (IOException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
	}
}
