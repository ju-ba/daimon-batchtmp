package de.isento.daimon.job;

import java.util.Arrays;
import java.util.List;

import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;

public class InitCMSJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {

		List<String> cmsNames = Arrays.asList("WordPress", "sup2", "sup3");
		EntityManagerHelper.beginTransaction();
		
		for(String cmsName : cmsNames) {
			
		}

		EntityManagerHelper.commit();

		return 0;
	}
}
