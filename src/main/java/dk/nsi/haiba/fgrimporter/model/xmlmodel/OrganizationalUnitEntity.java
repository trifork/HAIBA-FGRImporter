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

import java.util.ArrayList;
import java.util.List;


public class OrganizationalUnitEntity extends OrganizationalUnit
{
	private HealthInstitutionEntity healthInstitutionEntity;
	private HealthInstitutionEntity belongsTo;
	private OrganizationalUnitEntity parrent;
	private List<OrganizationalUnitEntity> sons;

	public OrganizationalUnitEntity(OrganizationalUnitEntity parrent)
	{
		super();
		sons = new ArrayList<OrganizationalUnitEntity>();
		setParrent(parrent);
	}

	public OrganizationalUnitEntity getParrent()
	{
		return parrent;
	}

	public void setParrent(OrganizationalUnitEntity parrent)
	{
		if (parrent != null) parrent.setSon(this);
		this.parrent = parrent;
	}

	public HealthInstitutionEntity getHealthInstitutionEntity()
	{
		return healthInstitutionEntity;
	}

	public void setHealthInstitutionEntity(HealthInstitutionEntity healthInstitutionEntity)
	{
		this.healthInstitutionEntity = healthInstitutionEntity;
	}

	public HealthInstitutionEntity getBelongsTo()
	{
		return belongsTo;
	}

	public void setBelongsTo(HealthInstitutionEntity belongsTo)
	{
		this.belongsTo = belongsTo;
	}

	public void setSons(List<OrganizationalUnitEntity> sons)
	{
		this.sons = sons;
	}

	public List<OrganizationalUnitEntity> getSons()
	{
		return sons;
	}

	public void setSon(OrganizationalUnitEntity son)
	{
		sons.add(son);
	}
}
