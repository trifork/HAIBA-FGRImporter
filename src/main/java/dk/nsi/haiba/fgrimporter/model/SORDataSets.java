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

import dk.nsi.sdm4.core.domain.CompleteDataset;


public class SORDataSets
{
	private CompleteDataset<Apotek> apotekDS;
	private CompleteDataset<Yder> yderDS;
	private CompleteDataset<Praksis> praksisDS;
	private CompleteDataset<Sygehus> sygehusDS;
	private CompleteDataset<SygehusAfdeling> sygehusAfdelingDS;

	public CompleteDataset<Apotek> getApotekDS()
	{
		return apotekDS;
	}

	public void setApotekDS(CompleteDataset<Apotek> apotekDS)
	{
		this.apotekDS = apotekDS;
	}

	public CompleteDataset<Yder> getYderDS()
	{
		return yderDS;
	}

	public void setYderDS(CompleteDataset<Yder> yderDS)
	{
		this.yderDS = yderDS;
	}

	public CompleteDataset<Praksis> getPraksisDS()
	{
		return praksisDS;
	}

	public void setPraksisDS(CompleteDataset<Praksis> praksisDS)
	{
		this.praksisDS = praksisDS;
	}

	public CompleteDataset<Sygehus> getSygehusDS()
	{
		return sygehusDS;
	}

	public void setSygehusDS(CompleteDataset<Sygehus> sygehusDS)
	{
		this.sygehusDS = sygehusDS;
	}

	public CompleteDataset<SygehusAfdeling> getSygehusAfdelingDS()
	{
		return sygehusAfdelingDS;
	}

	public void setSygehusAfdelingDS(CompleteDataset<SygehusAfdeling> sygehusAfdelingDS)
	{
		this.sygehusAfdelingDS = sygehusAfdelingDS;
	}
}
