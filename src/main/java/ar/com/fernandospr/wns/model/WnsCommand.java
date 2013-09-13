package ar.com.fernandospr.wns.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * From <a href="http://msdn.microsoft.com/en-us/library/windows/apps/dn268317.aspx">http://msdn.microsoft.com/en-us/library/windows/apps/dn268317.aspx</a>
 */
@XmlRootElement(name = "command")
public class WnsCommand {
	
	@XmlAttribute
	public String id;
	
	@XmlAttribute
	public Boolean arguments;

}
