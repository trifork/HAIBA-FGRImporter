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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
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

import dk.nsi.haiba.fgrimporter.status.ImportStatus;
import dk.nsi.haiba.fgrimporter.status.ImportStatusRepository;
import dk.nsi.haiba.fgrimporter.status.TimeSource;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportStatusRepositoryJdbcImplIT {

	private static final String TYPE = "na";

    @Configuration
	@PropertySource("classpath:test.properties")
	@Import(FGRIntegrationTestConfiguration.class)
	static class ContextConfiguration {
		@Bean
		public TimeSource timeSource() {
			return new ProgrammableTimeSource();
		}
	}

	@Autowired
	private ImportStatusRepository statusRepo;

	@Autowired
	@Qualifier("haibaJdbcTemplate")
	private JdbcTemplate haibaJdbcTemplate;

	@Autowired
	private ProgrammableTimeSource timeSource;

	@Test
	public void returnsNoStatusWhenTableIsEmpty() {
		assertNull(statusRepo.getLatestStatus(TYPE));
	}

	@Before
	public void resetTime() {
		timeSource.now = new DateTime();
	}

	@Test
	public void returnsOpenStatusWhenOnlyOneOpenStatusInDb() {
		DateTime startTime = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTime, TYPE);

		ImportStatus latestStatus = statusRepo.getLatestStatus(TYPE);
		assertNotNull(latestStatus);
		assertEquals(startTime, latestStatus.getStartTime());
	}

	@Test
	public void callingEndedAtWithAnEmptyDatabaseDoesNothing() {
		// this can happen in the ParserExecutor, if some exception occurs
		// before we reach the call to importStartedAt
		statusRepo.importEndedWithFailure(new DateTime(), "ErrorMessage", TYPE);
		assertNull(statusRepo.getLatestStatus(TYPE));
	}

	@Test
	public void returnsClosedStatusWhenOnlyOneStatusInDb() {
		ImportStatus expectedStatus = insertStatusInDb(ImportStatus.Outcome.SUCCESS);

		assertEquals(expectedStatus, statusRepo.getLatestStatus(TYPE));
	}

	@Test
	public void returnsSuccesStatusFromDb() {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		assertEquals(ImportStatus.Outcome.SUCCESS, statusRepo.getLatestStatus(TYPE).getOutcome());
		
		assertTrue(statusRepo.getLatestStatus(TYPE).toString().contains("Outcome was SUCCESS"));
	}

	@Test
	public void returnsErrorStatusFromDb() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		assertEquals(ImportStatus.Outcome.FAILURE, statusRepo.getLatestStatus(TYPE).getOutcome());
		assertTrue(statusRepo.getLatestStatus(TYPE).toString().contains("Outcome was FAILURE"));
	}

	@Test
	public void openStatusHasNoOutcome() {
		DateTime startTime = new DateTime();
		statusRepo.importStartedAt(startTime, TYPE);
		assertNull(statusRepo.getLatestStatus(TYPE).getOutcome());
	}
	
	@Test
	public void returnsLatestStatusWhenTwoClosedStatusesExistsInDb()
			throws InterruptedException {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		Thread.sleep(1000); // to avoid the next status having the exact same
							// startTime as the one just inserted
		ImportStatus expectedStatus = insertStatusInDb(ImportStatus.Outcome.FAILURE);

		ImportStatus latestStatus = statusRepo.getLatestStatus(TYPE);
		assertEquals(expectedStatus, latestStatus);
	}

	@Test
	public void whenTwoOpenStatusesExistsInDbEndingOnlyUpdatesTheLatest() throws InterruptedException {
		DateTime startTimeOldest = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTimeOldest, TYPE);
		// The reason for this not being closed would be some kind of program
		// error or outage
		Thread.sleep(1000);

		DateTime startTimeNewest = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTimeNewest, TYPE);

		Thread.sleep(1000);
		statusRepo.importEndedWithFailure(new DateTime().withMillisOfSecond(0), "ErrorMessage", TYPE);

		// check that the newest was closed
		ImportStatus dbStatus = statusRepo.getLatestStatus(TYPE);
		assertEquals(startTimeNewest, dbStatus.getStartTime());
		assertNotNull(dbStatus.getEndTime());

		// check that some open status exists (which we can then conclude must
		// be the oldest of the two test statuses)
		assertEquals(1, haibaJdbcTemplate.queryForInt("SELECT COUNT(*) from FGRImporterStatus WHERE EndTime IS NULL"));
	}

	@Test
	public void jobIsNotOverdueWhenItHasNotRun() {
		assertFalse(statusRepo.isOverdue(TYPE));
	}

	@Test
	public void jobIsNotOverdueWhenItHasJustRunWithSucces() {
		insertStatusInDb(ImportStatus.Outcome.SUCCESS);
		assertFalse(statusRepo.isOverdue(TYPE));
	}

	@Test
	public void jobIsNotOverdueWhenItHasJustRunWithError() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		assertFalse(statusRepo.isOverdue(TYPE));
	}

	@Test
	public void jobIsOverdueWhenItRanMoreDaysAgoThanTheLimit() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(2);
		assertTrue(statusRepo.isOverdue(TYPE));
	}

	@Test
	public void jobIsNotOverdueOneSecondBeforeTheDeadline() {
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(1).minusSeconds(1);
		assertFalse(statusRepo.isOverdue(TYPE));
	}

	@Test
	public void jobIsOverdueOneSecondAfterTheDeadline() {
	    System.out.println("ImportStatusRepositoryJdbcImplIT.jobIsOverdueOneSecondAfterTheDeadline() start");
		insertStatusInDb(ImportStatus.Outcome.FAILURE);
		timeSource.now = (new DateTime()).plusDays(1).plusSeconds(1);
		assertTrue(statusRepo.isOverdue(TYPE));
		System.out.println("ImportStatusRepositoryJdbcImplIT.jobIsOverdueOneSecondAfterTheDeadline() end");
	}

	private ImportStatus insertStatusInDb(ImportStatus.Outcome outcome) {
		DateTime startTime = new DateTime().withMillisOfSecond(0);
		statusRepo.importStartedAt(startTime, TYPE);
		DateTime endTime = new DateTime().withMillisOfSecond(0);

		ImportStatus expectedStatus = new ImportStatus();

		if (outcome == ImportStatus.Outcome.SUCCESS) {
			statusRepo.importEndedWithSuccess(endTime, TYPE);
		} else {
		    System.out.println("ImportStatusRepositoryJdbcImplIT.insertStatusInDb() ERROR");
			statusRepo.importEndedWithFailure(endTime, "ErrorMessage", TYPE);
			expectedStatus.setErrorMessage("ErrorMessage");
		}

		expectedStatus.setStartTime(startTime);
		expectedStatus.setEndTime(endTime);
		expectedStatus.setOutcome(outcome);
		return expectedStatus;
	}
}