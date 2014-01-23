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
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Override
    public void clearSygehuse() {
        jdbc.update("TRUNCATE TABLE SORSygehus");
    }

    @Override
    public void clearSygehusAfdelinger() {
        jdbc.update("TRUNCATE TABLE SORSygehusAfdeling");
    }

    @Override
    public void saveSygehuseAfdelinger(Collection<SygehusAfdeling> entities) {
        for (SygehusAfdeling sa : entities) {
            try {
                // @formatter:off
            String sql = ""
                    + "INSERT INTO SORSygehusAfdeling "
                    + "            (SorNummer, "
                    + "             EanLokationsnummer, "
                    + "             Nummer, "
                    + "             Navn, "
                    + "             SygehusSorNummer, "
                    + "             OverAfdelingSorNummer, "
                    + "             UnderlagtSygehusSorNummer, "
                    + "             AfdelingTypeKode, "
                    + "             AfdelingTypeTekst, "
                    + "             HovedSpecialeKode, "
                    + "             HovedSpecialeTekst, "
                    + "             Telefon, "
                    + "             Vejnavn, "
                    + "             Postnummer, "
                    + "             Bynavn, "
                    + "             Email, "
                    + "             Www, "
                    + "             ValidFrom, "
                    + "             ValidTo) "
                    + "VALUES      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
             jdbc.update(sql,
                     sa.getSorNummer(), 
                     sa.getEanLokationsnummer(),
                     sa.getNummer(),
                     sa.getNavn(),
                     sa.getSygehusSorNummer(),
                     sa.getOverAfdelingSorNummer(),
                     sa.getUnderlagtSygehusSorNummer(),
                     sa.getAfdelingTypeKode(),
                     sa.getAfdelingTypeTekst(),
                     sa.getHovedSpecialeKode(),
                     sa.getHovedSpecialeTekst(),
                     sa.getTelefon(),
                     sa.getVejnavn(),
                     sa.getPostnummer(),
                     sa.getBynavn(),
                     sa.getEmail(),
                     sa.getWww(),
                     sa.getValidFrom(),
                     sa.getValidTo()
                     );
             // @formatter:on}
            } catch (DataAccessException e) {
                throw new DAOException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void saveSygehuse(Collection<Sygehus> entities) {
        for (Sygehus sygehus : entities) {
            try {
                // @formatter:off
                String sql = ""
                        + "INSERT INTO SORSygehus "
                        + "            (SorNummer, "
                        + "             EanLokationsnummer, "
                        + "             Nummer, "
                        + "             Telefon, "
                        + "             Navn, "
                        + "             Vejnavn, "
                        + "             Postnummer, "
                        + "             Bynavn, "
                        + "             Email, "
                        + "             Www, "
                        + "             ValidFrom, "
                        + "             ValidTo) "
                        + "VALUES      (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
             jdbc.update(sql,
                     sygehus.getSorNummer(), 
                     sygehus.getEanLokationsnummer(),
                     sygehus.getNummer(),
                     sygehus.getTelefon(),
                     sygehus.getNavn(),
                     sygehus.getVejnavn(),
                     sygehus.getPostnummer(),
                     sygehus.getBynavn(),
                     sygehus.getEmail(),
                     sygehus.getWww(),
                     sygehus.getValidFrom(),
                     sygehus.getValidTo()
                     );
             // @formatter:on}
            } catch (DataAccessException e) {
                throw new DAOException(e.getMessage(), e);
            }
        }
    }
}
