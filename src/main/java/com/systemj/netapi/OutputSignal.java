package com.systemj.netapi;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * A class that implements a signal client, which connects to the server
 * created by {@link com.systemj.ipc.GenericSignalReceiver}.
 *
 */
public abstract class OutputSignal implements Closeable {
	private String ip;
	private int port;
	private Socket s;
	private ObjectOutputStream oos;
	private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> sf;

	protected OutputSignal(String ip, int port, BiFunction<Socket, OutputSignal, ObjectOutputStream> initializer) throws IOException {
		this.ip = ip;
		this.port = port;
		s = new Socket(ip, port);
		oos = initializer.apply(s, this);
		if (oos == null)
			throw new IOException("Could not bind to " + ip + ":" + port);
	}
	
	
	private final synchronized void cancelScheduled() {
		if (sf != null)
			sf.cancel(true);
	}

	/**
	 * Emits the signal with a value {@code val} for {@code ms}
	 * millisecond(s).
	 * 
	 * @param val
	 *            the value of the signal to be emitted.
	 * @param ms
	 *            the duration that the signal will remain <b>true</b>.
	 * @throws IOException
	 *             Any exception thrown during the socket communication.
	 */
	public final synchronized void emit(Object val, int ms) throws IOException {
		cancelScheduled();
		oos.writeObject(new Object[] { true, val });
		sf = ses.schedule(() -> {
			synchronized (this) {
				try {
					if (!Thread.interrupted())
						oos.writeObject(new Object[] { false });
				} catch (Exception e) {
				}
			}
		}, ms, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sustains the signal indefinitely.
	 * 
	 * @throws IOException
	 *             Any exception thrown during the socket communication.
	 */
	public final void sustain() throws IOException {
		cancelScheduled();
		oos.writeObject(new Object[] { true });
	}

	/**
	 * Sustains the signal indefinitely with a value {@code val}.
	 * 
	 * @param val
	 *            the value of the signal to be emitted.
	 * @throws IOException
	 *             Any exception thrown during the socket communication.
	 */
	public final void sustain(Object val) throws IOException {
		cancelScheduled();
		oos.writeObject(new Object[] { true, val });
	}

	/**
	 * Sets the status of the signal to <b>false</b>.
	 * 
	 * @throws IOException
	 *             Any exception thrown during the socket communication.
	 */
	public final void setFalse() throws IOException {
		cancelScheduled();
		oos.writeObject(new Object[] { false });
	}

	/**
	 * Cancels any scheduled threads and closes the socket opened by
	 * this instance.
	 */
	@Override
	public void close() throws IOException {
		ses.shutdownNow();
		try {
			ses.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.close();
	}

}
