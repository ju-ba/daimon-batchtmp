package de.isento.daimon.job;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.TLD;
import de.isento.daimon.entities.Website;

public class DomainForWebsiteScanner extends Thread {

	private int startPosition;
	private int batchSize;
	private TLD tld;

	public DomainForWebsiteScanner(int startPosition, int batchSize, TLD tld) {
		this.startPosition = startPosition;
		this.batchSize = batchSize;
		this.tld = tld;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void run() {
		List<Domain> domainList = DomainUtils.getDomainList(tld, startPosition, batchSize);

		if (domainList.size() > 0) {

			for (int i = 0; i < domainList.size(); i++) {
				Domain currentDomain = domainList.get(i);
				String currentDomainName = currentDomain.getFullName();

				try {
					String url = "http://" + currentDomainName;
					Response response = Jsoup.connect(url)
							.userAgent("Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0")
							.followRedirects(true).execute();
					URL responseUrl = response.url();
					url = "https://" + currentDomainName;

					Website website = DomainUtils.getWebsiteForDomain(currentDomain, responseUrl);

					Document doc = Jsoup.connect(responseUrl.getProtocol() + "://" + responseUrl.getHost()).get();
					Elements metalinks = doc.select("meta[name=generator]");
					if (metalinks != null && !"".equals(metalinks.toString().trim())) {
						System.out.println("Generator: " + metalinks.attr("content"));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}
}