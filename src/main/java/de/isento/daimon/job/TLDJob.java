package de.isento.daimon.job;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.isento.daimon.DaimonBatch;
import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.TLD;

public class TLDJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {
		Document doc;
		try {
			doc = Jsoup.connect("http://www.iana.org/domains/root/db").get();
			
			DomainUtils.startTransaction();

			Elements trs = doc.getElementById("tld-table").getElementsByTag("tr");
			for (Element tr : trs){
				Elements tds = tr.children();
				// Ignore headers and descriptions
				if(tds.get(0).text().startsWith(".")){
					String name = tds.get(0).text().substring(1);
					String type = tds.get(1).text();
					String organisation = tds.get(2).text();
					if(!DomainUtils.getTLDMap().containsKey(name)){
						DomainUtils.addTLD(name, type, organisation);
					}
				}
			}
			
			DomainUtils.commitTransaction();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
