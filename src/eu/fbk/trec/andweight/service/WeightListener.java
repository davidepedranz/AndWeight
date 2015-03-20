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

import java.util.List;

import eu.fbk.trec.andweight.model.AndWeight;

/**
 * Interface for callback from the ConnectionThread to the Service.
 * 
 * @author Davide Pedranz (davide.pedranz@gmail.com)
 */
public interface WeightListener {

	public void onNewThread(Thread thread);

	public void onConnect();

	public void onDisonnect();

	public void onError();

	public void onMeasuring();

	public void onWeight(AndWeight weight);

	public void onWeightList(List<AndWeight> weights);

	public void onInvalidMeasure();

}
