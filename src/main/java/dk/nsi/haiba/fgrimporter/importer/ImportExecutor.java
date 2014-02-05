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
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import dk.nsi.haiba.fgrimporter.dao.SKSDAO;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.Organisation;
import dk.nsi.haiba.fgrimporter.model.SKSLine;
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

    @Autowired
    private URL shakRemoteUrl;

    @Autowired
    private URL sorRemoteUrl;

    @Autowired
    private URL sksRemoteUrl;

    @Autowired
    ImportStatusRepository statusRepo;

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

    public void runManual(String type) {
        log.debug("running " + type + " manually");
        setManualOverride(type, true);
        if (SOR.equals(type)) {
            runSor();
        } else if (SKS.equals(type)) {
            runSks();
        } else if (SHAK.equals(type)) {
            runShak();
        } else {
            log.error("not able to run type " + type);
        }
    }

    @Scheduled(cron = "${cron.sor.import.job}")
    public void cronSor() {
        if (!isManualOverride(SOR)) {
            runSor();
        } else {
            log.debug("Sor importer must be started manually");
        }
    }

    public void runSor() {
        log.debug("Running sor Importer: " + new Date().toString());
        statusRepo.importStartedAt(new DateTime(), SOR);
        File destination = resolveDestinationFile(SOR, sorRemoteUrl);
        boolean fetched = FileFetch.fetch(sorRemoteUrl, destination);
        if (fetched) {
            try {
                sorParser.process(destination, SOR);
                statusRepo.importEndedWithSuccess(new DateTime(), SOR);
            } catch (Exception e) {
                statusRepo.importEndedWithFailure(new DateTime(), e.getMessage(), SOR);
            }
        } else {
            statusRepo.importEndedWithFailure(new DateTime(), "file fetch failed from " + sorRemoteUrl, SOR);
        }
    }

    @Scheduled(cron = "${cron.shak.import.job}")
    public void cronShak() {
        if (!isManualOverride(SHAK)) {
            runShak();
        } else {
            log.debug("Shak importer must be started manually");
        }
    }

    public void runShak() {
        log.debug("Running shak Importer: " + new Date().toString());
        doProcess(shakDao, shakParser, SHAK, shakRemoteUrl);
    }

    @Scheduled(cron = "${cron.sks.import.job}")
    public void cronSks() {
        if (!isManualOverride(SKS)) {
            runSks();
        } else {
            log.debug("Sks importer must be started manually");
        }
    }

    public void runSks() {
        log.debug("Running sks Importer: " + new Date().toString());
        doProcess(sksDao, sksParser, SKS, sksRemoteUrl);
    }

    /*
     * Separated into its own method for testing purpose, because testing a scheduled method isn't good
     */
    public <T extends SKSLine> void doProcess(SKSDAO<T> dao, SKSParser<T> parser, String type, URL remoteUrl) {
        // Fetch new records from LPR contact table
        try {
            statusRepo.importStartedAt(new DateTime(), type);
            File destination = resolveDestinationFile(type, remoteUrl);
            boolean fetched = FileFetch.fetch(remoteUrl, destination);

            if (fetched) {
                parser.process(destination, "TODO");
                Set<T> entities = parser.getEntities();
                if (entities != null && !entities.isEmpty()) {
                    dao.clearTable();
                    for (T t : entities) {
                        dao.saveEntity(t);
                    }
                    log.debug("stored " + entities.size() + " for " + type);
                }

                statusRepo.importEndedWithSuccess(new DateTime(), type);
            } else {
                statusRepo.importEndedWithFailure(new DateTime(), "file not fetched from " + remoteUrl, type);
            }
        } catch (Exception e) {
            log.error("error fetching and parsing " + remoteUrl, e);
            statusRepo.importEndedWithFailure(new DateTime(), e.getMessage(), type);
            throw new RuntimeException("runParserOnInbox failed", e); // to make sure the transaction rolls back
        }
    }

    public static File resolveDestinationFile(String type, URL remoteUrl) {
        String simpleFileName = new File(remoteUrl.getFile()).getName();
        return new File(type + File.separator + simpleFileName);
    }

    public boolean isManualOverride(String id) {
        Boolean manualOverride = manualOverrideMap.get(id);
        return manualOverride != null ? manualOverride : false;
    }

    public void setManualOverride(String id, boolean manualOverride) {
        manualOverrideMap.put(id, manualOverride);
    }
}
