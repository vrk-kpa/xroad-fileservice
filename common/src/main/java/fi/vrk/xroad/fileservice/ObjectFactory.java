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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.vrk.xroad.fileservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Get_QNAME = new QName("http://vrk.fi/xroad/fileservice", "get");
    private final static QName _GetResponse_QNAME = new QName("http://vrk.fi/xroad/fileservice", "getResponse");
    private final static QName _Put_QNAME = new QName("http://vrk.fi/xroad/fileservice", "put");
    private final static QName _PutResponse_QNAME = new QName("http://vrk.fi/xroad/fileservice", "putResponse");
    private final static QName _ListResponse_QNAME = new QName("http://vrk.fi/xroad/fileservice", "listResponse");
    private final static QName _ErrorResponse_QNAME = new QName("http://vrk.fi/xroad/fileservice", "errorResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.vrk.xroad.fileservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetRequestType }
     * 
     */
    public GetRequestType createGetRequestType() {
        return new GetRequestType();
    }

    /**
     * Create an instance of {@link GetResponseType }
     * 
     */
    public GetResponseType createGetResponseType() {
        return new GetResponseType();
    }

    /**
     * Create an instance of {@link PutRequestType }
     * 
     */
    public PutRequestType createPutRequestType() {
        return new PutRequestType();
    }

    /**
     * Create an instance of {@link PutResponseType }
     * 
     */
    public PutResponseType createPutResponseType() {
        return new PutResponseType();
    }

    /**
     * Create an instance of {@link List }
     * 
     */
    public List createList() {
        return new List();
    }

    /**
     * Create an instance of {@link ListResponseType }
     * 
     */
    public ListResponseType createListResponseType() {
        return new ListResponseType();
    }

    /**
     * Create an instance of {@link ErrorResponseType }
     * 
     */
    public ErrorResponseType createErrorResponseType() {
        return new ErrorResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "get")
    public JAXBElement<GetRequestType> createGet(GetRequestType value) {
        return new JAXBElement<GetRequestType>(_Get_QNAME, GetRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "getResponse")
    public JAXBElement<GetResponseType> createGetResponse(GetResponseType value) {
        return new JAXBElement<GetResponseType>(_GetResponse_QNAME, GetResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PutRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "put")
    public JAXBElement<PutRequestType> createPut(PutRequestType value) {
        return new JAXBElement<PutRequestType>(_Put_QNAME, PutRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PutResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "putResponse")
    public JAXBElement<PutResponseType> createPutResponse(PutResponseType value) {
        return new JAXBElement<PutResponseType>(_PutResponse_QNAME, PutResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "listResponse")
    public JAXBElement<ListResponseType> createListResponse(ListResponseType value) {
        return new JAXBElement<ListResponseType>(_ListResponse_QNAME, ListResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://vrk.fi/xroad/fileservice", name = "errorResponse")
    public JAXBElement<ErrorResponseType> createErrorResponse(ErrorResponseType value) {
        return new JAXBElement<ErrorResponseType>(_ErrorResponse_QNAME, ErrorResponseType.class, null, value);
    }

}
