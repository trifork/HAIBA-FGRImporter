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

package dk.nsi.haiba.fgrimporter.model;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dk.nsi.haiba.fgrimporter.model.xmlmodel.AddressInformation;
import dk.nsi.haiba.fgrimporter.model.xmlmodel.HealthInstitutionEntity;
import dk.nsi.haiba.fgrimporter.model.xmlmodel.InstitutionOwnerEntity;
import dk.nsi.haiba.fgrimporter.model.xmlmodel.OrganizationalUnitEntity;
import dk.nsi.haiba.fgrimporter.model.xmlmodel.XMLModelMapper;


public class SOREventHandler extends DefaultHandler
{
    private String elementValue;

    private InstitutionOwnerEntity curIOE;
    private HealthInstitutionEntity curHIE;
    private OrganizationalUnitEntity curOUE;

    private SORDataSets dataSets;

    private boolean postalRegionCode = false;

    public SOREventHandler(SORDataSets dataSets) {
        this.dataSets = dataSets;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        elementValue = "";

        if ("InstitutionOwnerEntity".equals(qName)) {
            curIOE = new InstitutionOwnerEntity();
        } else if ("HealthInstitutionEntity".equals(qName)) {
            curHIE = new HealthInstitutionEntity();
            curIOE.addHealthInstitutionEntity(curHIE);
        } else if ("OrganizationalUnitEntity".equals(qName)) {
            if (curOUE == null) {
                // Father is a HealthInstitutionEntity
                curOUE = new OrganizationalUnitEntity(null);
                curHIE.addOrganizationalUnitEntity(curOUE);
                curOUE.setHealthInstitutionEntity(curHIE);
            } else {
                // Father is a OrganizationalUnitEntity
                curOUE = new OrganizationalUnitEntity(curOUE);
            }
            curOUE.setBelongsTo(curHIE);

            // Der er nu også RegionCode i postal address, pga. måden denne parser er implementeret overskriver den der
            // ligger i ean, dette omgåes med dette flag som tjekkes
        } else if ("PostalAddressInformation".equals(qName)) {
            postalRegionCode = true;
        } else if ("EanLocationCodeEntity".equals(qName)) {
            postalRegionCode = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("InstitutionOwnerEntity".equals(qName)) {
            denormalizeAdress(curIOE);
            for (HealthInstitutionEntity institutuinEntity : curIOE.getHealthInstitutionEntity()) {
                if (institutuinEntity.getEntityTypeIdentifier() == 394761003L || institutuinEntity.getEntityTypeIdentifier() == 550671000005100L) {
                    // Lægepraksis og special læger.
                } else if (institutuinEntity.getEntityTypeIdentifier() == 22232009L) {
                    // Sygehus.
                    Sygehus s = XMLModelMapper.toSygehus(institutuinEntity);
                    for (OrganizationalUnitEntity oue : institutuinEntity.getOrganizationalUnitEntities()) {
                        addAfdelinger(oue);
                    }

                    dataSets.getSygehusDS().add(s);
                } else if (institutuinEntity.getEntityTypeIdentifier() == 264372000L) {
                    // Apotek.
                }
            }

            curIOE = null;
        } else if ("HealthInstitutionEntity".equals(qName)) {
            curHIE = null;
        } else if ("OrganizationalUnitEntity".equals(qName)) {
            curOUE = curOUE.getParrent();
        } else {
            try {
                setProperty(stripNS(qName), elementValue);
            } catch (Exception e) {
                throw (new SAXException(e));
            }
        }
    }

    private void addAfdelinger(OrganizationalUnitEntity oue) {
        if (oue.getShakIdentifier() != null) {
            // Ignore all 'SygehusAfdeling' with no SKS
            SygehusAfdeling sa = XMLModelMapper.toSygehusAfdeling(oue);
            dataSets.getSygehusAfdelingDS().add(sa);

            for (OrganizationalUnitEntity soue : oue.getSons()) {
                addAfdelinger(soue);
            }
        }
    }

    private static String stripNS(String qName) {
        return (qName.indexOf(':') != -1) ? qName.substring(qName.indexOf(':') + 1) : qName;
    }

    private static void denormalizeAdress(InstitutionOwnerEntity ioe) {
        for (HealthInstitutionEntity hie : ioe.getHealthInstitutionEntity()) {
            pushdownAdress(ioe, hie);
            for (OrganizationalUnitEntity oue : hie.getOrganizationalUnitEntities()) {
                pushdownAdress(hie, oue);
                for (OrganizationalUnitEntity son : oue.getSons()) {
                    pushdownAdress(oue, son);
                }
            }
        }
    }

    private static void pushdownAdress(AddressInformation parrent, AddressInformation son)
    {
        if (parrent == null || son == null) return;
        if (son.getCountryIdentificationCode() == null) son.setCountryIdentificationCode(parrent.getCountryIdentificationCode());
        if (son.getDistrictName() == null) son.setDistrictName(parrent.getDistrictName());
        if (son.getEmailAddressIdentifier() == null) son.setEmailAddressIdentifier(parrent.getEmailAddressIdentifier());
        if (son.getFaxNumberIdentifier() == null) son.setFaxNumberIdentifier(parrent.getFaxNumberIdentifier());
        if (son.getPostCodeIdentifier() == null) son.setPostCodeIdentifier(parrent.getPostCodeIdentifier());
        if (son.getStreetBuildingIdentifier() == null) son.setStreetBuildingIdentifier(parrent.getStreetBuildingIdentifier());
        if (son.getStreetName() == null) son.setStreetName(parrent.getStreetName());
        if (son.getTelephoneNumberIdentifier() == null) son.setTelephoneNumberIdentifier(parrent.getTelephoneNumberIdentifier());
        if (son.getWebsite() == null) son.setWebsite(parrent.getWebsite());
        if (son.hasEntityInheritanceIndicator() != null && son.hasEntityInheritanceIndicator()) {
            son.setEanLocationCode(parrent.getEanLocationCode());
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        elementValue += new String(chars, start, length);
    }

    private boolean setProperty(String qName, String value) throws Exception {
        boolean found = false;
        Method method = null;
        Object object = null;

        // Ignore region code in postal address
        if (postalRegionCode && "RegionCode".equals(qName)) {
            return false;
        }

        if (curOUE != null) {
            object = curOUE;
        } else if (curHIE != null) {
            object = curHIE;
        } else if (curIOE != null) {
            object = curIOE;
        }

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
                String message = "Unsupported datatype for property " + qName + ", expected datatype was " + param.getCanonicalName();
                throw new Exception(message);
            }
        }
        return found;
    }

    public static Date parseXSDDate(String xmlDate) throws ParseException
    {
        String datePattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(datePattern);

        return df.parse(xmlDate);
    }
}
