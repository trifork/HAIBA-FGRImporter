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

import java.util.Date;


public class OrganizationalUnit extends AddressInformation
{
	private Long sorIdentifier;
	private String entityName;
	private Long unitType;
	private String pharmacyIdentifier;
	private String shakIdentifier;
	private Long specialityCode;
	private String providerIdentifier;
	private Date fromDate;
	private Date toDate;

	public OrganizationalUnit()
	{

	}

	public Long getSorIdentifier()
	{

		return sorIdentifier;
	}

	public void setSorIdentifier(Long sorIdentifier)
	{

		this.sorIdentifier = sorIdentifier;
	}

	public String getEntityName()
	{

		return entityName;
	}

	public void setEntityName(String entityName)
	{

		this.entityName = entityName;
	}

	public Long getUnitType()
	{

		return unitType;
	}

	public void setUnitType(Long unitType)
	{

		this.unitType = unitType;
	}

	public String getPharmacyIdentifier()
	{

		return pharmacyIdentifier;
	}

	public void setPharmacyIdentifier(String pharmacyIdentifier)
	{

		this.pharmacyIdentifier = pharmacyIdentifier;
	}

	public String getShakIdentifier()
	{

		return shakIdentifier;
	}

	public void setShakIdentifier(String shakIdentifier)
	{

		this.shakIdentifier = shakIdentifier;
	}

	public Long getSpecialityCode()
	{

		return specialityCode;
	}

	public void setSpecialityCode(Long specialityCode)
	{

		this.specialityCode = specialityCode;
	}

	public String getProviderIdentifier()
	{

		return providerIdentifier;
	}

	public void setProviderIdentifier(String providerIdentifier)
	{

		this.providerIdentifier = providerIdentifier;
	}

	public Date getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(Date validFrom)
	{
		this.fromDate = validFrom;
	}

	public Date getToDate()
	{
		return toDate;
	}

	public void setToDate(Date toDate)
	{
		this.toDate = toDate;
	}
}
