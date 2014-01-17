/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.haiba.fgrimporter.importer;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import dk.nsi.haiba.fgrimporter.dao.HAIBADAO;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.parser.Inbox;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.status.ImportStatusRepository;

/*
 * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
 */
public class ImportExecutor {
	
	private static Log log = new Log(Logger.getLogger(ImportExecutor.class));

	private boolean manualOverride;
	
	@Autowired
	HAIBADAO haibaDao;

	@Autowired
	ImportStatusRepository statusRepo;
	
	@Autowired
	Inbox inbox;

	@Autowired
	Parser shakParser;

	@Scheduled(cron = "${cron.import.job}")
	public void run() {
		if(!isManualOverride()) {
			log.debug("Running Importer: " + new Date().toString());
			doProcess();
		} else {
			log.debug("Importer must be started manually");
		}
	}

	/*
	 * Separated into its own method for testing purpose, because testing a scheduled method isn't good
	 */
	public void doProcess() {
	    // TODO hent filer fra urler
	    
		// Fetch new records from LPR contact table
		try {
			statusRepo.importStartedAt(new DateTime());

			if (!inbox.isLocked()) {
				log.debug(inbox + " for parser is unlocked");

				inbox.update();
				File dataSet = inbox.top();

				if (dataSet != null) {

					shakParser.process(dataSet, "TODO");

					// Once the import is complete
					// we can remove the data set
					// from the inbox.
					inbox.advance();

					statusRepo.importEndedWithSuccess(new DateTime());
				} // if there is no data and no error, we never call store on the log item, which is okay
			} else {
				log.debug(inbox + " for parser is locked");
			}
	        
			statusRepo.importEndedWithSuccess(new DateTime());
			
		} catch(Exception e) {
			log.error("", e);
			try {
				inbox.lock();
			} catch (RuntimeException lockExc) {
				log.error("Unable to lock " + inbox, lockExc);
			}
			statusRepo.importEndedWithFailure(new DateTime(), e.getMessage());
			throw new RuntimeException("runParserOnInbox  failed", e); // to make sure the transaction rolls back
		}
	}

	public boolean isManualOverride() {
		return manualOverride;
	}

	public void setManualOverride(boolean manualOverride) {
		this.manualOverride = manualOverride;
	}
	
}
