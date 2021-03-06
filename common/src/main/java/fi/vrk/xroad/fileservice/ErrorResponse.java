/**
 * MIT License
 *
 * Copyright (c) 2017 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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

package fi.vrk.xroad.fileservice;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.2.1
 * 2017-12-29T14:14:20.997+02:00
 * Generated source version: 3.2.1
 */

@WebFault(name = "errorResponse", targetNamespace = "http://vrk.fi/xroad/fileservice")
public class ErrorResponse extends Exception {

    private fi.vrk.xroad.fileservice.ErrorResponseType errorResponse;

    public ErrorResponse() {
        super();
    }

    public ErrorResponse(String message) {
        this(message, new ErrorResponseType());
    }

    public ErrorResponse(String message, Throwable cause) {
        this(message, new ErrorResponseType(), cause);
    }

    public ErrorResponse(String message, fi.vrk.xroad.fileservice.ErrorResponseType errorResponse) {
        this(message, errorResponse, null);
    }

    public ErrorResponse(String message, fi.vrk.xroad.fileservice.ErrorResponseType errorResponse, Throwable cause) {
        super(message, cause);
        this.errorResponse = errorResponse;
    }

    public fi.vrk.xroad.fileservice.ErrorResponseType getFaultInfo() {
        return this.errorResponse;
    }
}
