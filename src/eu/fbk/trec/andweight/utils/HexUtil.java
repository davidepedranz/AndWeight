/**
 * Copyright © 2015 e-Health Research Unit - Fondazione Bruno Kessler 
 * http://ehealth.fbk.eu/
 * 
 * This document is a part of the source code and related artifacts of 
 * the TreC Project. All rights reserved.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
package eu.fbk.trec.andweight.utils;

/**
 * Useful methods to print a byte array.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public class HexUtil {

	private HexUtil() {
	}

	private static final char toHexChar(int b) {
		switch (0XF & b) {
		case 0xF:
			return 'F';
		case 0xE:
			return 'E';
		case 0xD:
			return 'D';
		case 0xC:
			return 'C';
		case 0xB:
			return 'B';
		case 0xA:
			return 'A';
		default:
			return (char) ('0' + (0xF & b));
		}
	}

	private static final String toHexString(int b) {
		return "" + toHexChar(b >> 4) + toHexChar(b);
	}

	public static final String dump(byte[] bb) {
		return dump(bb, bb.length);
	}

	public static final String dump(byte[] bb, int firstN) {
		if (bb.length < firstN) {
			throw new RuntimeException("bb.length < firstN");
		}

		StringBuilder sb = new StringBuilder("[ ");
		for (int i = 0; i < firstN; i++) {
			int b = 0xFF & bb[i];
			sb.append(toHexString(b)).append(" ");
		}
		sb.append("]");

		return sb.toString();
	}

	public static final int intFromAsciiHex(byte b) {
		return Integer.parseInt(String.valueOf((char) b));
	}

	public static final String ascii(byte[] bb) {
		return ascii(bb, bb.length);
	}

	public static final String ascii(byte[] bb, int firstN) {
		if (bb.length < firstN) {
			throw new RuntimeException("bb.length < firstN");
		}

		StringBuilder sb = new StringBuilder("[ ");
		for (int i = 0; i < firstN; i++) {
			switch (bb[i]) {
			case 0x0A:
				sb.append("LF");
				break;

			case 0x0D:
				sb.append("CR");
				break;

			default:
				sb.append(" ").append((char) bb[i]);
			}

			sb.append(" ");
		}
		sb.append("]");

		return sb.toString();
	}

}
