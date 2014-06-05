package de.ecspride.events;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the event information parser class.
 * @author Siegfried Rasthofer
 */
public class EventInformationParser {
	private final String eventInformationFile;
	
	public EventInformationParser(String eventInformationFile){
		this.eventInformationFile = eventInformationFile;
	}
	
	public Map<String, EventInformation> parseEventInformation(){
		Map<String, EventInformation> eventInformation = new HashMap<String, EventInformation>();
		
		try{
			File fXmlFile = new File(eventInformationFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
		 
			NodeList nList = doc.getElementsByTagName("event");
		 
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					
					String methodSignature = eElement.getAttribute("methodSignature");
					String eventName = eElement.getElementsByTagName("eventname").item(0).getAttributes().item(0).getNodeValue();
					String instrumentationPosition = eElement.getElementsByTagName("instrumentationpos").item(0).getTextContent();
					
					EventInformation eventInfo = null;
					if(instrumentationPosition.equals("after"))
						eventInfo = new EventInformation(eventName, true);
					else if(instrumentationPosition.equals("before"))
						eventInfo = new EventInformation(eventName, false);
					else
						throw new RuntimeException("Oops, something went all wonky!!");
					
					if(eElement.getElementsByTagName("data").getLength() != 0){
					Node dataNode = eElement.getElementsByTagName("data").item(0);
					
					if(dataNode.hasChildNodes()){
						NodeList nodeList = dataNode.getChildNodes();
						
						for(int dataNodeIndex = 0; dataNodeIndex < nodeList.getLength(); dataNodeIndex++){
							String paramPosition=null, paramName=null;
							
							if(nodeList.item(dataNodeIndex).hasAttributes()){
								if(nodeList.item(dataNodeIndex).getAttributes().item(0).getNodeName().equals("pos"))
									paramPosition = nodeList.item(dataNodeIndex).getAttributes().item(0).getNodeValue();
								if(nodeList.item(dataNodeIndex).getAttributes().item(1).getNodeName().equals("pos"))
									paramPosition = nodeList.item(dataNodeIndex).getAttributes().item(1).getNodeValue();
								if(nodeList.item(dataNodeIndex).getAttributes().item(0).getNodeName().equals("name"))
									paramName = nodeList.item(dataNodeIndex).getAttributes().item(0).getNodeValue();
								if(nodeList.item(dataNodeIndex).getAttributes().item(1).getNodeName().equals("name"))
									paramName = nodeList.item(dataNodeIndex).getAttributes().item(1).getNodeValue();
								
								try{
									if(paramPosition==null || paramName== null)
										throw new RuntimeException("Ooops, something went all wonky!!");
									
									eventInfo.setParameterInformation(Integer.parseInt(paramPosition), paramName);
								}catch(Exception ex){
									ex.printStackTrace();
									System.exit(0);
								}
							}
						}
					}
				}
					
//				if(eventInfo.getParameterInformation().isEmpty())
//					throw new RuntimeException("Oops, there have to be some data...");
				eventInformation.put(methodSignature, eventInfo);	
					
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
		
		return eventInformation;
	}
}
