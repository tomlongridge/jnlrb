package linegenerator.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import linegenerator.core.exceptions.InvalidPlaceNotationException;
import linegenerator.core.exceptions.MethodDefinitionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLMethodLibraryReader extends MethodLibraryReader {

	@Override
	public Method[] read(File pr_File) throws MethodDefinitionException,
			IOException {

		Document doc;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(pr_File);
			doc.getDocumentElement().normalize();
		} catch (SAXException | ParserConfigurationException e) {
			errorFound("Unable to parse method XML file: " + e.getLocalizedMessage());
			return null;
		}
		
		final HashMap<Stage, ArrayList<Method>> methods = new HashMap<>();
		
		final NodeList nodes = doc.getElementsByTagName("methodSet");
		for (int i = 0; i < nodes.getLength(); i++) {

			NodeList methodSet = nodes.item(i).getChildNodes();
			Stage stage = null;
			MethodType collectionMethodType = null;
			
			for (int j = 0; j < methodSet.getLength(); j++) {
				
				Node methodSetNode = methodSet.item(j);
				
				if (methodSetNode.getNodeName().equals("properties")) {

					NodeList properties = methodSetNode.getChildNodes();
					
					for (int l = 0; l < properties.getLength(); l++) {
						if (properties.item(l).getNodeName().equals("stage")) {
							stage = Stage.getStage(properties.item(l).getTextContent());
						} else if (properties.item(l).getNodeName().equals("classification")) {
							collectionMethodType = getMethodTypeFromString(properties.item(l));
						}
					}
					
				} else if (methodSetNode.getNodeName().equals("method")) {
					
					if (stage == null) {
						errorFound("No stage found before method");
						break;
					}
					
					String name = null;
					String notation = null;
					String leadEnd = null;
					MethodType methodType = null;
					NodeList methodNode = methodSetNode.getChildNodes();
					for (int l = 0; l < methodNode.getLength(); l++) {
						if (methodNode.item(l).getNodeName().equals("name")) {
							name = methodNode.item(l).getTextContent();
						} else if (methodNode.item(l).getNodeName().equals("classification")) {
							methodType = getMethodTypeFromString(methodNode.item(l));
						} else if (methodNode.item(l).getNodeName().equals("notation")) {
							notation = methodNode.item(l).getTextContent();
							notation = notation.replaceAll("\\-", "x");
							String[] notations = notation.split(",");
							if (notations.length < 2) {
								notations = new String[] { notations[0], "x" };
							}
							if (notations[0].length() < notations[1].length()) {
								notation = "+" + notations[1];
								notation += new StringBuilder(notations[1]).reverse().toString();
								leadEnd = notations[0];
							} else {
								notation = "&" + notations[0];
								leadEnd = notations[1];
							}
						}
					}
					
					if (!methods.containsKey(stage)) {
						methods.put(stage, new ArrayList<Method>());
					}
					
					try {
						methods.get(stage).add(
								new Method(name,
										   methodType == null ? collectionMethodType : methodType,
										   stage,
										   notation,
										   leadEnd == null ? "x" : leadEnd,
										   null,
										   null,
										   0,
										   leadEnd == null ? 1 : Stage.getPositionOfLabel(leadEnd.charAt(leadEnd.length() - 1))));
					} catch (InvalidPlaceNotationException e) {
						errorFound("Error in place notation: " + e.getLocalizedMessage());
						break;
					}
					
				}
			}
			
		}

        final ArrayList<Method> readMethods = new ArrayList<Method>();
        for (int i = Stage.UNUS.getBells(); i < Stage.SEXTUPLES.getBells(); i++) {
        	if (methods.containsKey(Stage.getStage(""+i))) {
        		Collections.sort(methods.get(Stage.getStage(""+i)));
        		readMethods.addAll(methods.get(Stage.getStage(""+i)));
        	}
        }
		return readMethods.toArray(new Method[readMethods.size()]);
	}

	private MethodType getMethodTypeFromString(final Node node) {
		Node little;
		switch (node.getTextContent()) {
		case "Place":
			return MethodType.PLACE;
		case "Bob":
			return MethodType.BOB;
		case "Slow Course":
			return MethodType.SLOWCOURSE;
		case "Treble Bob":
			return MethodType.TREBLEBOB;
		case "Delight":
			return MethodType.DELIGHT;
		case "Surprise":
			little = node.getAttributes().getNamedItem("little");
			if (little != null && little.getTextContent().equals("true")) {
				return MethodType.LITTLESURPRISE;
			} else {
				return MethodType.SURPRISE;
			}
		case "Alliance":
			little = node.getAttributes().getNamedItem("little");
			if (little != null && little.getTextContent().equals("true")) {
				return MethodType.LITTLEALLIANCE;
			} else {
				return MethodType.ALLIANCE;
			}
		case "Treble Place":
			return MethodType.TREBLEPLACE;
		case "Hybrid":
			return MethodType.HYBRID;
		default:
			return MethodType.PRINCIPLE;
		}
	}
	
}
