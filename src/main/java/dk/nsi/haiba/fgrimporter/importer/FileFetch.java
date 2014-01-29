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
package dk.nsi.haiba.fgrimporter.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileFetch {
    private static final Logger log = Logger.getLogger(FileFetch.class);

    public static boolean fetch(URL url, File destination) {
        boolean returnValue = false;
        log.info("Fetching from " + url + ", writing to " + destination.getAbsolutePath());
        try {
            long time = System.currentTimeMillis();
            int connectionTimeoutMs = 30000;
            int readTimeoutMs = 30000;
            FileUtils.copyURLToFile(url, destination, connectionTimeoutMs, readTimeoutMs);
            log.debug("fetch: done, in " + ((System.currentTimeMillis() - time) / 1000d) + " seconds, "
                    + destination.length() + " bytes");
            returnValue = true;
        } catch (IOException e) {
            log.error("Unable to retrieve from " + url, e);
        }
        return returnValue;
    }

    public static InputStream getInputStreamFromZip(ZipFile z, String fileName) {
        InputStream returnValue = null;
        try {
            Enumeration<? extends ZipEntry> entries = z.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equalsIgnoreCase(fileName)) {
                    returnValue = z.getInputStream(entry);
                    log.debug("getInputStreamFromZip: found " + fileName + ", length " + entry.getSize()
                            + " bytes, dated " + new Date(entry.getTime()));
                    break;
                }
            }
        } catch (ZipException e) {
            log.error("", e);
        } catch (IOException e) {
            log.error("", e);
        }
        return returnValue;
    }
}
