package de.isento.daimon.job;

import java.util.ArrayList;

import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;
import de.isento.daimon.entities.TLD;

/**
 * @author jb ScanDomainsForWebsitesJob
 * 
 *         Scans the list of Domains for a specific TLD (e.g. "de") to detect if
 *         its a Website If the Domain redirects to a subdomain, the subdomain
 *         (e.g. "www.domain.tld") is fetched/created and checked for being a
 *         Website, too.
 *
 *
 */
public class ScanDomainsForWebsitesJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {

		TLD tld = DomainUtils.getTLDMap().get("com");

		int batchSize = 100000;
		int currentPosition = 0;
		int maxSize = 25000000;

		ArrayList<DomainForWebsiteScanner> scannerList = new ArrayList<DomainForWebsiteScanner>();

		EntityManagerHelper.beginTransaction();

		while (currentPosition < maxSize) {
			scannerList.add(new DomainForWebsiteScanner(currentPosition, batchSize, tld));
			currentPosition += batchSize;
		}

		for (DomainForWebsiteScanner entry : scannerList) {
			entry.start();
			System.out.println("Started scanner " + entry.getStartPosition());
		}
		
		for (DomainForWebsiteScanner entry : scannerList) {
			try {
				entry.join();
				System.out.println("Joined scanner " + entry.getStartPosition());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		EntityManagerHelper.commit();

		return 0;
	}

}
