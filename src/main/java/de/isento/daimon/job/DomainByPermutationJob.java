package de.isento.daimon.job;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;

public class DomainByPermutationJob implements IDaimonJob {

	private long permutationCounter = 0;
	private long knownDomains = 0;
	private long addedDomains = 0;
	 
	private static final Logger logger = LogManager.getLogger(DomainByPermutationJob.class);

	@Override
	public int runJob(String[] args) {
		char[] allowedDomainChars = "abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray();
		int k = Integer.parseInt(args[1]);
		String tldSuffix = DomainUtils.DOM_SEP + args[2];
		
		boolean parallelProcessing = false;
		int parallelProcessors = 0;
		int chunkSize = 0;
		if (args.length == 5) {
			parallelProcessing = true;
			parallelProcessors = Integer.parseInt(args[4]);
			chunkSize = Integer.parseInt(args[5]);
		}

		EntityManagerHelper.beginTransaction();
		printAllKLength(allowedDomainChars, k, tldSuffix);
		EntityManagerHelper.commit();

		logger.info("Checked permutations: " + permutationCounter);
		logger.info("Domains already known: " + knownDomains);
		logger.info("Newly added domains: " + addedDomains);
		System.out.println("Checked permutations: " + permutationCounter);
		System.out.println("Domains already known: " + knownDomains);
		System.out.println("Newly added domains: " + addedDomains);
		return 0;
	}

	void printAllKLength(char[] set, int k, String tldSuffix) {
		int n = set.length;
		printAllKLengthRec(set, "", n, k, tldSuffix);
	}

	// The main recursive method
	// to print all possible
	// strings of length k
	void printAllKLengthRec(char[] set, String prefix, int n, int k, String tldSuffix) {
		// Base case: k is 0,
		// print prefix
		if (k == 0) {
			Domain resDomain = DomainUtils.getDomainByName(prefix + tldSuffix);
			if (resDomain != null) {
				knownDomains++;
			} else {
				boolean addDomain = false;
//				try {
//					Jsoup.connect("http://" + prefix + tldSuffix).followRedirects(false).timeout(50).execute();
//					addDomain = true;
//				} catch (HttpStatusException e) {
//					logger.trace("Http Status Errors: " + prefix + tldSuffix);
//					addDomain = true;
//				} catch (UnsupportedMimeTypeException e) {
//					logger.trace("Unsupported Mimetype: " + prefix + tldSuffix);
//					addDomain = true;
//				} catch (UnknownHostException e) {
//					logger.trace("Unknown Host: " + prefix + tldSuffix);
//				} catch (IOException e) {
//					addDomain = true;
//					if (e instanceof SocketTimeoutException || e instanceof SocketException) {
//						logger.trace("Socket Exception for: " + prefix + tldSuffix);
//					} else if (e instanceof SSLException) {
//						logger.trace("SSL Exception for: " + prefix + tldSuffix);
//					} else {
//						String message = e.getMessage();
//						if (message.contains("Too many redirects occurred trying to load URL")) {
//							logger.trace("Too many redirects for: " + prefix + tldSuffix);
//						} else {
//							logger.error("Checked domain: " + prefix + tldSuffix);
//							e.printStackTrace();
//						}
//					}
//				}
				if (addDomain == true) {
					DomainUtils.addDomain(prefix + tldSuffix, false);
					logger.info("Added domain " + prefix + tldSuffix);
					System.out.println("Added domain " + prefix + tldSuffix);
					addedDomains++;
				}

			}
			permutationCounter++;
			if (permutationCounter % 10000 == 0) {
				EntityManagerHelper.commit();
				EntityManagerHelper.getEntityManager().clear();
				EntityManagerHelper.beginTransaction();
			}
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

}
