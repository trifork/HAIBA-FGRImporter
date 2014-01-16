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
package dk.nsi.haiba.fgrimporter.parser;

import java.io.File;

/**
 * A parser that imports files and stores the data in a database.
 * <p/>
 * Generally parsers should never log anything other than on DEBUG level.
 * If something goes wrong the parser must throw an exception and let the
 * caller do the logging.
 *
 * @author Thomas Børlum <thb@trifork.com>
 */
public interface Parser {

    public final static String SLA_RECORDS_PROCESSED_MAME = "processed_records";
    public final static String SLA_INPUT_NAME = "input";

	/**
	 * Processes a data set and persists the data.
	 * <p/>
	 * Processing consists of four steps:
	 * <p/>
	 * <ol>
	 * <li>Check that all required files are present.</li>
	 * <li>Check that the import sequence is in order.</li>
	 * <li>Parse the data set and persisting it accordingly.</li>
	 * <li>Update the version number, in the key value store.</li>
	 * </ol>
	 * <p/>
	 * You should only log on DEBUG level. See {@linkplain Parser parser}.
	 *
	 * @param dataSet the root directory of the file set. Data files are contained within the directory.
     * @param identifier identifer for this "parse run"
	 * @throws OutOfSequenceException if the data set is out of sequence in the expected order.
	 * @throws ParserException        if anything parser specific error happens or unexpected happens.
	 */
	void process(File dataSet, String identifier) throws ParserException;

	String getHome();
}
