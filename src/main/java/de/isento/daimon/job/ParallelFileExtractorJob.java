package de.isento.daimon.job;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import de.isento.daimon.entities.Domain;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;

public class ParallelFileExtractorJob implements IDaimonJob {
	 
	private static final Logger logger = LogManager.getLogger(ParallelFileExtractorJob.class);

	@Override
	public int runJob(String[] args) {
		
		int k = Integer.parseInt(args[1]);
		String tld = args[2];
		
		int parallelProcessors = Integer.parseInt(args[3]);
		int batchSize = Integer.parseInt(args[4]);

		EntityManagerHelper.beginTransaction();;
		ArrayList<DomainPermutator> permuatorList = new ArrayList<DomainPermutator>();
		for (Long i=0L; i<parallelProcessors; i++) {
			permuatorList.add(new DomainPermutator(i * batchSize, batchSize, k, tld));
		}
		for (DomainPermutator entry : permuatorList) {
			entry.start();
		}
		for (DomainPermutator entry : permuatorList) {
			try {
				entry.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		EntityManagerHelper.getEntityManager().clear();
		EntityManagerHelper.commit();


		return 0;
	}

	

}
