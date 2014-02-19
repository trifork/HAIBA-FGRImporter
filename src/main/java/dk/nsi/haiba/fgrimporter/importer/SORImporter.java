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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.SAXException;

import dk.nsi.haiba.fgrimporter.dao.SORDAO;
import dk.nsi.haiba.fgrimporter.model.SORDataSets;
import dk.nsi.haiba.fgrimporter.model.SOREventHandler;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.parser.ParserException;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;

/**
 * Parser for the SOR register.
 * <p/>
 * SOR is an acronym for 'Sundhedsvæsenets Organisationsregister'.
 */
public class SORImporter implements Parser {
    private static final Logger logger = Logger.getLogger(SORImporter.class);

    @Value("${sor.filenameinziptoparse}")
    private String sorFilenameInZipToParse;

    @Autowired
    private SLALogger slaLogger;

    @Autowired
    SORDAO dao;

    @SuppressWarnings("unchecked")
    @Override
    public void process(File file, String identifier) {
        SLALogItem slaLogItem = slaLogger.createLogItem("sor.process", "SDM4.sor.process");
        slaLogItem.setMessageId(identifier);
        slaLogItem.addCallParameter(Parser.SLA_INPUT_NAME, file.getAbsolutePath());
        try {
            long processed = 0;
            MDC.put("filename", file.getName());
            
            SORDataSets dataSets = parse(file);
            dao.clear();
            dao.saveSygehuse(dataSets.getSygehusDS());
            processed += dataSets.getSygehusDS().size();
            dao.saveSygehuseAfdelinger(dataSets.getSygehusAfdelingDS());
            processed += dataSets.getSygehusAfdelingDS().size();

            MDC.remove("filename");
            slaLogItem.addCallParameter(Parser.SLA_RECORDS_PROCESSED_MAME, "" + processed);
            slaLogItem.setCallResultOk();
            slaLogItem.store();
        } catch (Exception e) {
            slaLogItem.setCallResultError("SORImporter failed - Cause: " + e.getMessage());
            slaLogItem.store();

            throw new ParserException(e);
        }
    }

    public SORDataSets parse(File file) throws SAXException, ParserConfigurationException, IOException {
        SORDataSets returnValue = null;
        if (file.getName().toLowerCase().endsWith(".zip")) {
            ZipFile zipfile = new ZipFile(file);
            try {
                InputStream is = FileFetch.getInputStreamFromZip(zipfile, sorFilenameInZipToParse);
                if (is != null) {
                    returnValue = parse(is);
                } else {
                    logger.warn("not able to get inputstream from zipfile " + file.getAbsolutePath());
                }
            } finally {
                if (zipfile != null) {
                    zipfile.close();
                }
            }
        } else if (sorFilenameInZipToParse.equalsIgnoreCase(file.getName())) {
            returnValue = parse(new FileInputStream(file));
        }
        return returnValue;
    }

    public SORDataSets parse(InputStream is) throws SAXException, ParserConfigurationException, IOException {
        SORDataSets dataSets = new SORDataSets();
        SOREventHandler handler = new SOREventHandler(dataSets);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(is, handler);
        return dataSets;
    }
}
