package ar.com.fernandospr.wns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * From <a href="http://msdn.microsoft.com/en-us/library/windows/apps/dn268318.aspx">http://msdn.microsoft.com/en-us/library/windows/apps/dn268318.aspx</a>
 */
@XmlRootElement(name = "commands")
public class WnsCommands {
	
	@XmlAttribute
	public String scenario;
	
	@XmlElement(name = "command")
	public List<WnsCommand> commands;
}
