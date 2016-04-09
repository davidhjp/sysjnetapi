package com.systemj.netapi;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.systemj.util.Tuple;

public abstract class InputSignal implements Runnable, Closeable {
	private String ip;
	private int port;
	private ExecutorService es = Executors.newCachedThreadPool();
	private final static int SO_TIMEOUT = 500;
	private boolean interrupted = false;
	private BiFunction<Socket, InputSignal, Tuple<ObjectInputStream, BiConsumer<Boolean, Object>>> initializer;
	private final Thread serverThread;

	protected InputSignal(String ip, int port, BiFunction<Socket, InputSignal, Tuple<ObjectInputStream, BiConsumer<Boolean, Object>>> initializer) {
		this.ip = ip;
		this.port = port;
		this.initializer = initializer;
		serverThread = new Thread(this);
		serverThread.start();
	}

	private class Worker implements Runnable {
		private Socket s;
		private ObjectInputStream ois;
		private BiConsumer<Boolean, Object> c;

		public Worker(Socket s, Tuple<ObjectInputStream, BiConsumer<Boolean, Object>> tu) throws IOException {
			this.s = s;
			this.ois = tu.getFirst();
			this.c = tu.getSecond();
		}

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					try {
						Object[] o = (Object[]) ois.readObject();
						c.accept((Boolean) o[0], o.length > 1 ? o[1] : null);
					} catch (SocketTimeoutException so) {
					}
				}
			} catch (IOException ee) {
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() {
		Socket s = null;
		try (ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(ip))) {
			server.setSoTimeout(SO_TIMEOUT);
			while (!interrupted) {
				try {
					s = server.accept();
					s.setSoTimeout(SO_TIMEOUT);
					Tuple<ObjectInputStream, BiConsumer<Boolean, Object>> tu = initializer.apply(s,  this);
					if (tu != null) {
						Worker w = new Worker(s, tu);
						es.submit(w);
					} else {
						s.close();
					}
				} catch (SocketTimeoutException ee) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(s != null){
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Tests if this signal server is still alive.
	 * 
	 * @return <b>true</b> if the server is alive <b>false</b>
	 *         otherwise.
	 */
	public final boolean isServerAlive() {
		return serverThread.isAlive();
	}

	/**
	 * Tries to shutdown this signal server gracefully.
	 */
	@Override
	public void close() throws IOException {
		interrupted = true;
		es.shutdownNow();
		while (isServerAlive())
			;
		try {
			es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
