package de.isento.daimon.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.entities.EntityManagerHelper;

public class FileByLineJob implements IDaimonJob {

	@Override
	public int runJob(String[] args) {
		Scanner lineReader;
		boolean check = true;
		try {
			lineReader = new Scanner(new File(args[1]));
			if (args.length > 2 && args[2].equals("nocheck")) {
				check = false;
			}
			EntityManagerHelper.beginTransaction();
			int domainCounter = 0;
			while (lineReader.hasNext())
	        {
	            String line = lineReader.nextLine();
//	            if (!check || !DomainUtils.domainExists(line)) {
	            if (line.length() <= 50) {
		            DomainUtils.addDomain(line, false);
		            domainCounter++;
	            } else {
	            	System.out.println("Length >50: " + line);
	            }

	            if(domainCounter % 200000 == 0 && domainCounter > 0) {
	            	EntityManagerHelper.commit();
	            	EntityManagerHelper.getEntityManager().clear();
	            	EntityManagerHelper.beginTransaction();
	            	System.out.println("Domains added: " + domainCounter);
	            }
	        }
			lineReader.close();
			
			EntityManagerHelper.commit();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
		return 0;
	}
}
