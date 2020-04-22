package de.isento.daimon.job;

import java.util.Arrays;
import java.util.List;

import de.isento.daimon.entities.DomainUtils;

public class InitCMSJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {

		List<String> cmsNames = Arrays.asList("WordPress", "sup2", "sup3");
		DomainUtils.startTransaction();
		
		for(String cmsName : cmsNames) {
			
		}

		DomainUtils.commitTransaction();

		return 0;
	}
}
