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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import dk.nsi.haiba.fgrimporter.dao.SKSDAO;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.Organisation;
import dk.nsi.haiba.fgrimporter.model.SKSLine;
import dk.nsi.haiba.fgrimporter.parser.Inbox;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.status.ImportStatusRepository;

/*
 * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
 */
public class ImportExecutor {
    private static Log log = new Log(Logger.getLogger(ImportExecutor.class));

    public static final String SHAK = "shak";
    public static final String SKS = "sks";
    public static final String SOR = "sor";

    private Map<String, Boolean> manualOverrideMap = new HashMap<String, Boolean>();

    @Value("${sor.folderpath}")
    private String sorFolderPath;

    @Autowired
    ImportStatusRepository statusRepo;

    @Autowired
    // XXX wont work - needs an inbox for each parser
    Inbox inbox;

    @Autowired
    Parser sorParser;

    @Autowired
    SKSParser<Organisation> shakParser;

    @Autowired
    SKSDAO<Organisation> shakDao;

    @Autowired
    SKSParser<SKSLine> sksParser;

    @Autowired
    SKSDAO<SKSLine> sksDao;

    @Scheduled(cron = "${cron.sor.import.job}")
    public void runSor() {
        if (!isManualOverride(SOR)) {
            log.debug("Running sor Importer: " + new Date().toString());
            File sorFolder = new File(sorFolderPath);
            sorParser.process(sorFolder, SOR);
        } else {
            log.debug("Sor importer must be started manually");
        }
    }

    @Scheduled(cron = "${cron.shak.import.job}")
    public void runShak() {
        if (!isManualOverride(SHAK)) {
            log.debug("Running shak Importer: " + new Date().toString());
            doProcess(shakDao, shakParser, SHAK);
        } else {
            log.debug("Shak importer must be started manually");
        }
    }

    @Scheduled(cron = "${cron.sks.import.job}")
    public void runSks() {
        if (!isManualOverride("sks")) {
            log.debug("Running sks Importer: " + new Date().toString());
            doProcess(sksDao, sksParser, SKS);
        } else {
            log.debug("Sks importer must be started manually");
        }
    }

    /*
     * Separated into its own method for testing purpose, because testing a scheduled method isn't good
     */
    public <T extends SKSLine> void doProcess(SKSDAO<T> dao, SKSParser<T> parser, String type) {
        // TODO hent filer fra urler

        // Fetch new records from LPR contact table
        try {
            statusRepo.importStartedAt(new DateTime(), type);

            if (!inbox.isLocked()) {
                log.debug(inbox + " for parser is unlocked");

                inbox.update();
                File dataSet = inbox.top();

                if (dataSet != null) {
                    parser.process(dataSet, "TODO");
                    Set<T> entities = parser.getEntities();
                    if (entities != null && !entities.isEmpty()) {
                        dao.clearTable();
                        for (T t : entities) {
                            dao.saveEntity(t);
                        }
                    }

                    // Once the import is complete
                    // we can remove the data set
                    // from the inbox.
                    inbox.advance();

                    statusRepo.importEndedWithSuccess(new DateTime(), type);
                } // if there is no data and no error, we never call store on the log item, which is okay
            } else {
                log.debug(inbox + " for parser is locked");
            }

            statusRepo.importEndedWithSuccess(new DateTime(), type);

        } catch (Exception e) {
            log.error("", e);
            try {
                inbox.lock();
            } catch (RuntimeException lockExc) {
                log.error("Unable to lock " + inbox, lockExc);
            }
            statusRepo.importEndedWithFailure(new DateTime(), e.getMessage(), type);
            throw new RuntimeException("runParserOnInbox  failed", e); // to make sure the transaction rolls back
        }
    }

    public boolean isManualOverride(String id) {
        Boolean manualOverride = manualOverrideMap.get(id);
        return manualOverride != null ? manualOverride : false;
    }

    public void setManualOverride(String id, boolean manualOverride) {
        manualOverrideMap.put(id, manualOverride);
    }
}
