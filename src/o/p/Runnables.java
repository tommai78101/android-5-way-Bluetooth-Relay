package o.p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Runnables {
	
	public class Accept implements Runnable {
		private BluetoothAdapter adapter;
		private BluetoothSocket socket;
		private int iterator;
		
		public Accept(BluetoothAdapter adapter, int value) {
			this.adapter = adapter;
			iterator = value;
		}
		
		@Override
		public void run() {
			try {
				Log.d("DEBUG", "Listening with uuid: " + Main.uuids[iterator].toString());
				BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord(Integer.toString(iterator), Main.uuids[iterator]);
				Log.d("DEBUG", "Accepting...");
				socket = serverSocket.accept();
				Log.d("DEBUG", "Connected. Closing server socket.");
				serverSocket.close();
				
				//TODO: (Accept) Pass the socket to another thread for read/write management.
				Manage thread = new Manage(socket);
				manageThreads.add(thread);
				executor.execute(thread);
				
				/*Log.d("DEBUG", "Not doing anything. Closing connected socket.");
				socket.close();*/
			}
			catch (IOException e) {
				Log.d("DEBUG", "Exception", e);
			}
		}
		
		public void cancel() {
			if (socket != null) {
				try {
					Log.d("DEBUG", "Cancelled connection. Socket is closing.");
					socket.close();
					socket = null;
				}
				catch (IOException e) {
					Log.d("DEBUG", "Exception", e);
				}
			}
		}
	}
	
	//---------------------------------------------------------------------
	
	public class NewAccept implements Runnable {
		private BluetoothAdapter adapter;
		private BluetoothSocket socket;
		private BluetoothServerSocket serverSocket;
		
		public NewAccept(BluetoothAdapter a) {
			adapter = a;
		}
		
		@Override
		public void run() {
			for (int i = 0; i < Main.uuids.length; i++) {
				UUID uuid = Main.uuids[i];
				/*if (usedUUIDs.size() > 0) {
					if (usedUUIDs.contains(uuid))
						continue;
				}*/
				try {
					Log.d("DEBUG", "Listening with uuid: " + uuid.toString());
					serverSocket = adapter.listenUsingRfcommWithServiceRecord("test", uuid);
					socket = serverSocket.accept();
					Log.d("DEBUG", "Connected successfully.");
					serverSocket.close();
					Manage thread = new Manage(socket);
					manageThreads.add(thread);
					executor.execute(thread);
					/*usedUUIDs.add(uuid);*/
					break;
				}
				catch (IOException e) {
					Log.d("DEBUG", "Unable to connect.", e);
				}
			}
		}
		
		public void cancel() {
			if (serverSocket != null) {
				try {
					serverSocket.close();
					serverSocket = null;
				}
				catch (IOException e1) {
					Log.d("DEBUG", "Unable to close server socket.", e1);
				}
			}
			if (socket != null) {
				try {
					socket.close();
					socket = null;
				}
				catch (IOException e1) {
					Log.d("DEBUG", "Unable to close socket.", e1);
				}
			}
		}
	}
	
	//---------------------------------------------------------------------
	
	public class Connect implements Runnable {
		private BluetoothDevice device;
		private BluetoothSocket socket;
		private int iterator;
		public Object monitor = new Object();
		
		public Connect(BluetoothDevice d, int value) {
			device = d;
			iterator = value;
		}
		
		@Override
		public void run() {
			try {
				synchronized (monitor) {
					connectThreadIsRunning = true;
				}
				
				Log.d("DEBUG", "Device is creating socket with uuid: " + Main.uuids[iterator].toString());
				socket = device.createRfcommSocketToServiceRecord(Main.uuids[iterator]);
				Log.d("DEBUG", "Trying to connect...");
				socket.connect();
				Log.d("DEBUG", "Connection successful.");
				
				//TODO: (Connect) Pass the socket to another thread for read/write management.
				Manage thread = new Manage(socket);
				manageThreads.add(thread);
				executor.execute(thread);
				/*Log.d("DEBUG", "Not doing anything, closing connected socket.");
				socket.close();*/
				synchronized (monitor) {
					connectThreadIsRunning = false;
				}
			}
			catch (IOException e) {
				Log.d("DEBUG", "Exception", e);
				synchronized (monitor) {
					connectThreadIsRunning = false;
				}
			}
		}
		
		public void cancel() {
			if (socket != null) {
				try {
					Log.d("DEBUG", "Cancelled connection. Socket is closing.");
					socket.close();
					socket = null;
				}
				catch (IOException e) {
					Log.d("DEBUG", "Exception", e);
				}
			}
		}
	}
	
	//---------------------------------------------------------------------
	
	public class Manage implements Runnable {
		private InputStream inputStream;
		private OutputStream outputStream;
		private BluetoothSocket socket;
		
		public Manage(BluetoothSocket s) {
			socket = s;
			try {
				inputStream = s.getInputStream();
				outputStream = s.getOutputStream();
			}
			catch (IOException e) {
				Log.d("DEBUG", "Exception", e);
			}
		}
		
		@Override
		public void run() {
			byte[] buffer = new byte[256];
			int bytes = 0;
			while (true) {
				try {
					bytes = inputStream.read(buffer);
					handler.obtainMessage(Main.MSG_READ, bytes, 0, buffer).sendToTarget();
				}
				catch (IOException e) {
					Log.d("DEBUG", "Exception", e);
					break;
				}
			}
			cancel();
		}
		
		public void write(byte[] data) {
			if (outputStream != null) {
				try {
					outputStream.write(data);
					handler.obtainMessage(Main.MSG_WRITE, 0, 0, data).sendToTarget();
				}
				catch (IOException e) {
					Log.d("DEBUG", "Exception", e);
				}
			}
		}
		
		public void cancel() {
			try {
				socket.close();
			}
			catch (IOException e) {
				Log.d("DEBUG", "Exception", e);
			}
		}
	}
	
	//---------------------------------------------------------------------
	
	public class Multi implements Runnable {
		
		private Object monitor = new Object();
		private boolean isComplete;
		private boolean hasConnected;
		private List<BluetoothDevice> list;
		private BluetoothSocket socket;
		
		public Multi(List<BluetoothDevice> d) {
			list = d;
			hasConnected = false;
		}
		
		@Override
		public void run() {
			Thread block = new Thread(new Runnable() {
				@Override
				public void run() {
					for (BluetoothDevice device : list) {
						if (usedDevices.size() > 0) {
							if (usedDevices.contains(device))
								continue;
						}
						for (int i = 0; i < Main.uuids.length; i++) {
							/*if (usedUUIDs.size() > 0) {
								if (usedUUIDs.contains(Main.uuids[i]))
									continue;
							}*/
							try {
								Log.d("DEBUG", "Trying to connect device " + device.getName() + " with uuid: " + Main.uuids[i]);
								socket = device.createRfcommSocketToServiceRecord(Main.uuids[i]);
								socket.connect();
								Log.d("DEBUG", "Connected successfully.");
								hasConnected = true;
								Manage thread = new Manage(socket);
								manageThreads.add(thread);
								executor.execute(thread);
								usedDevices.add(device);
								/*usedUUIDs.add(Main.uuids[i]);*/
								break;
							}
							catch (IOException e) {
								Log.d("DEBUG", "Exception: Unsuccessful attempt at connecting with uuid: " + Main.uuids[i], e);
							}
						}
					}
					
					if (!hasConnected)
						Log.d("DEBUG", "No connections made. Unsuccessful.");
					
					synchronized (monitor) {
						isComplete = true;
						monitor.notifyAll();
					}
				}
			});
			block.start();
			
			synchronized (monitor) {
				isComplete = false;
				while (!isComplete) {
					try {
						monitor.wait();
						if (isComplete)
							break;
					}
					catch (InterruptedException e) {
						Log.d("DEBUG", "Interrupted within Multi, ignoring...", e);
					}
				}
			}
		}
		
		public boolean hasConnected() {
			return hasConnected;
		}
		
		public void cancel() {
			if (socket != null) {
				try {
					socket.close();
					socket = null;
				}
				catch (IOException e) {
					Log.d("DEBUG", "Unable to close socket.", e);
				}
			}
		}
	}
	
	//---------------------------------------------------------------------
	
	private Handler handler;
	private Main mainActivity;
	private ThreadPoolExecutor executor;
	private ArrayList<Manage> manageThreads = new ArrayList<Manage>();
	private NewAccept acceptThread;
	private Multi connectThread;
	private boolean connectThreadIsRunning;
	
	private ArrayList<BluetoothDevice> usedDevices = new ArrayList<BluetoothDevice>();
	
	/*private ArrayList<UUID> usedUUIDs = new ArrayList<UUID>();*/
	
	public Runnables(Main m, Handler h) {
		handler = h;
		mainActivity = m;
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		
		Button button = (Button) m.findViewById(R.id.b_send);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean test = false;
				for (Manage manageThread : manageThreads) {
					if (manageThread != null) {
						String message = "It worked.";
						manageThread.write(message.getBytes());
						test = true;
					}
				}
				if (!test) {
					Log.d("DEBUG", "Not connected yet.");
					Toast.makeText(mainActivity, "Not connected yet.", Toast.LENGTH_LONG).show();
				}
			}
		});
		button = (Button) m.findViewById(R.id.b_cancel);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("DEBUG", "Cancelling all accept/connect threads.");
				for (Manage manageThread : manageThreads) {
					if (manageThread != null) {
						manageThread.cancel();
						manageThread = null;
					}
				}
				manageThreads.clear();
				if (acceptThread != null) {
					acceptThread.cancel();
					acceptThread = null;
				}
				if (connectThread != null) {
					connectThread.cancel();
					connectThread = null;
				}
			}
		});
		
	}
	
	public void executeAccept(BluetoothAdapter a, int value) {
		acceptThread = new NewAccept(a);
		executor.execute(acceptThread);
		//executor.execute(new Accept(a, value));
	}
	
	public void executeConnect(List<BluetoothDevice> d, int value) {
		//executor.execute(new Connect(d, value));
		connectThread = new Multi(d);
		executor.execute(connectThread);
	}
	
	public synchronized boolean IsConnectThreadRunning() {
		return connectThreadIsRunning;
	}
	
	public synchronized void setConnectThreadFlag() {
		connectThreadIsRunning = true;
	}
	
	public void cancel() {
		usedDevices.clear();
		/*usedUUIDs.clear();*/
	}
}
