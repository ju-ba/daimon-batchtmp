package de.isento.daimon.job;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.TLD;
import de.isento.daimon.entities.Website;


/**
 * @author jb
 * ScanDomainsForWebsitesJob
 * 
 * Scans the list of Domains for a specific TLD (e.g. "de") to detect if its a Website
 * If the Domain redirects to a subdomain, the subdomain (e.g. "www.domain.tld") is fetched/created and checked for being a Website, too.
 *
 *
 */
public class ScanDomainsForWebsitesJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {

		TLD tld = DomainUtils.getTLDMap().get("de");

		int batchSize = 100;
		int currentPosition = 0;

		List<Domain> domainList = DomainUtils.getDomainList(tld, currentPosition, batchSize);

		while (domainList.size() > 0) { //TODO: test this condition, if correct on last fetch, too
			
			DomainUtils.startTransaction();

			for (int i = 0; i < domainList.size(); i++) {
				Domain currentDomain = domainList.get(i);
				String currentDomainName = currentDomain.getFullName();

				try {
					String url = "http://" + currentDomainName;
					Response response = Jsoup.connect(url)
							.userAgent("Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0")
							.followRedirects(true).execute();
					URL responseUrl = response.url();
					System.out.println("Protocol: " + responseUrl.getProtocol());
					url = "https://" + currentDomainName;
					
					Website website = DomainUtils.getWebsiteForDomain(currentDomain, responseUrl.getHost());
					if (responseUrl.getProtocol().equals("https")) {
						website.setHttps(true);
					} else {
						website.setHttps(false);
					}

					System.out.println("ResponsURL.getHost: " + responseUrl.getHost());
					System.out.println("ResponsURL.getPath: " + responseUrl.getPath());
					
					Document doc = Jsoup.connect(responseUrl.getProtocol() +  "://" + responseUrl.getHost()).get();
					Elements metalinks = doc.select("meta[name=generator]");
					if (metalinks != null) {
						System.out.println("Generator: " + metalinks.attr("content"));
					}
					System.out.println("");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			DomainUtils.commitTransaction();
			
			currentPosition += batchSize;
			domainList = DomainUtils.getDomainList(tld, currentPosition, batchSize);
			
		}

		return 0;
	}

}
