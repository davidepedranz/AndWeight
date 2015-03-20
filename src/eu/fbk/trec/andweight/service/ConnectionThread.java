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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import eu.fbk.trec.andweight.exceptions.InvalidMeasureException;
import eu.fbk.trec.andweight.exceptions.PacketExpection;
import eu.fbk.trec.andweight.model.AndWeight;
import eu.fbk.trec.andweight.model.InPacket;
import eu.fbk.trec.andweight.model.OutPacket;
import eu.fbk.trec.andweight.utils.HexUtil;

/**
 * This thread is responsible for handling the communication with the weight scale.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public class ConnectionThread extends Thread {
	private static final String TAG = ConnectionThread.class.getSimpleName();

	private final WeightListener listener;
	private final BluetoothSocket socket;
	private final InputStream input;
	private final OutputStream output;

	public ConnectionThread(BluetoothSocket socket, WeightListener listener) throws IOException {
		this.socket = socket;
		this.listener = listener;

		if (socket == null || listener == null) {
			throw new RuntimeException("Socket and Listener cannot be null!");
		}

		input = socket.getInputStream();
		output = socket.getOutputStream();

		// communicate to the service that this thread as been created
		listener.onNewThread(this);
	}

	public void run() {
		Log.i(TAG, "START ConnectionThread");
		byte[] buffer = new byte[500];

		// weight list
		List<AndWeight> weights = new ArrayList<>();

		// Keep listening to the InputStream while connected
		while (socket.isConnected()) {
			try {
				// Read from the InputStream
				int bytes = input.read(buffer);

				// log message
				Log.d("IN  <<< ", HexUtil.dump(buffer, bytes));
				Log.v("in  ^^^ ", HexUtil.ascii(buffer, bytes));

				// wrap message
				// InPacket packet = new InPacket(buffer, bytes);

				// get packets
				List<InPacket> packets = InPacket.parseStream(buffer, bytes);

				for (InPacket packet : packets) {
					Log.d(TAG, packet.toString());

					// get type
					switch (packet.getType()) {
					case REQUEST_PATIENT_INFO:
						Log.d(TAG, "REQUEST_PATIENT_INFO");
						// TODO: implement patient info
						break;

					case CANNOT_WAIT_ANYMORE_FOR_PATIENT_INFO:
						Log.d(TAG, "CANNOT_WAIT_ANYMORE_FOR_PATIENT_INFO");
						break;

					case WEIGHT:
						try {
							// extract weight & date
							float weight = packet.getWeight();
							Date date = packet.getMeasureDate();

							// create Weight object
							AndWeight w = new AndWeight(weight, date);
							listener.onWeight(w);
							weights.add(w);
							Log.d(TAG, w.toString());

							// write response... OK, next measure
							output.write(OutPacket.ACCEPTED_NO_DISCONNECT);
							output.flush();

							// log message
							Log.d("OUT >>> ", HexUtil.dump(OutPacket.ACCEPTED_NO_DISCONNECT));
							Log.v("out ^^^ ", HexUtil.ascii(OutPacket.ACCEPTED_NO_DISCONNECT));

						} catch (InvalidMeasureException e) {
							Log.e(TAG, "InvalidMeasure...");
							listener.onInvalidMeasure();
						} catch (PacketExpection e) {
							Log.e(TAG, "PacketExpection... -> " + e.getMessage());
							listener.onError();
						}

						break;

					case DATA_NOT_WEIGHT:
						Log.d(TAG, "DATA_NOT_WEIGHT");
						listener.onError();
						break;

					case UNKNOWN:
					default:
						Log.d(TAG, "UNKNOWN");
						listener.onError();
						break;
					}
				}

			} catch (IOException e) {
				Log.e(TAG, "disconnected...");
				break;
			}

		}

		// send weight list to the service
		listener.onWeightList(weights);

		// close socket
		try {
			Log.v(TAG, "closing socket...");
			socket.close();
		} catch (IOException e) {
			Log.e(TAG, "close() of connect socket failed", e);
		}

		// send status disconnected
		listener.onDisonnect();

		Log.i(TAG, "END ConnectionThread");
	}
}