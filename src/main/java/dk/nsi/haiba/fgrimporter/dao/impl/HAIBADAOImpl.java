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
package dk.nsi.haiba.fgrimporter.dao.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.fgrimporter.dao.CommonDAO;
import dk.nsi.haiba.fgrimporter.dao.HAIBADAO;
import dk.nsi.haiba.fgrimporter.exception.DAOException;
import dk.nsi.haiba.fgrimporter.importer.Organisation;
import dk.nsi.haiba.fgrimporter.log.Log;

@Transactional("haibaTransactionManager")
public class HAIBADAOImpl extends CommonDAO implements HAIBADAO {

	private static Log log = new Log(Logger.getLogger(HAIBADAOImpl.class));

	@Autowired
	@Qualifier("haibaJdbcTemplate")
	JdbcTemplate jdbc;

	@Override
	public void saveOrganisation(Organisation org) throws DAOException {

		try {

			SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
			String created = formatter.format(new Date()); 
			
			String sql = "INSERT INTO Organisation (Nummer, Navn, Organisationstype, CreatedDate, ModifiedDate, ValidFrom, ValidTo) VALUES (?, ?, ?, '"+created+"', '"+created+"', ?, ?)";

			Object[] args = new Object[] {
				org.getNummer(),
				org.getNavn(),
				org.getOrganisationstype(),
				org.getValidFrom(),
				org.getValidTo()
			};

			jdbc.update(sql, args);
			
			log.debug("** Inserted Organisation");
		} catch (DataAccessException e) {
			throw new DAOException(e.getMessage(), e);
		}
	}
	

	@Override
	public void clearOrganisationTable() throws DAOException {
	    try {
			jdbc.update("DELETE FROM Organisation");
        } catch (Exception e) {
            throw new DAOException("", e);
        }
	}

}
