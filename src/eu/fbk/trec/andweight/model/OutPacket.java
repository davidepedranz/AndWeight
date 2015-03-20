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

public class OutPacket {

	// keep the measurement in memory and power down
	public static final byte[] REFUSED = { 0x50, 0x57, 0x41, 0x30 };

	// accept the measure and disconnect
	// now the weight scale tries to connect back to the phone...
	public static final byte[] ACCEPTED = { 0x50, 0x57, 0x41, 0x31 };

	// accept the measure and enter in configuration mode
	public static final byte[] ACCEPTED_CONFIG = { 0x50, 0x57, 0x41, 0x32 };

	// accept the measure and delete all the data in memory
	public static final byte[] ACCEPTED_DELETE_ALL = { 0x50, 0x57, 0x41, 0x33 };

	// RECOMMENDED: accept the measure and do not disconnect
	// now the weight scale sends the other measurements on the same BlueTooth connection
	public static final byte[] ACCEPTED_NO_DISCONNECT = { 0x50, 0x57, 0x41, 0x34 };
}
