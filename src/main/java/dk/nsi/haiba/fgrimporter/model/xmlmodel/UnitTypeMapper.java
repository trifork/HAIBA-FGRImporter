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

import java.util.HashMap;
import java.util.Map;


public class UnitTypeMapper
{
	static Map<Long, String> unitTypeMap;

	private static void init()
	{
		if (unitTypeMap == null)
		{
			unitTypeMap = new HashMap<Long, String>();
			unitTypeMap.put(550811000005108L, "administrativ enhed");
			unitTypeMap.put(550871000005101L, "akut modtage enhed");
			unitTypeMap.put(309964003L, "billeddiagnostisk enhed");
			unitTypeMap.put(2104671000005110L, "ergoterapiklinik");
			unitTypeMap.put(550841000005107L, "forskningsenhed");
			unitTypeMap.put(550861000005106L, "fysioterapi- og ergoterapiklinik");
			unitTypeMap.put(547011000005103L, "fysioterapiklinik");
			unitTypeMap.put(309904001L, "intensiv enhed");
			unitTypeMap.put(550631000005103L, "jordemoderklinik");
			unitTypeMap.put(550851000005109L, "klinisk enhed");
			unitTypeMap.put(726L, "KONVAFD");
			unitTypeMap.put(551611000005102L, "operationsgang");
			unitTypeMap.put(550821000005102L, "service enhed");
			unitTypeMap.put(225728007L, "skadestue");
			unitTypeMap.put(554071000005100L, "sygehusapotek");
			unitTypeMap.put(550831000005104L, "uddannelsesenhed");
		}
	}

	public static Map<Long, String> getMap()
	{
		init();
		return unitTypeMap;
	}

	public static String kodeToString(Long code)
	{
		init();
		return unitTypeMap.get(code);
	}

}
