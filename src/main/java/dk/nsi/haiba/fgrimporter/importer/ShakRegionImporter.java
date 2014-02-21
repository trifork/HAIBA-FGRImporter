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
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.nsi.haiba.fgrimporter.dao.SHAKRegionDAO;
import dk.nsi.haiba.fgrimporter.model.ShakRegion;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.parser.ParserException;
import dk.sdsd.nsp.slalog.api.SLALogItem;
import dk.sdsd.nsp.slalog.api.SLALogger;

/**
 * Parser for the SOR register.
 * <p/>
 * SOR is an acronym for 'Sundhedsvæsenets Organisationsregister'.
 */
public class ShakRegionImporter implements Parser {
    private static final Logger logger = Logger.getLogger(ShakRegionImporter.class);

    @Autowired
    private SLALogger slaLogger;

    @Autowired
    SHAKRegionDAO dao;

    @Override
    public void process(File file, String identifier) {
        SLALogItem slaLogItem = slaLogger.createLogItem("shakregion.process", "SDM4.shakregion.process");
        slaLogItem.setMessageId(identifier);
        slaLogItem.addCallParameter(Parser.SLA_INPUT_NAME, file.getAbsolutePath());
        try {
            long processed = 0;
            MDC.put("filename", file.getName());

            Collection<ShakRegion> collection = parse(file);
            dao.saveShakRegions(collection);
            processed += collection.size();

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

    public Collection<ShakRegion> parse(File file) throws SAXException, ParserConfigurationException, IOException {
        return parse(new FileInputStream(file));
    }

    public Collection<ShakRegion> parse(InputStream is) throws SAXException, ParserConfigurationException, IOException {
        List<ShakRegion> returnValue = new ArrayList<ShakRegion>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        DefaultHandler handler = new MyShakRegionEventHandler(returnValue);
        parser.parse(is, handler);
        return returnValue;
    }

    public static class MyShakRegionEventHandler extends DefaultHandler {
        static DateFormat DF = new SimpleDateFormat("yyyyMMdd");
        private List<ShakRegion> aList;
        private String aElementValue;
        private ShakRegion aShakRegion;

        public MyShakRegionEventHandler(List<ShakRegion> list) {
            aList = list;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            aElementValue = "";
            if ("SHAKregion".equals(qName)) {
                aShakRegion = new ShakRegion();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("SHAKregion".equals(qName)) {
                // if object is ok, add to list
                if (isShakRegionOk()) {
                    aList.add(aShakRegion);
                } else {
                    logger.error("unable to parse shakregion fully: " + aShakRegion);
                }
                aShakRegion = null;
            } else if (aShakRegion != null) {
                try {
                    setProperty(stripNS(qName), aElementValue);
                } catch (Exception e) {
                    throw (new SAXException(e));
                }
            }
        }

        public boolean isShakRegionOk() {
            return aShakRegion.getDatoFra() != null && aShakRegion.getDatotil() != null
                    && aShakRegion.getEjerforhold() != null && aShakRegion.getInstitutionsart() != null
                    && aShakRegion.getRegionskode() != null && aShakRegion.getSHAKkode() != null;
        }

        private boolean setProperty(String qName, String value) throws Exception {
            boolean found = false;
            Method method = null;

            Object object = aShakRegion;
            if (object != null) {
                // Find den rigtige setter methode
                Class<?> target = object.getClass();
                while (target != null && method == null) {
                    Method methods[] = target.getDeclaredMethods();
                    for (Method prop : methods) {
                        if (prop.getName().equals("set" + qName)) {
                            method = prop;
                            break;
                        }
                    }
                    target = target.getSuperclass();
                }
            }

            if (method != null) {
                // Find ud af hvad type setter metoden forventer og kald med den korrekte parameter
                Class<?> param = method.getParameterTypes()[0];
                if (param.isAssignableFrom(String.class)) {
                    method.invoke(object, value);
                    found = true;
                } else if (param.isAssignableFrom(Long.class)) {
                    Long convValue = Long.parseLong(value);
                    method.invoke(object, convValue);
                    found = true;
                } else if (param.isAssignableFrom(Date.class)) {
                    Date convValue = parseXSDDate(value);
                    method.invoke(object, convValue);
                    found = true;
                } else if (param.isAssignableFrom(Boolean.class)) {
                    Boolean b = Boolean.valueOf(value);
                    method.invoke(object, b);
                } else {
                    String message = "Unsupported datatype for property " + qName + ", expected datatype was "
                            + param.getCanonicalName();
                    throw new Exception(message);
                }
            }
            return found;
        }

        public static Date parseXSDDate(String xmlDate) throws ParseException {
            return DF.parse(xmlDate);
        }

        private static String stripNS(String qName) {
            return (qName.indexOf(':') != -1) ? qName.substring(qName.indexOf(':') + 1) : qName;
        }

        @Override
        public void characters(char[] chars, int start, int length) {
            aElementValue += new String(chars, start, length);
        }
    }
}
