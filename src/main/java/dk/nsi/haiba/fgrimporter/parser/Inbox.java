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
import java.io.IOException;

/**
 * An inbox is a store where parser input is placed.
 * <p/>
 * An inbox represents a snapshot of the inbox's state. You have to call the
 * {@code update()} method before new elements are considered.
 * <p/>
 * An inbox is essentially a priority queue with slightly different semantics.
 *
 * @author Thomas Børlum <thb@trifork.com>
 */
public interface Inbox {
	/**
	 * Gets the top-most data set in the inbox.
	 * <p/>
	 * The top-most (next) must always be handled before any others to ensure
	 * data integrity.
	 * <p/>
	 * It is on the other hand not the inbox's responsibility to check the
	 * import sequence. This is up to the respective parsers.
	 *
	 * @return the top-most data set from the inbox, or null if non are ready.
	 */
	File top();

	/**
	 * Disposes of the top-most element and moves the next.
	 * <p/>
	 * This should only be called when an element has been successfully
	 * imported.
	 * <p/>
	 * This has no effect if the inbox is empty.
	 *
	 * @throws IllegalStateException thrown if {@link #advance()} called while the inbox is empty.
	 */
	void advance() throws IOException;

	/**
	 * The number of data sets that are ready for handling.
	 * <p/>
	 * This count only shows the number of data sets that are ready for import
	 * from the top down.
	 *
	 * @return the number of elements in the inbox.
	 */
	int readyCount();

	/**
	 * Updates the inbox's state.
	 * <p/>
	 * This should be called before the initial call to {@code top()}.
	 * Subsequent calls will refresh the state including {@link #readyCount},
	 * top-most data set, etc.
	 */
	void update() throws IOException;

	/**
	 * Locks the inbox.
	 */
	void lock();

	/**
	 * Checks if the inbox is locked.
	 */
	boolean isLocked();
}
