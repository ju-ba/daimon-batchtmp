package de.isento.daimon.job;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;
import de.isento.daimon.entities.TLD;

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
		
		EntityManagerHelper.beginTransaction();

		if (domainList.size() > 0) {

			for (int i = 0; i < domainList.size(); i++) {
				Domain currentDomain = domainList.get(i);
				String currentDomainName = currentDomain.getFullName();

				try {
					String url = "http://" + currentDomainName;
					Response response = Jsoup.connect(url)
							.userAgent("Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0")
							.followRedirects(true).execute();
					currentDomain.setHttpCode(response.statusCode());
					URL responseUrl = response.url();

					DomainUtils.getWebsiteForDomain(currentDomain, responseUrl);

					Document doc = Jsoup.connect(responseUrl.getProtocol() + "://" + responseUrl.getHost()).get();
					Elements metalinks = doc.select("meta[name=generator]");
					if (metalinks != null && !"".equals(metalinks.toString().trim())) {
						System.out.println("Generator: " + metalinks.attr("content"));
					}
					
					if (i>0 && i%1000==0) {
						EntityManagerHelper.commit();
						EntityManagerHelper.beginTransaction();
						System.out.println("Scanner " + startPosition + " is at " + i + " of " + batchSize);
					}
				} catch (UnsupportedMimeTypeException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_UNSOPPORTED_MIMETYPE);
				} catch (HttpStatusException e) {
					currentDomain.setHttpCode(e.getStatusCode());
				} catch (SocketTimeoutException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_SOCKET_TIMEOUT);
				} catch (UnknownHostException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_UNKNOWN_HOST);
				} catch (ConnectException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_CONNECT);
				} catch (SSLHandshakeException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_SSL_HANDSHAKE);
				} catch (SSLException e) {
					currentDomain.setHttpCode(DomainUtils.CUSTOM_HTTP_STATUS_SSL);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			EntityManagerHelper.commit();
			EntityManagerHelper.getEntityManager().clear();

		}

	}
}