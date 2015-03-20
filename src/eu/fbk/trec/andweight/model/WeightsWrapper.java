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
import java.util.List;

/**
 * This class only wraps a list of AndWeight objects.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public class WeightsWrapper implements Serializable {
	private static final long serialVersionUID = 7209809263040325607L;

	private List<AndWeight> weights;

	public WeightsWrapper(List<AndWeight> weights) {
		this.weights = weights;
	}

	public List<AndWeight> getWeights() {
		return weights;
	}

}
