package de.isento.daimon.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import de.isento.daimon.entities.DomainUtils;

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
			DomainUtils.startTransaction();
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
	            	DomainUtils.commitTransaction();
	            	DomainUtils.clearEntityManager();
	            	DomainUtils.startTransaction();
	            	System.out.println("Domains added: " + domainCounter);
	            }
	        }
			lineReader.close();
			
			DomainUtils.commitTransaction();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
		return 0;
	}
}
