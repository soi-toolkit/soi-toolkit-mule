
package org.soitoolkit.commons.mule.log;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	    "id"
	})
@XmlRootElement(name = "JaxbPayload")
public class JaxbPayload {

    @XmlElement(required = true)
    protected String id;
 
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
