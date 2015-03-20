/**
 * Copyright © 2015 e-Health Research Unit - Fondazione Bruno Kessler 
 * http://ehealth.fbk.eu/
 * 
 * This document is a part of the source code and related artifacts of 
 * the TreC Project. All rights reserved.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
package eu.fbk.trec.andweight.model;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import eu.fbk.trec.andweight.exceptions.InvalidMeasureException;
import eu.fbk.trec.andweight.exceptions.PacketExpection;
import eu.fbk.trec.andweight.utils.HexUtil;

/**
 * This class represent an incoming packet from the weight scale.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public class InPacket {
	private static final String TAG = InPacket.class.getSimpleName();

	// useful constants
	private static final int HEADER_LENGHT = 60;
	private static final int WEIGHT_LENGHT = 21;

	// error messages
	private static final String NOT_DATA = "Not a data packet: ";
	private static final String NOT_A_WEIGHT = "Not a weight packet: ";

	// raw data
	private byte[] packet;

	/**
	 * Represent the type of the packet.
	 */
	public enum Type {
		REQUEST_PATIENT_INFO, CANNOT_WAIT_ANYMORE_FOR_PATIENT_INFO, DATA_NOT_WEIGHT, WEIGHT, UNKNOWN
	}

	/**
	 * Parse a long message for single InPacket
	 */
	public final static List<InPacket> parseStream(byte[] buffer, int length) {
		List<InPacket> packets = new LinkedList<>();

		// extract the real data
		byte[] tmp = new byte[length];
		System.arraycopy(buffer, 0, tmp, 0, length);

		if (tmp[0] == 0x50 && tmp.length > 9) {
			byte[] b1 = new byte[6];
			System.arraycopy(tmp, 0, b1, 0, b1.length);

			byte[] b2 = new byte[tmp.length - b1.length];
			System.arraycopy(tmp, b1.length, b2, 0, b2.length);

			InPacket p1 = new InPacket(b1, b1.length);
			packets.add(p1);

			InPacket p2 = new InPacket(b2, b2.length);
			packets.add(p2);

		} else {
			InPacket p = new InPacket(tmp, tmp.length);
			packets.add(p);
		}

		Log.v(TAG, "parseStream -> " + packets.size());
		return packets;
	}

	/**
	 * Packet constructor. Copy the n read bytes and construct a packet.
	 * 
	 * @param buffer
	 *            Byte array buffer.
	 * @param length
	 *            Number of read bytes.
	 */
	public InPacket(byte[] buffer, int length) {
		this.packet = new byte[length];
		System.arraycopy(buffer, 0, this.packet, 0, length);
	}

	/**
	 * Check if this packet is a request for patient info.
	 */
	private boolean isRequestPatientInfo() {
		// patient info packet format
		// P W R Q P I , X X (XX -> patient number)

		if (packet.length == 9 && packet[0] == 0x50 && packet[1] == 0x57 && packet[2] == 0x52 && packet[3] == 0x51
				&& packet[4] == 0x50 && packet[5] == 0x49 && packet[6] == 0x2C) {
			return true;
		}

		return false;
	}

	/**
	 * Check if this packet means that the weight scale cannot wait for the patient info anymore.
	 * 
	 * After this packet the weight scale sends immediately the weights.
	 */
	private boolean isCannotWait() {
		// cannot wait packet format
		// P W C A P I

		if (packet.length == 6 && packet[0] == 0x50 && packet[1] == 0x57 && packet[2] == 0x43 && packet[3] == 0x41
				&& packet[4] == 0x50 && packet[5] == 0x49) {
			return true;
		}

		return false;
	}

	/**
	 * Check if this packet contains data (e.g. a weight measure).
	 */
	public boolean isData() {

		// check length
		if (packet.length < HEADER_LENGHT) {
			return false;
		}

		// check first byte
		if (packet[0] != 0x02) {
			return false;
		}

		// this packet assumes the weight scale model
		// UC-351PBT-Ci or compatible
		// TODO: implement the other packages' models

		// check the balance model
		if (packet[6] == 0x42 && packet[7] == 0x01) {
			return true;
		}

		return false;
	}

	/**
	 * Check if this packet contains a weight measure.
	 */
	public boolean isWeight() {

		// check if data
		if (!isData()) {
			return false;
		}

		// check length
		if (packet.length != HEADER_LENGHT + WEIGHT_LENGHT) {
			return false;
		}

		// start with 02 00 15 (HEX)
		if (packet[0] == 0x02 && packet[1] == 0x00 && packet[2] == 0x15 && packet[60] == 0x53) {
			return true;
		}

		return false;
	}

	/**
	 * Return the type of this packet.
	 */
	public Type getType() {
		if (isRequestPatientInfo()) {
			return Type.REQUEST_PATIENT_INFO;
		}

		if (isCannotWait()) {
			return Type.CANNOT_WAIT_ANYMORE_FOR_PATIENT_INFO;
		}

		if (isData()) {
			if (isWeight()) {
				return Type.WEIGHT;
			} else {
				return Type.DATA_NOT_WEIGHT;
			}
		}

		return Type.UNKNOWN;
	}

	/**
	 * Get the measure Date.
	 * 
	 * @throws PacketExpection
	 */
	public Date getMeasureDate() throws PacketExpection {
		if (!isData()) {
			throw new PacketExpection(NOT_DATA + HexUtil.dump(packet));
		}

		int year = (packet[10] << 8) & 0xff00 | (packet[9] << 0) & 0x00ff;
		int month = packet[11];
		int day = packet[12];
		int hour = packet[13];
		int minute = packet[14];
		int second = packet[15];

		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, minute, second);

		return c.getTime();
	}

	/**
	 * Get the transmission Date.
	 * 
	 * @throws PacketExpection
	 */
	public Date getTrasmissionDate() throws PacketExpection {
		if (!isData()) {
			throw new PacketExpection(NOT_DATA + HexUtil.dump(packet));
		}

		int year = (packet[17] << 8) & 0xff00 | (packet[16] << 0) & 0x00ff;
		int month = packet[18];
		int day = packet[19];
		int hour = packet[20];
		int minute = packet[21];
		int second = packet[22];

		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, minute, second);

		return c.getTime();
	}

	/**
	 * Get the patient number.
	 * 
	 * @throws PacketExpection
	 */
	public int getPatientNumber() throws PacketExpection {

		// check if weight
		if (!isWeight()) {
			throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
		}

		// remove header
		byte[] ww = new byte[WEIGHT_LENGHT];
		System.arraycopy(packet, HEADER_LENGHT, ww, 0, WEIGHT_LENGHT);

		// extract the patient info
		byte[] patientBytes = new byte[2];
		System.arraycopy(ww, 17, patientBytes, 0, 2);

		// compute the patient
		String patientString = new String(patientBytes);
		int patient = Integer.parseInt(patientString);

		return patient;
	}

	/**
	 * Return the weight measured contained in the packet.
	 * 
	 * @return Weight (in KG)
	 * @throws PacketExpection
	 *             If not a weight.
	 * @throws InvalidMeasureException
	 *             If the measure is invalid.
	 */
	public float getWeight() throws PacketExpection, InvalidMeasureException {
		try {

			// check if weight
			if (!isWeight()) {
				throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
			}

			// remove header
			byte[] ww = new byte[WEIGHT_LENGHT];
			System.arraycopy(packet, HEADER_LENGHT, ww, 0, WEIGHT_LENGHT);

			// check first byte (0x53 = 'S')
			if (ww[0] != 0x53) {
				throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
			}

			// check measure type
			char type = (char) ww[1];
			switch (type) {
			case 'T':
			case 'L':
				// measure valid, do nothing
				break;

			case 'E':
				// measure invalid
				throw new InvalidMeasureException();

			default:
				// something went wrong
				throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
			}

			// extract the weight
			byte[] weightBytes = new byte[7];
			System.arraycopy(ww, 3, weightBytes, 0, 7);

			// extract the measure unit
			byte[] unitBytes = new byte[2];
			System.arraycopy(ww, 10, unitBytes, 0, 2);

			// compute the weight
			String weightString = new String(weightBytes);
			float weight = Float.parseFloat(weightString);

			// compute the unit
			String unit = new String(unitBytes);

			// check if kg or lb
			switch (unit) {
			case "kg":
				// do nothing, already kg
				break;

			case "lb":
				// convert kg to lbs
				weight = round(weight * 0.453592f);
				break;

			default:
				// something went wrong
				throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
			}

			return weight;

		} catch (ArrayIndexOutOfBoundsException e) {
			// something went wrong
			throw new PacketExpection(NOT_A_WEIGHT + HexUtil.dump(packet));
		}
	}

	/**
	 * Round a float number to 1 digit.
	 */
	private static float round(float numberToRound) {
		return round(numberToRound, 1);
	}

	/**
	 * Round a float number to n digit.
	 */
	private static float round(float numberToRound, int numberOfDecimals) {
		int n = (int) Math.pow(10, numberOfDecimals);
		return (float) Math.round(numberToRound * n) / n;
	}

	@Override
	public String toString() {
		return "InPacket, dump=" + HexUtil.dump(packet) + ", ascii=" + HexUtil.ascii(packet);
	}

}
