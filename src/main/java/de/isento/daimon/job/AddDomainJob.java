package de.isento.daimon.job;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;

public class AddDomainJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {
		
		for (int i = 1; i<args.length; i++) {
			String currentDomain = args[i];
			Domain resultDomain = DomainUtils.getDomainByName(currentDomain);
			if (resultDomain == null) {
				DomainUtils.addDomain(currentDomain, true);
				System.out.println("Added domain " + currentDomain);
			} else {
				System.out.println("Domain " + resultDomain.getFullName() + " is already known");
			}
		}
		

		return 0;
	}

}
