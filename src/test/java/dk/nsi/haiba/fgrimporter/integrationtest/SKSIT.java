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
package dk.nsi.haiba.fgrimporter.integrationtest;

import static org.apache.commons.io.FileUtils.toFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.fgrimporter.dao.SKSDAO;
import dk.nsi.haiba.fgrimporter.dao.impl.GenericSKSLineDAOImpl;
import dk.nsi.haiba.fgrimporter.dao.impl.SHAKDAOImpl;
import dk.nsi.haiba.fgrimporter.importer.SKSParser;
import dk.nsi.haiba.fgrimporter.model.Organisation;
import dk.nsi.haiba.fgrimporter.model.SKSLine;

/*
 * Tests the HAIBADAO class
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class SKSIT {

    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(FGRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Bean
        public SKSDAO<Organisation> shakdao() {
            return new SHAKDAOImpl();
        }

        @Bean
        public SKSDAO<SKSLine> sksdao() {
            return new GenericSKSLineDAOImpl();
        }
    }

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Autowired
    SKSDAO<Organisation> shakDao;

    @Autowired
    SKSDAO<SKSLine> sksDao;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Autowired
    SKSParser<Organisation> shakParser;

    @Autowired
    SKSParser<SKSLine> sksParser;

    @Before
    public void init() throws IOException {
    }

    @Test
    public void canImportTheCorrectNumberOfRecords() throws Throwable {
        process(shakParser, shakDao, "data/sks/SHAKCOMPLETE.TXT");
        // FIXME: These record counts are only correct iff if duplicate keys are disregarted.
        // This is unfortunate. Keys are currently only considered based their SKSKode.
        // They should be a combination of type + kode + startdato based on the register doc.
        assertEquals(745, jdbc.queryForInt("SELECT COUNT(*) FROM klass_shak WHERE Organisationstype = 'Sygehus'"));
        assertEquals(9754, jdbc.queryForInt("SELECT COUNT(*) FROM klass_shak WHERE Organisationstype = 'Afdeling'"));

        process(sksParser, sksDao, "data/sks/SKScomplete.txt");
        assertEquals(573, jdbc.queryForInt("SELECT COUNT(*) FROM klass_sks WHERE Type = 'und'"));
        assertEquals(8930, jdbc.queryForInt("SELECT COUNT(*) FROM klass_sks WHERE Type = 'pro'"));
        assertEquals(42222, jdbc.queryForInt("SELECT COUNT(*) FROM klass_sks WHERE Type = 'dia'"));
        assertEquals(19955, jdbc.queryForInt("SELECT COUNT(*) FROM klass_sks WHERE Type = 'opr'"));
    }

    @Test
    public void validToAndFromInclusive() throws IOException, ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Rigshospitalet have the following date specified in input:
        // Valid from inclusive = 19760401
        // Valid to inclusive = 25000101
        process(shakParser, shakDao, "data/sks/SHAKCOMPLETE.TXT");
        process(sksParser, sksDao, "data/sks/SKScomplete.txt");

        Date validTo = jdbc.queryForObject("SELECT ValidTo FROM klass_shak WHERE Navn='Rigshospitalet'", Date.class);
        Date validFrom = jdbc.queryForObject("SELECT ValidFrom FROM klass_shak WHERE Navn='Rigshospitalet'",
                Date.class);

        Date lastValidTo = formatter.parse("2500-01-01 23:59:58"); // 2500-01-02 00:00:00.0
        Date firstInvalidTo = formatter.parse("2500-01-02 00:00:01");
        assertTrue(validTo.after(lastValidTo));
        assertTrue(validTo.before(firstInvalidTo));

        Date firstValidFrom = formatter.parse("1976-04-01 00:00:01");
        Date lastInvalidBefore = formatter.parse("1976-03-31 23:59:58");
        assertTrue(validFrom.after(lastInvalidBefore));
        assertTrue(validFrom.before(firstValidFrom));

        validTo = jdbc.queryForObject("SELECT ValidTo FROM klass_sks WHERE Text='Selvmordsforsøg med anden metode før patientkontakt'", Date.class);
        validFrom = jdbc.queryForObject("SELECT ValidFrom FROM klass_sks WHERE Text='Selvmordsforsøg med anden metode før patientkontakt'",
                Date.class);
        // from 201201012 to 25000101
        assertEquals(new Date(validFrom.getTime()), new Date(formatter.parse("2012-01-01 00:00:00").getTime()));
        assertEquals(new Date(validTo.getTime()), new Date(formatter.parse("2500-01-02 00:00:00").getTime()));
    }

    private <T extends SKSLine> void process(SKSParser<T> parser, SKSDAO<T> dao, String string) throws IOException {
        parser.process(toFile(getClass().getClassLoader().getResource(string)), "");
        Set<T> entities = parser.getEntities();
        if (entities != null && !entities.isEmpty()) {
            dao.clearTable();
            for (T t : entities) {
                dao.saveEntity(t);
            }
        }
//        
//        Map<String, List<T>> map = new HashMap<String, List<T>>(); 
//        for (T t : entities) {
//            List<T> list = map.get(t.getType());
//            if (list == null) {
//                list = new ArrayList<T>();
//                map.put(t.getType(), list);
//            }
//            list.add(t);
//        }
//        
//        Set<String> keySet = map.keySet();
//        for (String key : keySet) {
//            System.out.println("got "+map.get(key).size()+" of '"+key+"'");
//        }
    }
}
