package de.isento.daimon.job;

import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;

public class DomainPermutator extends Thread {

	private static final Logger logger = LogManager.getLogger(DomainPermutator.class);

	private long localPermutations = 0;
	private long addedDomains = 0;

	private long startCounter;
	private long chunkSize;

	private int startK;
	private String tldSuffix;

	private FileWriter outputFile;

	public DomainPermutator(long startCounter, long chunkSize, int k, String tld) {
		this.startCounter = startCounter;
		this.chunkSize = chunkSize;
		this.startK = k;
		this.tldSuffix = DomainUtils.DOM_SEP + tld;
	}

	public void run() {
		logger.info("Started processor " + startCounter);
		System.out.println("Started processor " + startCounter);
		try {
			outputFile = new FileWriter("domains_" + tldSuffix.substring(1) + "_" + startCounter + ".txt");
			char[] allowedDomainChars = "abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray();
			int n = allowedDomainChars.length;
			printAllKLengthRec(allowedDomainChars, "", n, startK, tldSuffix);

//			logger.info("Newly added domains: " + addedDomains + " by processor " + startCounter);
//			logger.info("Finished processor " + startCounter);
			outputFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// The main recursive method
	// to print all possible
	// strings of length k
	void printAllKLengthRec(char[] set, String prefix, int n, int k, String tldSuffix) {
		// Base case: k is 0,
		// print prefix
		if (k == 0) {
			if (startCounter <= localPermutations && localPermutations <= startCounter + chunkSize-1) {
				if (!existsDomain(prefix, tldSuffix)) {
					boolean addDomain = false;
					try {
						Jsoup.connect("http://" + prefix + tldSuffix).followRedirects(false).timeout(50).execute();
						addDomain = true;
					} catch (HttpStatusException e) {
						logger.trace("Http Status Errors: " + prefix + tldSuffix);
						addDomain = true;
					} catch (UnsupportedMimeTypeException e) {
						logger.trace("Unsupported Mimetype: " + prefix + tldSuffix);
						addDomain = true;
					} catch (UnknownHostException e) {
						logger.trace("Unknown Host: " + prefix + tldSuffix);
					} catch (IOException e) {
						addDomain = true;
						if (e instanceof SocketTimeoutException || e instanceof SocketException) {
							logger.trace("Socket Exception for: " + prefix + tldSuffix);
						} else if (e instanceof SSLException) {
							logger.trace("SSL Exception for: " + prefix + tldSuffix);
						} else {
							String message = e.getMessage();
							if (message.contains("Too many redirects occurred trying to load URL")) {
								logger.trace("Too many redirects for: " + prefix + tldSuffix);
							} else {
								logger.error("Checked domain: " + prefix + tldSuffix);
								e.printStackTrace();
							}
						}
					}
					if (addDomain == true) {
						addDomain(prefix + tldSuffix);
					}
				}
			}
			localPermutations++;
			return;
		}

		// One by one add all characters
		// from set and recursively
		// call for k equals to k-1
		for (int i = 0; i < n; ++i) {

			// Next character of input added
			String newPrefix = prefix + set[i];

			// k is decreased, because
			// we have added a new character
			printAllKLengthRec(set, newPrefix, n, k - 1, tldSuffix);
		}
	}

	private synchronized boolean existsDomain(String prefix, String tldSuffix) {
		Domain resDomain = DomainUtils.getDomainByName(prefix + tldSuffix);
		if (resDomain == null) {
			return false;
		} else {
			DomainUtils.remove(resDomain);
			return true;
		}
	}

	private synchronized void addDomain(String fullDomainName) {
		try {
			outputFile.append(fullDomainName+"\n");
			System.out.println("Processor " + startCounter + " added: " + fullDomainName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addedDomains++;
	}

}
