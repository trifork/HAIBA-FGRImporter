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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.fgrimporter.dao.CommonDAO;
import dk.nsi.haiba.fgrimporter.dao.SKSDAO;
import dk.nsi.haiba.fgrimporter.exception.DAOException;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.SKSLine;

@Transactional("haibaTransactionManager")
public class GenericSKSLineDAOImpl extends CommonDAO implements SKSDAO<SKSLine> {

    private static Log log = new Log(Logger.getLogger(GenericSKSLineDAOImpl.class));

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Value("${jdbc.haibatableprefix:}")
    String tableprefix;

    @Override
    public void saveEntity(SKSLine sks) throws DAOException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String created = formatter.format(new Date());

            String sql = "INSERT INTO " + tableprefix
                    + "GenericSKS (Code, Text, Type, Created, ValidFrom, ValidTo) VALUES (?, ?, ?, '" + created
                    + "', ?, ?)";

            Object[] args = new Object[] { sks.getCode(), sks.getText(), sks.getType(), sks.getValidFrom(),
                    sks.getValidTo() };

            jdbc.update(sql, args);

            log.debug("** Inserted SKSLine");
        } catch (DataAccessException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void clearTable() throws DAOException {
        try {
            jdbc.update("DELETE FROM " + tableprefix + "GenericSKS");
        } catch (Exception e) {
            throw new DAOException("", e);
        }
    }
}
