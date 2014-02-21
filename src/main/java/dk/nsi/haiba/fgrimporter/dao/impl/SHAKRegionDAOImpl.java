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
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import dk.nsi.haiba.fgrimporter.dao.CommonDAO;
import dk.nsi.haiba.fgrimporter.dao.SHAKRegionDAO;
import dk.nsi.haiba.fgrimporter.exception.DAOException;
import dk.nsi.haiba.fgrimporter.log.Log;
import dk.nsi.haiba.fgrimporter.model.ShakRegion;

public class SHAKRegionDAOImpl extends CommonDAO implements SHAKRegionDAO {
    private static Log log = new Log(Logger.getLogger(SHAKRegionDAOImpl.class));

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Value("${jdbc.haibatableprefix:}")
    String tableprefix;

    @Override
    public void saveShakRegions(Collection<ShakRegion> shakRegions) throws DAOException {
        log.debug("saveShakRegions: testing with " + shakRegions.size() + " ShakRegions");
        for (ShakRegion shakRegion : shakRegions) {
            try {
                String sql = "UPDATE "
                        + tableprefix
                        + "klass_shak SET Ejerforhold=?, Institutionsart=?, Regionskode=? WHERE Nummer = ? AND ValidFrom = ? AND ValidTo = ?";

                Date datotil = shakRegion.getDatotil();
                // add another day as in klass_shak
                DateTime dt = new DateTime(datotil.getTime()).plusDays(1);
                datotil = dt.toDate();
                Object[] args = new Object[] { shakRegion.getEjerforhold(), shakRegion.getInstitutionsart(),
                        shakRegion.getRegionskode(), shakRegion.getSHAKkode(), shakRegion.getDatoFra(), datotil };

                int update = jdbc.update(sql, args);
                log.trace("update=" + update + " for " + shakRegion);
            } catch (DataAccessException e) {
                log.error("not able to insert shakRegion " + shakRegion, e);
                throw new DAOException(e.getMessage(), e);
            }
        }
    }
}
