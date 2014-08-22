package com.example.ibeacontest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BeaconMainActivity extends Activity {
	private final static String TAG = "BeaconMainActivity";
	private final static int REQ_ENABLE_BT		= 2000;
	private static void log(String s) {
		Log.d(TAG, s);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beacon_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	public static class PlaceholderFragment extends Fragment implements iBeaconScanManager.OniBeaconScan{
		private iBeaconScanManager miScaner	= null;
		private BeaconListAdapter mListAdapter		= null;
		private List<ScanediBeacon> miBeacons	= new ArrayList<ScanediBeacon>();
		private ListView mLVBLE= null;
		private static BluetoothAdapter mBLEAdapter= BluetoothAdapter.getDefaultAdapter();
		
		private BeaconHandler mBCHandler= new BeaconHandler();

		// constants
		final int MSG_SCAN_IBEACON			= 1000;
		final int MSG_UPDATE_BEACON_LIST	= 1001;
		final int TIME_BEACON_TIMEOUT		= 10000;
		final int TIME_BEACON_SCANNING		= 1600;
		final int TIME_BEACON_SCAN_INTERVAL	= 3000;
		final int TIME_LIST_DATA_UPDATE		= 2000;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_beacon_main,
					container, false);
			return rootView;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
	  	{
	  		log("onActivityResult()");

	  		switch(requestCode)
	  		{
	  			case REQ_ENABLE_BT:
		  			if(RESULT_OK == resultCode)
		  			{
		  				log("REQ_ENABLE_BT - RESULT_OK");
		  				sendScanAndUpdateMsgs();
					}
		  			break;
	  		}
	  	}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			log("onActivityCreated");
			/** create instance of iBeaconScanManager. */
			miScaner 		= new iBeaconScanManager(this.getActivity(), this);
			mListAdapter	= new BeaconListAdapter(this.getActivity());
			mLVBLE			= (ListView)(this.getView().findViewById(R.id.beacon_list));
			mLVBLE.setAdapter(mListAdapter);

			//check if bt enabled
			if(!mBLEAdapter.isEnabled())
			{
				Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, REQ_ENABLE_BT);
			}
			else
			{
				sendScanAndUpdateMsgs();
			}
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			log("onDestroy");
			mBCHandler.removeCallbacksAndMessages(null);
			miScaner.stopScaniBeacon();
		}

		@Override
		public void onPause() {
			super.onPause();
			log("onPause");
		}

		@Override
		public void onResume() {
			super.onResume();
			log("onResume");
		}

		@Override
		public void onScaned(iBeaconData iBeacon) {
			log("onScaned : calDistance=" + iBeacon.calDistance()
					+ ", UUID:" + iBeacon.beaconUuid
					+ ", oneMeterRssi" +  iBeacon.oneMeterRssi
					+ ", rssi" + iBeacon.rssi);
			
			synchronized(mListAdapter)
			{
				addOrUpdateiBeacon(iBeacon);
			}
		}

		public void sendScanAndUpdateMsgs(){
			Message msg= Message.obtain(mBCHandler, MSG_SCAN_IBEACON,
					TIME_BEACON_SCANNING, TIME_BEACON_SCAN_INTERVAL);
			msg.sendToTarget();
			mBCHandler.sendEmptyMessageDelayed(MSG_UPDATE_BEACON_LIST, 500);
		}

		public void addOrUpdateiBeacon(iBeaconData iBeacon)
		{
			log("addOrUpdateiBeacon - rssi=" + iBeacon.rssi + ", uuid=" + iBeacon.beaconUuid);
			long currTime= System.currentTimeMillis();
			
			ScanediBeacon beacon= null;
			
			for(ScanediBeacon b : miBeacons)
			{
				if(b.equals(iBeacon, false))
				{
					beacon= b;
					break;
				}
			}
			
			if(null == beacon)
			{
				beacon= ScanediBeacon.copyOf(iBeacon);
				miBeacons.add(beacon);
			}
			else
			{
				beacon.rssi= iBeacon.rssi;
			}
			
			beacon.lastUpdate= currTime;
		}
		public void verifyiBeacons()
		{
			log("verifyiBeacons");
			{
				long currTime	= System.currentTimeMillis();
				
				int len= miBeacons.size();
				ScanediBeacon beacon= null;
				
				for(int i= len- 1; 0 <= i; i--)
				{
					beacon= miBeacons.get(i);
					
					if(null != beacon && TIME_BEACON_TIMEOUT < (currTime- beacon.lastUpdate))
					{
						miBeacons.remove(i);
					}
				}
			}
			
			{
				mListAdapter.clear();
				
				for(ScanediBeacon beacon : miBeacons)
				{
					log("verifyiBeacons : add UUID " +  beacon.beaconUuid.toString().toUpperCase(Locale.getDefault()));
					mListAdapter.addItem(new ListItem(beacon.beaconUuid.toString().toUpperCase(Locale.getDefault()),
							""+ beacon.major,
							""+ beacon.minor,
							""+ beacon.rssi,
							""+ beacon.calDistance()));
				}
			}
		}
		/** ================================================ */
		private class BeaconHandler extends Handler {
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
					case MSG_SCAN_IBEACON:
						{
							int timeForScaning		= msg.arg1;
							int nextTimeStartScan	= msg.arg2;
							log("MSG_SCAN_IBEACON - startScaniBeacon");
							
							miScaner.startScaniBeacon(timeForScaning);
							this.sendMessageDelayed(Message.obtain(msg), nextTimeStartScan);
						}
						break;
						
					case MSG_UPDATE_BEACON_LIST:
						synchronized(mListAdapter)
						{
							verifyiBeacons();
							mListAdapter.notifyDataSetChanged();
							mBCHandler.sendEmptyMessageDelayed(MSG_UPDATE_BEACON_LIST,
									TIME_LIST_DATA_UPDATE);
						}
						break;
				}
			}
		};
	}//end of fragment
}
