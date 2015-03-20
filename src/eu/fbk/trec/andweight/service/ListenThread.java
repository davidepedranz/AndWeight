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

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ListenThread extends Thread {
	private static final String TAG = ListenThread.class.getSimpleName();

	// BlueTooth
	private static final String NAME = "Service Name";
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final WeightListener listener;

	public ListenThread(WeightListener listener) {
		if (listener == null) {
			throw new RuntimeException("Listener cannot be null!");
		}
		this.listener = listener;

		// communicate to the service that this thread as been created
		listener.onNewThread(this);
	}

	@Override
	public void run() {
		Log.v(TAG, "START ListenThread");

		try {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			BluetoothServerSocket s = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, uuid);

			while (true) {
				Log.i(TAG, "before connection");
				BluetoothSocket socket = s.accept();
				Log.i(TAG, "connection from: " + socket.getRemoteDevice().getName());

				ConnectionThread connectedThread = new ConnectionThread(socket, listener);
				connectedThread.start();
				s.close();

				// accept only 1 incoming connection
				break;
			}

		} catch (IOException e) {
			Log.e(TAG, "cannot accept incoming connections...", e);
			listener.onError();
		}

		Log.v(TAG, "END ListenThread");
	}

}
