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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.SKSLine;
import dk.nsi.haiba.fgrimporter.parser.Parser;
import dk.nsi.haiba.fgrimporter.parser.ParserException;
import dk.nsi.haiba.fgrimporter.util.Util;

/**
 * Parser for the SKS register.
 * <p/>
 * SKS is an acronym for 'Sundhedsvæsenets Klassifikationssystem'. The input file is documented in the sks.pdf file in
 * the doc directory.
 * <p/>
 * Ændringer til SKS-registeret (sgh/afd) indlæses via deltafiler. Ved etablering af registeret anvendes en deltafil der
 * indeholder samtlige sgh/afd dvs. indlæsningen foretages på præcis samme måde hvadenten der indlæses/opdateres et fuld
 * register eller blot ændringer siden sidst (delta)
 * <p/>
 * Eksempel på deltafil-indhold:
 * <p/>
 * afd1301011 197901011979010119821231ANÆSTHESIAFD. AN 084 afd1301011 198301011983010119941231ANÆSTESIAFD.
 * AN,ANÆSTESIAFSNIT 084 afd1301011 199501011999112419991231ANÆSTESIAFD. AN,ANÆSTESIAFSNIT 084 SKS 3 afd1301011
 * 200001012004102920031231Anæstesiologisk klinik AN, anæstesiafsnit 084 SKS 3 afd1301011
 * 200401012004102925000101Anæstesi-/operationsklinik, ABD 084 SKS 1
 * <p/>
 * Hver række angiver en sgh/afd med nummer, gyldighedperiode, navn samt operationskode (3=opdatering, 1=ny eller
 * 2=sletning) Der anvendes fastpositionering dvs. værdierne er altid placeret på samme position og der anvendes
 * whitespaces til at "fylde" ud med
 * <p/>
 * Der er intet krav om at rækkefølgen for hvert nummer skal være kronologisk dvs. der tages højde for at der efter at
 * være indlæst en sgh/afd med gyldighedsperiode
 * <p/>
 * 01.01.2008 - 01.01.2500
 * <p/>
 * kan optræde en anden record for samme nummer med gyldighedsperiode
 * <p/>
 * 01.01.2000 - 31.13.2007
 * <p/>
 * Det garanteres dog at der ikke optræder overlap på gyldighedsperioden for samme nummer.
 * <p/>
 * Operationskoden (action) (position 187-188) angiver om recorden skal betragtes som ny, opdatering eller sletning. Med
 * den måde hvorpå SKS-registeret anvendes i PEM gælder det at alle entries/versioner af hvert nummer skal være placeret
 * i Organisationshistorik-tabellen (og altså ikke kun gamle versioner i denne tabel) dvs. det er altid muligt heri at
 * finde den gyldige/aktive sgh/afd for en bestemt dato. I Organisations-tabellen derimod placeres kun den nyeste record
 * for en given sgh/afd dvs. recorden med nyeste gyldighedsdato. For at sikre denne versionering skal enhver entry (med
 * operationskode 1 eller 3) altid skal indsættes/opdateres i Organisationshistorik-tabellen, mens kun nyeste entry (med
 * operationskode 1 eller 3) skal indsættes/opdateres i Organisations-tabellen. Det gælder at operationskode/action
 * (1,2,3) kun er angivet for entries nyere end 1995. Da vi kun ønsker at indlæse records nyere end 1995 ignoreres alle
 * records hvor operationskode ikke er angivet.
 */
public class SKSParser<T extends SKSLine> implements Parser {
    private static Log log = new Log(Logger.getLogger(SKSParser.class));

    private static final int SKS_CODE_START_INDEX = 3;
    private static final int SKS_CODE_END_INDEX = 23;

    private static final int CODE_TEXT_START_INDEX = 47;

    /**
     * The field is actually 120 characters long. But the specification says only to use the first 60.
     */
    private static final int CODE_TEXT_END_INDEX = 107;

    private static final int ENTRY_TYPE_START_INDEX = 0;
    private static final int ENTRY_TYPE_END_INDEX = 3;

    private static final char OPERATION_CODE_NONE = ' ';
    private static final char OPERATION_CODE_CREATE = '1';
    private static final char OPERATION_CODE_UPDATE = '3';

    private static final int OPERATION_CODE_INDEX = 187;

    private static final String FILE_ENCODING = "ISO8859-15";
    private Set<T> entities;
    private Set<String> parsablePrefixes;

    private Class<T> sksClass;

    public SKSParser(Class<T> clazz, String[] parsablePrefixes) {
        sksClass = clazz;
        this.parsablePrefixes = new HashSet<String>(Arrays.asList(parsablePrefixes));
    }

    public Set<T> getEntities() {
        return entities;
    }

    @Override
    public void process(File file, String identifier) {
        entities = new HashSet<T>();

        try {
            LineIterator lines = null;
            try {
                lines = FileUtils.lineIterator(file, FILE_ENCODING);

                innerParse(lines);

                log.debug("Processed " + entities.size() + " lines");

            } catch (IOException e) {
                throw new ParserException(e);
            } catch (Exception e) {
                // the persister throws these. Let's make them unchecked from here on at least
                throw new ParserException(e);
            } finally {
                LineIterator.closeQuietly(lines);
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private void innerParse(Iterator<String> lines) {
        while (lines.hasNext()) {
            try {
                T t = parseLine(lines.next());
                if (t != null) {
                    entities.add(t);
                }
            } catch (InstantiationException e) {
                log.error("", e);
            } catch (IllegalAccessException e) {
                log.error("", e);
            }
        }
    }

    private T parseLine(String line) throws InstantiationException, IllegalAccessException {
        // Determine the record type.
        //
        String recordType = line.substring(ENTRY_TYPE_START_INDEX, ENTRY_TYPE_END_INDEX);

        if (!parsable(recordType)) {
            log.debug("Ignored record type. line=" + line);
            return null;
        }

        // Since the old record types do not have a operation code (and we are not
        // interested in those records) we can ignore the line.
        //
        if (line.length() < OPERATION_CODE_INDEX + 1) {
            return null;
        }

        // Determine the operation code.
        //
        char code = line.charAt(OPERATION_CODE_INDEX);

        if (code == OPERATION_CODE_CREATE || code == OPERATION_CODE_UPDATE) {
            // Create and update are handled the same way.

            T t = sksClass.newInstance();
            t.setType(recordType);
            t.setCode(line.substring(SKS_CODE_START_INDEX, SKS_CODE_END_INDEX).trim());

            t.setValidFrom(Util.parseValidFrom(line));
            t.setValidTo(Util.parseValidTo(line));

            t.setText(line.substring(CODE_TEXT_START_INDEX, CODE_TEXT_END_INDEX).trim());

            return t;
        } else if (code == OPERATION_CODE_NONE) {
            return null;
        } else {
            throw new ParserException("SKS parser encountered an unknown operation code in line " + line + ". code="
                    + code);
        }
    }

    private boolean parsable(String recordType) {
        boolean returnValue = false;
        for (String parsableType : parsablePrefixes) {
            if (parsableType.equalsIgnoreCase(recordType)) {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }
}
