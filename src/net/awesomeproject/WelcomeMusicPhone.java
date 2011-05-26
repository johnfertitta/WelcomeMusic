package net.awesomeproject;

import java.util.ArrayList;
import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class WelcomeMusicPhone extends BaseActivity {
	static final String TAG = "DemoKitPhone";
	/** Called when the activity is first created. */
	Button b;
	boolean state = false;
    private BluetoothAdapter mBtAdapter;
    private HashMap<String, Integer> songs;
    private static final int REQUEST_ENABLE_BT = 1;
    private static MediaPlayer mMediaPlayer = null;  
    private ArrayList<String> lastScan;
    private ArrayList<String> currentScan;
    private Context mContext;
    private ListView lv;
    private ArrayAdapter<String> aa;
    
	@Override
	protected void hideControls() {
		super.hideControls();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		songs = new HashMap<String, Integer>();
		
		songs.put("04:1E:64:11:85:92", R.raw.john);
		
		songs.put("60:33:4B:F2:E0:39", R.raw.chris);

		lastScan = new ArrayList<String>();

		// Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(mReceiver, filter);
        
	}

	@Override
	public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        sendCommand(DemoKitActivity.ADC_COMMAND, (byte) 0x1, 0);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
	
	protected void showControls() {
		super.showControls();
		lv = (ListView) findViewById(R.id.ListView01);
		aa = new ArrayAdapter<String>(mContext, R.layout.list_item);
        lv.setAdapter(aa);
		doDiscovery();
	}
	
	private void doDiscovery() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }
    
	private void playTrack(final String addr) {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) return;
		mMediaPlayer = MediaPlayer.create(this, songs.get(addr)); 
		mMediaPlayer.setLooping(false);  
		mMediaPlayer.start();
		sendCommand(DemoKitActivity.ADC_COMMAND, (byte) 0x1, 1);
		
		new Thread(new Runnable() {
	        public void run() {
	            try {
					Thread.sleep(mMediaPlayer.getDuration());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		sendCommand(DemoKitActivity.ADC_COMMAND, (byte) 0x1, 0);
	        }
	    }).start();
		
	}
	
	
	
    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                
                currentScan.add(address);
                aa.add(address);
                if (!lastScan.contains(address)) {
                	//it's new!
                	if (songs.containsKey(address)) {

                		playTrack(address);
                	}

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	lastScan = currentScan;
            	doDiscovery();
	            	
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
        		currentScan = new ArrayList<String>();
        		aa.clear();
            }
       
        }
    };


}