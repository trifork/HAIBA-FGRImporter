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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
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

import dk.nsi.haiba.fgrimporter.dao.HAIBADAO;
import dk.nsi.haiba.fgrimporter.dao.impl.HAIBADAOImpl;
import dk.nsi.haiba.fgrimporter.importer.SKSParser;

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
        public HAIBADAO haibaDao() {
            return new HAIBADAOImpl();
        }

    }

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;
    
    @Autowired
    HAIBADAO haibaDao;

	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	@Autowired
	SKSParser importer;
    
	@Before
    public void init() {
    }


	@Test
	public void canImportTheCorrectNumberOfRecords() throws Throwable {
		importer.process(datasetDirWith("data/sks/SHAKCOMPLETE.TXT"), "");
		
		// FIXME: These record counts are only correct iff if duplicate keys are disregarted.
		// This is unfortunate. Keys are currently only considered based their SKSKode.
		// They should be a combination of type + kode + startdato based on the register doc.
		assertEquals(745, jdbc.queryForInt("SELECT COUNT(*) FROM Organisation WHERE Organisationstype = 'Sygehus'"));
		assertEquals(9754, jdbc.queryForInt("SELECT COUNT(*) FROM Organisation WHERE Organisationstype = 'Afdeling'"));
	}
	
	private File datasetDirWith(String filename) throws IOException {
		File datasetDir = tmpDir.newFolder();
		FileUtils.copyFileToDirectory(getFile(filename), datasetDir);
		return datasetDir;
	}

	private File getFile(String filename) {
		return toFile(getClass().getClassLoader().getResource(filename));
	}

}
