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

import java.io.Serializable;
import java.util.Date;

public class AndWeight implements Serializable {
	private static final long serialVersionUID = 7369085597368033079L;

	private float weight;
	private Date measureDate;

	public AndWeight(float weight, Date measureDate) {
		this.weight = weight;
		this.measureDate = measureDate;
	}

	public float getWeight() {
		return weight;
	}

	public Date getMeasureDate() {
		return measureDate;
	}

	@Override
	public String toString() {
		return "Weight [weight=" + weight + ", measureDate=" + measureDate + "]";
	}

}
