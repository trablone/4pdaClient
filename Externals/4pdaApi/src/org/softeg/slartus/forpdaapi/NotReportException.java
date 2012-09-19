package org.softeg.slartus.forpdaapi;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 21.10.11
 * Time: 8:03
 */
public class NotReportException extends IOException {
    public NotReportException(String message){
        super(message);
    }
}
