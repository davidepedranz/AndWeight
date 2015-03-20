/**
 * Copyright © 2015 e-Health Research Unit - Fondazione Bruno Kessler 
 * http://ehealth.fbk.eu/
 * 
 * This document is a part of the source code and related artifacts of 
 * the TreC Project. All rights reserved.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
package eu.fbk.trec.andweight.service;

import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import eu.fbk.trec.andweight.model.AndWeight;
import eu.fbk.trec.andweight.model.WeightsWrapper;

public class BluetoothService extends Service implements WeightListener {
	public final static String TAG = BluetoothService.class.getSimpleName();

	// intent
	public static final String INTENT = "intent";
	public static final boolean START = true;
	public static final boolean STOP = !START;

	// communication with activity
	public static final String TYPE = "type";
	public static final int TYPE_CONTROL = 100;
	public static final int TYPE_DATA_SINGLE = 200;
	public static final int TYPE_DATA_LIST = 300;
	public static final String STATUS = "status";
	public static final String WEIGHT = "weight";
	public static final String WEIGHTS_LIST = "weights_list";

	// status codes (errors)
	public static final int NO_BT_ADAPTER = -1;
	public static final int BT_TURNED_OFF = -2;
	public static final int COMUNICATION_ERROR = -10;
	public static final int INVALID_MEASURE = -20;

	// status codes
	public static final int SERVICE_STARTED = 1;
	public static final int SERVICE_ENDED = 2;
	public static final int LISTEN_START = 3;
	public static final int DEVICE_CONNECTED = 5;
	public static final int DEVICE_DISCONNECT = 6;
	public static final int MEASURING = 7;
	public static final int TURNING_BT_ON = 8;
	public static final int TURNING_BT_OFF = 9;

	// BlueTooth adapter
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	// status at start
	private boolean wasBluetoothOn;

	// Thread list
	private final List<Thread> threads = new LinkedList<>();

	/**
	 * Receive callback from the BluetoothAdapter.
	 */
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			switch (intent.getAction()) {

			case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
				Log.v(TAG, "ACTION_CONNECTION_STATE_CHANGED");
				break;

			case BluetoothAdapter.ACTION_STATE_CHANGED:
				Log.v(TAG, "ACTION_STATE_CHANGED");

				switch (intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE)) {
				case BluetoothAdapter.STATE_ON:
					Log.v(TAG, "ACTION_STATE_CHANGED -> STATE_ON");

					if (!wasBluetoothOn) {
						startListening();
					}

					break;

				case BluetoothAdapter.STATE_OFF:
					Log.v(TAG, "ACTION_STATE_CHANGED -> STATE_OFF");
					break;

				case BluetoothAdapter.STATE_TURNING_ON:
					Log.v(TAG, "ACTION_STATE_CHANGED -> STATE_TURNING_ON");
					break;

				case BluetoothAdapter.STATE_TURNING_OFF:
					Log.v(TAG, "ACTION_STATE_CHANGED -> STATE_TURNING_OFF");
					updateStatus(BT_TURNED_OFF);

					// if the user switch the BlueTooth off
					// stop and exit
					stop();

					break;
				}
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "OnCreate");

		// register receiver
		registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
		registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

		// exit if BlueTooth adapter not present
		if (bluetoothAdapter == null) {
			updateStatus(NO_BT_ADAPTER);
			stop();
		}

		// check BlueTooth initial state (to turn it off at the end if was off
		wasBluetoothOn = bluetoothAdapter.isEnabled();
		if (!wasBluetoothOn) {
			updateStatus(TURNING_BT_ON);
			bluetoothAdapter.enable();
		} else {
			startListening();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// if stop -> stop the service
		if (intent.getBooleanExtra(INTENT, STOP) == STOP) {
			Log.v(TAG, "onStartCommand: STOP");
			stop();
		} else {
			Log.v(TAG, "onStartCommand: START");
			updateStatus(SERVICE_STARTED);
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");

		// unregister receiver
		unregisterReceiver(bluetoothReceiver);

		// turn off BlueTooth if needed
		if (!wasBluetoothOn) {
			updateStatus(TURNING_BT_OFF);
			bluetoothAdapter.disable();
		}

		updateStatus(SERVICE_ENDED);
	}

	/**
	 * Start the ListenThread.
	 */
	private void startListening() {
		Log.v(TAG, "startListening");

		// launch a thread which listen for incoming connections
		ListenThread listenThread = new ListenThread(this);
		listenThread.start();

		updateStatus(LISTEN_START);
	}

	/**
	 * Method to call to terminate correctly this service.
	 */
	private void stop() {
		Log.i(TAG, "stop...");

		// kill every running thread
		for (Thread t : threads) {
			Log.v(TAG, "killing thread " + t);
			if (t != null) {
				t.interrupt();
			}
		}

		// terminate
		stopSelf();
	}

	/**
	 * Send a status update to the connected activities.
	 * 
	 * @param status
	 *            Status code.
	 */
	private void updateStatus(int status) {
		Intent intent = new Intent(TAG);
		intent.putExtra(TYPE, TYPE_CONTROL);
		intent.putExtra(STATUS, status);
		sendBroadcast(intent);
	}

	@Override
	public void onNewThread(Thread thread) {
		synchronized (this) {
			threads.add(thread);
			Log.v(TAG, thread.toString());
		}
	}

	@Override
	public void onConnect() {
		synchronized (this) {
			updateStatus(DEVICE_CONNECTED);
		}
	}

	@Override
	public void onDisonnect() {
		synchronized (this) {
			updateStatus(DEVICE_DISCONNECT);
			stop();
		}
	}

	@Override
	public void onError() {
		synchronized (this) {
			updateStatus(COMUNICATION_ERROR);
		}
	}

	@Override
	public void onMeasuring() {
		synchronized (this) {
			updateStatus(MEASURING);
		}
	}

	@Override
	public void onWeight(AndWeight weight) {
		// do nothing...
		// see onWeightList()

		// synchronized (this) {
		// Intent intent = new Intent(TAG);
		// intent.putExtra(TYPE, TYPE_DATA);
		// intent.putExtra(WEIGHT, weight);
		// sendBroadcast(intent);
		// }
	}

	@Override
	public void onWeightList(List<AndWeight> weights) {
		synchronized (this) {
			Log.i(TAG, "onWeightList");

			Intent intent = new Intent(TAG);
			intent.putExtra(TYPE, TYPE_DATA_LIST);
			intent.putExtra(WEIGHTS_LIST, new WeightsWrapper(weights));
			sendBroadcast(intent);

			Log.i(TAG, "AFTER onWeightList");
		}
	}

	@Override
	public void onInvalidMeasure() {
		synchronized (this) {
			updateStatus(INVALID_MEASURE);
		}
	}

}
