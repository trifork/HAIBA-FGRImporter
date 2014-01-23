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

package dk.nsi.haiba.fgrimporter.model.xmlmodel;

import dk.nsi.haiba.fgrimporter.model.Sygehus;
import dk.nsi.haiba.fgrimporter.model.SygehusAfdeling;

public class XMLModelMapper {
	public static Sygehus toSygehus(HealthInstitutionEntity hie) {

		Sygehus s = new Sygehus();
		s.setSorNummer(hie.getSorIdentifier());
		s.setNavn(hie.getEntityName());
		s.setEanLokationsnummer(hie.getEanLocationCode());
		s.setNummer(hie.getShakIdentifier());
		s.setVejnavn(hie.getStreetName() + " " + hie.getStreetBuildingIdentifier());
		s.setBynavn(hie.getDistrictName());
		s.setPostnummer(hie.getPostCodeIdentifier());
		s.setEmail(hie.getEmailAddressIdentifier());
		s.setWww(hie.getWebsite());
		s.setTelefon(hie.getTelephoneNumberIdentifier());
		s.setValidFrom(hie.getFromDate());
		s.setValidTo(hie.getToDate());

		return s;
	}

	public static SygehusAfdeling toSygehusAfdeling(OrganizationalUnitEntity oue) {
		SygehusAfdeling sa = new SygehusAfdeling();
		sa.setEanLokationsnummer(oue.getEanLocationCode());
		sa.setSorNummer(oue.getSorIdentifier());
		sa.setNavn(oue.getEntityName());
		sa.setNummer(oue.getShakIdentifier());
		sa.setVejnavn(oue.getStreetName() + " " + oue.getStreetBuildingIdentifier());
		sa.setBynavn(oue.getDistrictName());
		sa.setPostnummer(oue.getPostCodeIdentifier());
		sa.setEmail(oue.getEmailAddressIdentifier());
		sa.setWww(oue.getWebsite());
		sa.setTelefon(oue.getTelephoneNumberIdentifier());
		sa.setAfdelingTypeKode(oue.getEntityTypeIdentifier());
		sa.setAfdelingTypeTekst(UnitTypeMapper.kodeToString(oue.getEntityTypeIdentifier()));
		sa.setHovedSpecialeKode(oue.getSpecialityIdentifier());
		sa.setHovedSpecialeTekst(SpecialityMapper.kodeToString(oue.getSpecialityIdentifier()));
		if (oue.getParrent() != null) {
			// Subdivision of an other 'afdeling'
			sa.setOverAfdelingSorNummer(oue.getParrent().getSorIdentifier());
		} else {
			// Directly under a 'Sygehus'
			sa.setSygehusSorNummer(oue.getHealthInstitutionEntity().getSorIdentifier());
		}
		sa.setUnderlagtSygehusSorNummer(oue.getBelongsTo().getSorIdentifier());
		sa.setValidFrom(oue.getFromDate());
		sa.setValidTo(oue.getToDate());
		return sa;
	}
}
