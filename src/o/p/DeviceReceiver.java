package o.p;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DeviceReceiver extends BroadcastReceiver {
	public List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private boolean complete;
	private int size = 0;
	
	@Override
	public void onReceive(Context context, Intent data) {
		String action = data.getAction();
		if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
			Toast.makeText(context, "Scanning finished.", Toast.LENGTH_LONG).show();
			Log.d("DEBUG", "Scanning has finished.");
			size = devices.size();
			complete = true;
			//TODO: Try to make layout buttons visible once scanning has completed.
		}
		else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
			BluetoothDevice device = (BluetoothDevice) data.getExtras().get(BluetoothDevice.EXTRA_DEVICE);
			Toast.makeText(context, device.getName() + " found.", Toast.LENGTH_SHORT).show();
			Log.d("DEBUG", "Device found: " + device.getName());
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				try {
					Method m = device.getClass().getMethod("removeBond", (Class[]) null);
					m.invoke(device, (Object[]) null);
					m = device.getClass().getMethod("createBond", (Class[]) null);
					m.invoke(device, (Object[]) null);
				}
				catch (Exception e) {
					Log.e("DEBUG", "Exception.", e);
				}
			}
			devices.add(device);
		}
		else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
			complete = false;
			size = 0;
			devices.clear();
			Log.d("DEBUG", "Scanning has started.");
		}
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public int getDeviceCount() {
		return size;
	}
}
