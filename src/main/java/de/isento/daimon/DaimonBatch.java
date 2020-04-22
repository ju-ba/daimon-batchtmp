package de.isento.daimon;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import de.isento.daimon.entities.DomainUtils;
import de.isento.daimon.job.AddDomainJob;
import de.isento.daimon.job.DomainByPermutationJob;
import de.isento.daimon.job.ParallelFileExtractorJob;
import de.isento.daimon.job.ScanDomainsForWebsitesJob;
import de.isento.daimon.job.FileByLineJob;
import de.isento.daimon.job.IDaimonJob;
import de.isento.daimon.job.TLDJob;

/**
 * 
 *
 */
public class DaimonBatch {
	
    public static void main( String[] args )
    {
    	setLogLevel();
    	DomainUtils.connectDB();
        
        IDaimonJob job = null;
        if(args.length > 0 && args[0].equals("-tlds")){
    		job = new TLDJob();
    	} else if(args.length > 0 && args[0].equals("-fileByLine")){
    		job = new FileByLineJob();
    	} else if(args.length > 0 && args[0].equals("-addDomains")){
    		job = new AddDomainJob();
    	} else if(args.length > 0 && args[0].equals("-scanDomainsForWebsites")){
    		job = new ScanDomainsForWebsitesJob();
    	} else if(args.length > 0 && args[0].equals("-permutate")){
    		job = new DomainByPermutationJob();
    	} else if(args.length > 0 && args[0].equals("-parallelFileExtract")){
    		job = new ParallelFileExtractorJob();
    	} else {
    		System.out.println("Options:"+"\n\n"
    				+ "-tlds"+ "\n"
    				+ "\n");
    	}
        job.runJob(args);

        DomainUtils.closeDB();
    }
    
    private static void setLogLevel() {
    	LoggerContext context = (LoggerContext) LogManager.getContext(false);
    	Configuration config = context.getConfiguration();
    	LoggerConfig rootConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    	rootConfig.setLevel(Level.INFO);
    	context.updateLoggers();
    }

}
