package introsde.rest.ehealth.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import introsde.rest.ehealth.model.Person;

public class MeasureNamesReader {

	private Document doc;
    private XPath xpath;
	
	public MeasureNamesReader(String body) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(body));
        doc = builder.parse(is);
        
        xpath = getXPathObj(); 
	}
	
	public XPath getXPathObj() {
        XPathFactory factory = XPathFactory.newInstance();
        return factory.newXPath();
    }
	
	public List<String> getAllNames() throws XPathExpressionException {  
		List<String> names = new ArrayList<String>();
		
        XPathExpression expr = xpath.compile("/measureDefinitions/*");
        NodeList node = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        
        for (int i = 0; i < node.getLength(); i ++) {
        	Node nameNode = node.item(i);
        	names.add(nameNode.getTextContent());
		}
        
        return names;
    }
}
