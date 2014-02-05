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
package dk.nsi.haiba.fgrimporter.status;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import dk.nsi.haiba.fgrimporter.importer.ImportExecutor;

/*
 * This class is responsible for showing a statuspage, this page contains information about the general health of the application.
 * If it returns HTTP 200, no errors are detected
 * If it returns HTTP 500, an error is detected and must be taken care of before further operation. 
 */
@Controller
@Scope("request")
public class StatusReporter {
    @Autowired
    ImportStatusRepository statusRepo;

    @Autowired
    ImportExecutor importExecutor;

    @Value("${cron.shak.import.job}")
    String shakcron;
    @Value("${cron.sks.import.job}")
    String skscron;
    @Value("${cron.sor.import.job}")
    String sorcron;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/status")
    public ResponseEntity<String> reportStatus() {

        HttpHeaders headers = new HttpHeaders();
        StringBuilder sb = new StringBuilder();
        String body = "OK";
        HttpStatus status = HttpStatus.OK;
        sb.append("</br>------------------</br>");
        sb.append(ImportExecutor.SHAK);
        sb.append("</br>------------------</br>");
        String manual = handleManual(ImportExecutor.SHAK, request);
        HttpStatus shakStatus = buildBody(sb, ImportExecutor.SHAK, manual, shakcron);
        
        sb.append("<br>");
        sb.append("</br>------------------</br>");
        sb.append(ImportExecutor.SKS);
        sb.append("</br>------------------</br>");
        manual = handleManual(ImportExecutor.SKS, request);
        HttpStatus sksStatus = buildBody(sb, ImportExecutor.SKS, manual, skscron);

        sb.append("<br>");
        sb.append("</br>------------------</br>");
        sb.append(ImportExecutor.SOR);
        sb.append("</br>------------------</br>");
        manual = handleManual(ImportExecutor.SOR, request);
        HttpStatus sorStatus = buildBody(sb, ImportExecutor.SOR, manual, sorcron);

        if (shakStatus != HttpStatus.OK) {
            status = shakStatus;
        }
        if (sksStatus != HttpStatus.OK) {
            status = sksStatus;
        }
        if (sorStatus != HttpStatus.OK) {
            status = sorStatus;
        }
        body = sb.toString();

        headers.setContentType(MediaType.TEXT_HTML);

        return new ResponseEntity<String>(body, headers, status);
    }

    private String handleManual(final String type, HttpServletRequest request) {
        String manual = request.getParameter("manual_" + type);
        if (manual == null || manual.trim().length() == 0) {
            // no value set, use default set in the import executor
            manual = "" + importExecutor.isManualOverride(type);
        } else {
            // manual flag is set on the request
            if (manual.equalsIgnoreCase("true")) {
                // flag is true, start the importer in a new thread
                // XXX not a new thread
                importExecutor.setManualOverride(type, true);
                Runnable importer = new Runnable() {
                    public void run() {
                        importExecutor.run(type);
                    }
                };
                importer.run();
            } else {
                importExecutor.setManualOverride(type, false);
            }
        }
        return manual;
    }

    private HttpStatus buildBody(StringBuilder sb, String type, String manual, String cron) {
        HttpStatus returnValue = HttpStatus.OK;
        try {
            if (!statusRepo.isHAIBADBAlive()) {
                sb.append("HAIBA Database is _NOT_ running correctly");
                returnValue = HttpStatus.INTERNAL_SERVER_ERROR;
            } else if (statusRepo.isOverdue(type)) {
                // last run information is applied to body later
                sb.append("Is overdue");
                returnValue = HttpStatus.INTERNAL_SERVER_ERROR;
            } else {
                returnValue = HttpStatus.OK;
            }
        } catch (Exception e) {
            sb.append(e.getMessage());
        }

        sb.append("</br>");
        addLastRunInformation(sb, type);

        sb.append("</br>------------------</br>");

        String url = request.getRequestURL().toString();

        sb.append("<a href=\"" + url + "?manual_"+type+"=true\">Manual start importer</a>");
        sb.append("</br>");
        sb.append("<a href=\"" + url + "?manual_"+type+"=false\">Scheduled start importer</a>");
        sb.append("</br>");
        if ("true".equalsIgnoreCase(manual)) {
            sb.append("status: MANUAL");
        } else {
            // default
            sb.append("status: SCHEDULED - " + cron);
        }
        return returnValue;
    }

    private void addLastRunInformation(StringBuilder body, String type) {
        ImportStatus latestStatus = statusRepo.getLatestStatus(type);
        if (latestStatus == null) {
            body.append("\nLast import: Never run");
        } else {
            body.append("\n" + latestStatus.toString());
        }
    }
}
