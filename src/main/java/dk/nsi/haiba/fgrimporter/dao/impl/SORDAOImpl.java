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

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dk.nsi.haiba.fgrimporter.dao.SORDAO;
import dk.nsi.haiba.fgrimporter.exception.DAOException;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.Sygehus;
import dk.nsi.haiba.fgrimporter.model.SygehusAfdeling;

public class SORDAOImpl implements SORDAO {
    private static Log log = new Log(Logger.getLogger(SORDAOImpl.class));

    @Autowired
    @Qualifier("classJdbcTemplate")
    JdbcTemplate jdbc;

    @Override
    public void clear() {
        jdbc.update("TRUNCATE TABLE Klass_SOR");
    }

    @Override
    public void saveSygehuseAfdelinger(Collection<SygehusAfdeling> entities) {
        log.debug("storing " + entities.size() + " afdelinger");
        for (SygehusAfdeling sa : entities) {
            Long Sor_ID = sa.getSorNummer();
            String SHAK = sa.getNummer();
            if (Sor_ID != null && SHAK != null) {
                try {
                    String sql = "INSERT INTO Klass_SOR (Sor_ID, SHAK) VALUES (?, ?)";
                    jdbc.update(sql, Sor_ID, SHAK);
                } catch (DataAccessException e) {
                    throw new DAOException(e.getMessage(), e);
                }
            } else {
                log.info("no SHAK or SOR_Id in " + sa);
            }
        }
    }

    @Override
    public void saveSygehuse(Collection<Sygehus> entities) {
        log.debug("storing " + entities.size() + " sygehuse");
        for (Sygehus s : entities) {
            Long Sor_ID = s.getSorNummer();
            String SHAK = s.getNummer();
            if (Sor_ID != null && SHAK != null) {
                try {
                    String sql = "INSERT INTO Klass_SOR (Sor_ID, SHAK) VALUES (?, ?)";
                    jdbc.update(sql, Sor_ID, SHAK);
                } catch (DataAccessException e) {
                    throw new DAOException(e.getMessage(), e);
                }
            } else {
                log.info("no SHAK or SOR_Id in " + s);
            }
        }
    }
}
