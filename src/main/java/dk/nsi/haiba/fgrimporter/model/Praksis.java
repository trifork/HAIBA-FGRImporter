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

import dk.nsi.sdm4.core.util.Dates;

import org.apache.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import java.util.Date;

@Entity
public class Praksis
{
	private String navn;
	private Long eanLokationsnummer;
	private Long regionCode;
	private Long sorNummer;
	private Date validFrom;
	private Date validTo;
    private static final Logger logger = Logger.getLogger(Praksis.class);

	public Praksis()
	{
	}

	public String getNavn()
	{
		return navn;
	}

	public void setNavn(String navn)
	{
		this.navn = navn;
	}

	public Long getEanLokationsnummer()
	{
		return eanLokationsnummer;
	}

	public void setEanLokationsnummer(Long eanLokationsnummer)
	{
		this.eanLokationsnummer = eanLokationsnummer;
	}

	public Long getRegionCode()
	{
		return regionCode;
	}

	public void setRegionCode(Long regionCode)
	{
		this.regionCode = regionCode;
	}

	public Long getSorNummer()
	{
		return sorNummer;
	}

	public void setSorNummer(Long sorNummer)
	{
		this.sorNummer = sorNummer;
	}

	public Date getValidFrom()
	{
		return validFrom;
	}

	public void setValidFrom(Date validFrom)
	{
		this.validFrom = validFrom;
	}

	public Date getValidTo()
	{
		return (validTo != null) ? validTo : Dates.THE_END_OF_TIME;
	}

	public void setValidTo(Date validTo)
	{
		this.validTo = validTo;
	}
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sorNummer == null) ? 0 : sorNummer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Praksis other = (Praksis) obj;
        if (sorNummer == null) {
            if (other.sorNummer != null)
                return false;
        } else if (!sorNummer.equals(other.sorNummer))
            return false;
        return true;
    }
}
