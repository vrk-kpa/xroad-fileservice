
package fi.vrk.xroad.fileservice;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetResponseType", propOrder = {
    "object"
})
public class GetResponseType {

    @XmlMimeType("application/octet-stream")
    protected DataHandler object;

    public DataHandler getObject() {
        return object;
    }

    public void setObject(DataHandler object) {
        this.object = object;
    }
}
