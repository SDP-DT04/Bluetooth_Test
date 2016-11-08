package com.dt04.bluetoothtest;

import java.io.IOException;
import java.util.Calendar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity 
{
	boolean isPine64 = false;

	int REQUEST_ENABLE_BT = 13;
	String TEST_BT_DEVICE_ADDR = "34:C3:D2:7A:DD:B8";
	String MY_UUID = "dt04";
	String NAME = "bt_test";
	String mRssiInfoStr; 
	BluetoothDevice mTestDevice; 
	BluetoothAdapter mBluetoothAdapter;
	Button mStartButton;
    int mCount;
    TextView mTextView;


	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
            Log.d("APP", action);
            if (action.equals(BluetoothDevice.ACTION_FOUND))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("APP", device.toString() + " : " + device.getAddress());
                if (device.getAddress().equals(TEST_BT_DEVICE_ADDR))
				{
					mTestDevice = device;
                    String info = Integer.toString(mCount++) + "," + Short.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0)) + "\n";
                    mRssiInfoStr += info;
                    mTextView.setText(mTextView.getText().toString() + info);
					mBluetoothAdapter.cancelDiscovery(); 
				}
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		final Button measureButton = (Button) findViewById(R.id.measure_button);
		measureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                if (mStartButton.getText().toString().equals("Stop")) {
                    mBluetoothAdapter.startDiscovery();
                }
			}
		});
		mStartButton = (Button) findViewById(R.id.start_button); 
		mStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startStopTest();
			}
		});
		mTextView = (TextView)findViewById(R.id.textView);

		mRssiInfoStr = "";
        bluetoothInit();
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		//bluetoothInit();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		
		unregisterReceiver(mReceiver);	
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_ENABLE_BT)
		{
			if (resultCode == RESULT_OK)
			{

                bluetoothInit();
			}
		}
	}

	public void bluetoothInit()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null)
		{
			// device does not support bluetooth	
		}

		if (!mBluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return; 
		}

		if (isPine64)
		{
            Log.d("APP", "onActivityResult: ");
			makeDiscoverable(); 
		}
		//else
		//{
		//	mBluetoothAdapter.startDiscovery(); 
		//}
	}

	public void makeDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
		startActivity(discoverableIntent);
	}

	public void startStopTest()
	{
		if (mStartButton.getText().toString().equals("Stop"))
		{
			mStartButton.setText("Start");
            mStartButton.setBackgroundColor(getResources().getColor(R.color.start));
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"ees37@zips.uakron.ed"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "RSSI data"); 
			intent.putExtra(Intent.EXTRA_TEXT, mRssiInfoStr); 

			startActivity(Intent.createChooser(intent, "Send Email"));
		}
		else
		{
			mStartButton.setText("Stop");
            mStartButton.setBackgroundColor(getResources().getColor(R.color.stop));
			mRssiInfoStr = ""; 	
		}
	}
}
