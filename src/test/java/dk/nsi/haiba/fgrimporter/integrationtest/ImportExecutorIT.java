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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
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
import org.xml.sax.SAXException;

import dk.nsi.haiba.fgrimporter.dao.SKSDAO;
import dk.nsi.haiba.fgrimporter.dao.SORDAO;
import dk.nsi.haiba.fgrimporter.dao.impl.GenericSKSLineDAOImpl;
import dk.nsi.haiba.fgrimporter.dao.impl.SHAKDAOImpl;
import dk.nsi.haiba.fgrimporter.dao.impl.SHAKRegionDAOImpl;
import dk.nsi.haiba.fgrimporter.dao.impl.SORDAOImpl;
import dk.nsi.haiba.fgrimporter.importer.FileFetch;
import dk.nsi.haiba.fgrimporter.importer.ImportExecutor;
import dk.nsi.haiba.fgrimporter.importer.ShakRegionImporter;
import dk.nsi.haiba.fgrimporter.model.Organisation;
import dk.nsi.haiba.fgrimporter.model.SKSLine;
import dk.nsi.haiba.fgrimporter.model.ShakRegion;

/*
 * Tests the HAIBADAO class
 * Spring transaction ensures rollback after test is finished
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("classTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportExecutorIT {
    @Configuration
    @PropertySource("classpath:test.properties")
    @Import(FGRIntegrationTestConfiguration.class)
    static class ContextConfiguration {
        @Bean
        public SORDAO sorDao() {
            return new SORDAOImpl();
        }

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
    @Qualifier("classJdbcTemplate")
    JdbcTemplate jdbc;

    @Autowired
    SORDAO dao;

    @Autowired
    ImportExecutor importExecutor;

    @Before
    public void init() {
        Logger.getLogger(FileFetch.class).setLevel(Level.DEBUG);
        Logger.getLogger(SORDAOImpl.class).setLevel(Level.DEBUG);
        Logger.getLogger(ImportExecutor.class).setLevel(Level.DEBUG);
        Logger.getLogger(SHAKRegionDAOImpl.class).setLevel(Level.DEBUG);
    }

    @Test
    public void canParseShakRegion() throws Throwable {
        ShakRegionImporter i = new ShakRegionImporter();
        File file = new File("shakregion/SHAKregion.xml");
        Collection<ShakRegion> parse = i.parse(file);
        assertTrue(!parse.isEmpty());
        ShakRegion next = parse.iterator().next();
        assertNotNull(next);
        assertNotNull(next.getEjerforhold());
        assertNotNull(next.getSHAKkode());
    }

    @Test
    public void canImportShakFromRealUrls() throws Throwable {
        doImport(ImportExecutor.SHAK);
    }

    @Test
    public void canImportSksFromRealUrls() throws Throwable {
        doImport(ImportExecutor.SKS);
    }

    @Test
    public void canImportSorFromRealUrls() throws Throwable {
        doImport(ImportExecutor.SOR);
    }

    public void doImport(String type) {
        long time = System.currentTimeMillis();
        System.out.println("running " + type);
        importExecutor.runManual(type);
        System.out.println(type + " done, took " + ((System.currentTimeMillis() - time) / 1000d) + " seconds");
    }

}
