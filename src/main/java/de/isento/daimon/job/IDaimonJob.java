package de.isento.daimon.job;

import javax.persistence.EntityManager;

public interface IDaimonJob {
	
	public int runJob(String[] args);

}
