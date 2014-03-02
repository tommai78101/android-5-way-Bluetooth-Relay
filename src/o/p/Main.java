package o.p;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * The purpose of this app is to create a server-only Bluetooth connection that
 * only accepts incoming Bluetooth connections. It is used only for testing
 * one device that needs Bluetooth tweaking in order to consistently work
 * correctly on the other device.
 * */

public class Main extends Activity implements OnClickListener {
	public static final UUID[] uuids = {UUID.fromString("f783e78a-4390-40a4-bc6a-2d2fa9160c66"),
			UUID.fromString("63886203-a801-44da-9063-0eef6c0b0fb8"), UUID.fromString("3c970000-5ee6-4246-b3ee-f2404e7aaced"),
			UUID.fromString("9b3a2f36-d0ee-4d04-8598-3cad098b5fcd"), UUID.fromString("4908c9a4-4a5b-49f5-b6d9-3cdcaa99e4bc")};
	
	private BluetoothAdapter bluetoothAdapter;
	private DeviceReceiver receiver;
	public LogService log;
	private Runnables run;
	private MyHandler handler;
	
	//---------------------------------------------------------------------
	
	//Constants
	public static final int REQUEST_ENABLE_BLUETOOTH = 1;
	
	@Override
	public void onCreate(Bundle b) {
		Log.d("DEBUG", "Initializing...");
		super.onCreate(b);
		this.setContentView(R.layout.main);
		
		Button button = (Button) this.findViewById(R.id.b_server);
		button.setOnClickListener(this);
		button = (Button) this.findViewById(R.id.b_client);
		button.setOnClickListener(this);
		button = (Button) this.findViewById(R.id.b_scan);
		button.setOnClickListener(this);
		button = (Button) this.findViewById(R.id.b_clear);
		button.setOnClickListener(this);
		
		Log.d("DEBUG", "Registering BroadcastReceiver.");
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		receiver = new DeviceReceiver();
		this.registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		this.registerReceiver(receiver, filter);
		
		log = new LogService(this);
		handler = new MyHandler(this);
		run = new Runnables(this, this.handler);
		checkSupport();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("DEBUG", "Unregistering BroadcastReceiver.");
		this.unregisterReceiver(receiver);
	}
	
	@Override
	public void onClick(View view) {
		Log.d("DEBUG", "Cancelling scanning, regardless.");
		bluetoothAdapter.cancelDiscovery();
		switch (view.getId()) {
			case R.id.b_server:
				if (receiver.isComplete()) {
					/*for (int i = 0; i < receiver.getDeviceCount(); i++) {*/
					Log.d("DEBUG", "Executing Accept thread.");
					run.executeAccept(bluetoothAdapter, 0);
					/*}*/
				}
				break;
			case R.id.b_client:
				if (receiver.isComplete()) {
					List<BluetoothDevice> list = receiver.devices;
					Log.d("DEBUG", "Connect list size: " + list.size());
					/*for (int i = 0; i < list.size(); i++) {*/
					Log.d("DEBUG", "Setting the flag to 'true'.");
					run.setConnectThreadFlag();
					Log.d("DEBUG", "Executing Connect thread.");
					run.executeConnect(list, 0);
					/*}*/
				}
				break;
			case R.id.b_scan:
				checkEnabled();
				discover();
				scan();
				break;
			case R.id.b_clear:
				log.clear();
				run.cancel();
				break;
		}
	}
	
	//---------------------------------------------------------------------
	
	private void discover() {
		//Check to see if it's discoverable.
		if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Log.d("DEBUG", "Enabling discoverable mode.");
			Intent scan = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			scan.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
			this.startActivity(scan);
			Toast.makeText(this, "Being discovered.", Toast.LENGTH_LONG).show();
		}
		else {
			Log.d("DEBUG", "Still being discovered.");
			Toast.makeText(this, "Still being discovered.", Toast.LENGTH_LONG).show();
		}
	}
	
	private void scan() {
		//Check to see if it's scanning.
		if (bluetoothAdapter.isDiscovering()) {
			Log.d("DEBUG", "Re-scanning.");
			Toast.makeText(this, "Re-scanning", Toast.LENGTH_LONG).show();
			bluetoothAdapter.cancelDiscovery();
		}
		else {
			Log.d("DEBUG", "Scanning.");
			Toast.makeText(this, "Start scanning.", Toast.LENGTH_LONG).show();
		}
		bluetoothAdapter.startDiscovery();
	}
	
	private void checkSupport() {
		//Check if Bluetooth is supported.
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth isn't supported.", Toast.LENGTH_LONG).show();
			Log.d("DEBUG", "Bluetooth isn't supported.");
			this.finish();
		}
	}
	
	private void checkEnabled() {
		//Check if Bluetooth is enabled.
		if (!bluetoothAdapter.isEnabled()) {
			Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			this.startActivityForResult(enable, REQUEST_ENABLE_BLUETOOTH);
			Toast.makeText(this, "Bluetooth is enabled.", Toast.LENGTH_LONG).show();
			Log.d("DEBUG", "Bluetooth is enabled.");
		}
	}
	
	//---------------------------------------------------------------------
	
	public static final int MSG_READ = 0x1000;
	public static final int MSG_WRITE = 0x1001;
	
	private static class MyHandler extends Handler {
		private final WeakReference<Main> main;
		
		public MyHandler(Main m) {
			main = new WeakReference<Main>(m);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Main mainActivity = main.get();
			switch (msg.what) {
				case MSG_READ:
					byte[] readBuffer = (byte[]) msg.obj;
					mainActivity.log.read(readBuffer, msg.arg1);
					break;
				case MSG_WRITE:
					byte[] writeBuffer = (byte[]) msg.obj;
					mainActivity.log.write(writeBuffer);
					break;
			}
		}
	};
}
