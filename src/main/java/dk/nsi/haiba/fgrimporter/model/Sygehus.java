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

package dk.nsi.haiba.fgrimporter.model;

import dk.nsi.sdm4.core.domain.AbstractStamdataEntity;
import dk.nsi.sdm4.core.util.Dates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Sygehus extends AbstractStamdataEntity
{
	private String navn;
	private Long eanLokationsnummer;
	private String nummer;
	private String telefon;
	private String vejnavn;
	private String postnummer;
	private String bynavn;
	private String email;
	private String www;
	private Long sorNummer;
	private Date validFrom;
	private Date validTo;

	@Column
	public String getNavn()
	{
		return navn;
	}

	public void setNavn(String navn)
	{
		this.navn = navn;
	}

	@Column
	public Long getEanLokationsnummer()
	{
		return eanLokationsnummer;
	}

	public void setEanLokationsnummer(Long eanLokationsnummer)
	{
		this.eanLokationsnummer = eanLokationsnummer;
	}

	@Column
	public String getNummer()
	{
		return nummer;
	}

	public void setNummer(String nummer)
	{
		this.nummer = nummer;
	}

	@Column
	public String getTelefon()
	{
		return telefon;
	}

	public void setTelefon(String telefon)
	{
		this.telefon = telefon;
	}

	@Column
	public String getVejnavn()
	{
		return vejnavn;
	}

	public void setVejnavn(String vejnavn)
	{
		this.vejnavn = vejnavn;
	}

	@Column
	public String getPostnummer()
	{
		return postnummer;
	}

	public void setPostnummer(String postnummer)
	{
		this.postnummer = postnummer;
	}

	@Column
	public String getBynavn()
	{
		return bynavn;
	}

	public void setBynavn(String bynavn)
	{
		this.bynavn = bynavn;
	}

	@Column
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column
	public String getWww()
	{
		return www;
	}

	public void setWww(String www)
	{
		this.www = www;
	}

	@Id
	@Column
	public Long getSorNummer()
	{
		return sorNummer;
	}

	public void setSorNummer(Long sorNummer)
	{
		this.sorNummer = sorNummer;
	}

	@Override
	public Date getValidFrom()
	{
		return validFrom;
	}

	public void setValidFrom(Date validFrom)
	{

		this.validFrom = validFrom;
	}

	@Override
	public Date getValidTo()
	{
		return (validTo != null) ? validTo : Dates.THE_END_OF_TIME;
	}

	public void setValidTo(Date validTo)
	{
		this.validTo = validTo;
	}
}