package fi.vrk.xroad.fileservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * This class was generated by Apache CXF 3.1.4
 * 2017-03-06T11:53:43.629+02:00
 * Generated source version: 3.1.4
 */
@WebService(targetNamespace = "http://vrk.fi/xroad/fileservice", name = "XroadFileService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
//@SchemaValidation(type = SchemaValidation.SchemaValidationType.REQUEST)
public interface XroadFileService {

    @WebMethod
    @WebResult(name = "getResponse", targetNamespace = "http://vrk.fi/xroad/fileservice", partName = "payload")
    GetResponseType get(
            @WebParam(partName = "parameters", name = "get", targetNamespace = "http://vrk.fi/xroad/fileservice")
                    GetRequestType parameters
    ) throws ErrorResponse;
}
