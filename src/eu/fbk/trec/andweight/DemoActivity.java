/**
 * Copyright © 2015 e-Health Research Unit - Fondazione Bruno Kessler 
 * http://ehealth.fbk.eu/
 * 
 * This document is a part of the source code and related artifacts of 
 * the TreC Project. All rights reserved.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
package eu.fbk.trec.andweight;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import eu.fbk.trec.andweight.model.AndWeight;
import eu.fbk.trec.andweight.model.WeightsWrapper;
import eu.fbk.trec.andweight.service.BluetoothService;

/**
 * Demo activity.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public class DemoActivity extends Activity {
	private static final String TAG = DemoActivity.class.getSimpleName();

	private static final String START_BUTTON_ENABLED = "startButtonEnabled";
	private static final String STOP_BUTTON_ENABLED = "stopButtonEnabled";

	private static final String STATUS = "status";
	private static final String WEIGHT = "weight";
	private static final String DATE = "date";

	// layout
	private Button startButton;
	private Button stopButton;
	private TextView dateTV;
	private TextView weightTV;
	private TextView statusTV;

	/**
	 * Handles every event from the service.
	 */
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int type = bundle.getInt(BluetoothService.TYPE);

				switch (type) {
				case BluetoothService.TYPE_CONTROL:
					int control = bundle.getInt(BluetoothService.STATUS);

					switch (control) {

					case BluetoothService.NO_BT_ADAPTER:
						Log.v(TAG, "NO_BT_FOUND");
						statusTV.setText("NO_BT_FOUND");
						break;

					case BluetoothService.BT_TURNED_OFF:
						Log.v(TAG, "BT_TURNED_OFF");
						statusTV.setText("BT_TURNED_OFF");
						break;

					case BluetoothService.COMUNICATION_ERROR:
						Log.v(TAG, "COMUNICATION_ERROR");
						statusTV.setText("COMUNICATION_ERROR");
						break;

					case BluetoothService.SERVICE_STARTED:
						Log.v(TAG, "SERVICE_STARTED");
						statusTV.setText("SERVICE_STARTED");
						break;

					case BluetoothService.SERVICE_ENDED:
						Log.v(TAG, "SERVICE_ENDED");
						statusTV.setText("SERVICE_ENDED");
						enableStart();
						break;

					case BluetoothService.LISTEN_START:
						Log.v(TAG, "LISTEN_START");
						statusTV.setText("LISTEN_START");
						break;

					case BluetoothService.DEVICE_CONNECTED:
						Log.v(TAG, "DEVICE_CONNECTED");
						statusTV.setText("DEVICE_CONNECTED");
						break;

					case BluetoothService.DEVICE_DISCONNECT:
						Log.v(TAG, "DEVICE_DISCONNECT");
						statusTV.setText("DEVICE_DISCONNECT");
						break;

					case BluetoothService.MEASURING:
						Log.v(TAG, "MEASURING");
						statusTV.setText("MEASURING");
						break;

					case BluetoothService.TURNING_BT_ON:
						Log.v(TAG, "TURNING_BT_ON");
						statusTV.setText("TURNING_BT_ON");
						break;

					case BluetoothService.TURNING_BT_OFF:
						Log.v(TAG, "TURNING_BT_OFF");
						statusTV.setText("TURNING_BT_OFF");
						break;
					}
					break;

				// case BluetoothService.TYPE_DATA_SINGLE:
				// AndWeight w = (AndWeight) bundle.getSerializable(BluetoothService.WEIGHT);
				//
				// Log.d(TAG, w.toString());
				//
				// dateTV.setText(String.valueOf(w.getWeight()));
				// weightTV.setText(String.valueOf(w.getMeasureDate().toString()));
				//
				// break;
				// }

				case BluetoothService.TYPE_DATA_LIST:
					WeightsWrapper wrapper = (WeightsWrapper) bundle.getSerializable(BluetoothService.WEIGHTS_LIST);
					List<AndWeight> w = wrapper.getWeights();

					weightTV.setText(w.get(0).getWeight() + " kg");
					dateTV.setText(String.valueOf(w.get(0).getMeasureDate().toString()));

					break;
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_demo);

		// prevent screen from locking
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// get layout elements
		startButton = (Button) findViewById(R.id.button_start);
		stopButton = (Button) findViewById(R.id.button_stop);

		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService();
				disableStart();

				weightTV.setText("-");
				dateTV.setText("-");
			}
		});

		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopService();
				enableStart();
			}
		});

		weightTV = (TextView) findViewById(R.id.tv_weight);
		dateTV = (TextView) findViewById(R.id.tv_date);
		statusTV = (TextView) findViewById(R.id.tv_status);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(serviceReceiver, new IntentFilter(BluetoothService.TAG));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(serviceReceiver);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		stopService();
		super.onDestroy();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		startButton.setEnabled(savedInstanceState.getBoolean(START_BUTTON_ENABLED));
		stopButton.setEnabled(savedInstanceState.getBoolean(STOP_BUTTON_ENABLED));

		dateTV.setText(savedInstanceState.getString(WEIGHT));
		weightTV.setText(savedInstanceState.getString(DATE));
		statusTV.setText(savedInstanceState.getString(STATUS));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(START_BUTTON_ENABLED, startButton.isEnabled());
		savedInstanceState.putBoolean(STOP_BUTTON_ENABLED, stopButton.isEnabled());

		savedInstanceState.putString(WEIGHT, dateTV.getText().toString());
		savedInstanceState.putString(DATE, weightTV.getText().toString());
		savedInstanceState.putString(STATUS, statusTV.getText().toString());
	}

	private void startService() {
		Log.v(TAG, "startService");
		Intent start = new Intent(this, BluetoothService.class);
		start.putExtra(BluetoothService.INTENT, BluetoothService.START);
		startService(start);
	}

	private void stopService() {
		Log.v(TAG, "stopService");
		Intent stop = new Intent(this, BluetoothService.class);
		stop.putExtra(BluetoothService.INTENT, BluetoothService.STOP);
		startService(stop);
	}

	private void enableStart() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}

	private void disableStart() {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

}
