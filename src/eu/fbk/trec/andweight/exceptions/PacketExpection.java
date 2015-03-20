/**
 * Copyright © 2015 e-Health Research Unit - Fondazione Bruno Kessler 
 * http://ehealth.fbk.eu/
 * 
 * This document is a part of the source code and related artifacts of 
 * the TreC Project. All rights reserved.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
package eu.fbk.trec.andweight.exceptions;

public class PacketExpection extends Exception {
	private static final long serialVersionUID = 4332631627507772329L;

	public PacketExpection(String detailMessage) {
		super(detailMessage);
	}

}
