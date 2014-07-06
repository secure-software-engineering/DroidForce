package de.tum.in.i22.uc.cm.datatypes.basic;

/**
 * This class represents a XML policy. The intuition behind this class
 * was to not pass around strings whenever we talk about an XML policy,
 * in order to get some kind of type safety. With the help of this class,
 * we actually know that we talk about a xml policy (which we wouldn't know when
 * passing around strings).
 * 
 * @author Florian Kelbert
 *
 */
public class XmlPolicy {
	private final String _name;
	private final String _xml;

	public XmlPolicy(String name, String xml) {
		_name = name;
		_xml = xml;
	}

	public String getName() {
		return _name;
	}

	public String getXml() {
		return _xml;
	}
}
