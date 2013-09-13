package ar.com.fernandospr.wns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * From <a href="http://msdn.microsoft.com/en-us/library/windows/apps/br230847.aspx">http://msdn.microsoft.com/en-us/library/windows/apps/br230847.aspx</a>
 */
@XmlRootElement(name = "visual")
public class WnsVisual {
	
	@XmlAttribute
	public Integer version;
	
	@XmlAttribute
	public String lang;
	
	@XmlAttribute
	public String baseUri;
	
	@XmlAttribute
	public String branding;
	
	@XmlAttribute
	public Boolean addImageQuery;
	
	/** @brief only relevant for tile, undefined for toast */
	@XmlAttribute
	public String contentId;

	@XmlElement(name = "binding")
	public List<WnsBinding> bindings; 
}
