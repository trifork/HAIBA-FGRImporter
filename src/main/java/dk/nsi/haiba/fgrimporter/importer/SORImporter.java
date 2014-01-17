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
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import dk.nsi.haiba.fgrimporter.dao.HAIBADAO;
import dk.nsi.haiba.fgrimporter.model.SORDataSets;
import dk.nsi.haiba.fgrimporter.model.SOREventHandler;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.parser.ParserException;
import dk.nsi.sdm4.core.persistence.Persister;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;


/**
 * Parser for the SOR register.
 * <p/>
 * SOR is an acronym for 'Sundhedsvæsenets Organisationsregister'.
 */
public class SORImporter implements Parser {
	private static final Logger logger = Logger.getLogger(SORImporter.class);

	@Autowired
	private SLALogger slaLogger;

    @Autowired
    HAIBADAO haibaDao;

	@SuppressWarnings("unchecked")
	@Override
	public void process(File datadirectory, String identifier) {
		Preconditions.checkNotNull(datadirectory);

        SLALogItem slaLogItem = slaLogger.createLogItem(getHome()+".process", "SDM4."+getHome()+".process");
        slaLogItem.setMessageId(identifier);
        slaLogItem.addCallParameter(Parser.SLA_INPUT_NAME, datadirectory.getAbsolutePath());
		try {
            // Reset transaction time before importing
            persister.resetTransactionTime();
            long processed = 0;
			for (File file : datadirectory.listFiles()) {
				MDC.put("filename", file.getName());

				SORDataSets dataSets = parse(file);
//				persister.persistCompleteDataset(dataSets.getPraksisDS());
//                processed += dataSets.getPraksisDS().size();
//				persister.persistCompleteDataset(dataSets.getYderDS());
//                processed += dataSets.getYderDS().size();
				haibaDao.persistSygehuse(dataSets.getSygehusDS().getEntities());
                processed += dataSets.getSygehusDS().size();
                haibaDao.persistSygehuseAfdelinger(dataSets.getSygehusAfdelingDS().getEntities());
                processed += dataSets.getSygehusAfdelingDS().size();
//				persister.persistCompleteDataset(dataSets.getApotekDS());
//                processed += dataSets.getApotekDS().size();

				MDC.remove("filename");
			}
            slaLogItem.addCallParameter(Parser.SLA_RECORDS_PROCESSED_MAME, ""+processed);
			slaLogItem.setCallResultOk();
			slaLogItem.store();
		} catch (Exception e) {
			slaLogItem.setCallResultError("SORImporter failed - Cause: " + e.getMessage());
			slaLogItem.store();

			throw new ParserException(e);
		}
	}

	public static SORDataSets parse(File file) throws SAXException, ParserConfigurationException, IOException {
		SORDataSets dataSets = new SORDataSets();
		SOREventHandler handler = new SOREventHandler(dataSets);
		SAXParserFactory factory = SAXParserFactory.newInstance();

		SAXParser parser = factory.newSAXParser();

		if (file.getName().toLowerCase().endsWith("xml")) {
			parser.parse(file, handler);
		} else {
			logger.warn("Can only parse files with extension 'xml'! The file is ignored. file=" + file.getAbsolutePath());
		}

		return dataSets;
	}

	@Override
	public String getHome() {
		return "sorimporter";
	}
}
