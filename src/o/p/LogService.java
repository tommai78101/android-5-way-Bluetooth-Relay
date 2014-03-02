package o.p;

import android.app.Activity;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogService {
	private ListView logView;
	private ArrayAdapter<String> logAdapter;
	
	public LogService(Activity a) {
		logAdapter = new ArrayAdapter<String>(a, R.layout.message);
		logView = (ListView) a.findViewById(R.id.log);
		logView.setStackFromBottom(true);
		logView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		logView.setAdapter(logAdapter);
	}
	
	public void write(byte[] data) {
		String message = new String(data);
		logAdapter.add("Write: " + message);
		return;
	}
	
	public void read(byte[] data, int length) {
		String message = new String(data, 0, length);
		logAdapter.add("Read: " + message);
	}
	
	public void clear() {
		//TODO: Add a button that clears the ListView.
		logAdapter.clear();
	}
}
