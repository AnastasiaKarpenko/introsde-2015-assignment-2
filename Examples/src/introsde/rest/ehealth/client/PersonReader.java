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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import introsde.rest.ehealth.model.Person;

public class PersonReader {

	private Document doc;
    private XPath xpath;
	
	public PersonReader(String body) throws ParserConfigurationException, SAXException, IOException {
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
	
	public List<Person> getAllPersonsIds() throws XPathExpressionException {  
		List<Person> persons = new ArrayList<Person>();
		
        XPathExpression expr = xpath.compile("/people/*");
        NodeList node = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);       
        for (int i = 0; i < node.getLength(); i ++) {        
        	Node nameNode = node.item(i);
        	
        	NodeList childs = nameNode.getChildNodes();
        	
        	for (int z = 0; z <childs.getLength(); z++) {    
        		Node child = childs.item(z);
        		if(child.getNodeName().equals("idPerson")){    
        			Person person = new Person();
        			person.setIdPerson(Integer.parseInt(child.getTextContent()));
        			persons.add(person);        			
        		}
        	}
		}
        
        return persons;
    }
	
	public Person getPerson() {
		Person person = new Person();
		
		try{
			person.setIdPerson(Integer.parseInt(getPersonId()));
			person.setName(getPersonName());
			person.setLastname(getPersonLastName());
		} catch (XPathExpressionException e){
			System.out.println("XPathExpressionException");
		} catch (NumberFormatException e1){
			System.out.println("NumberFormatException");
		}
		
		return person;   
	}
	
	private String getPersonId() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/person/idPerson");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        return node.getTextContent();
	}
	
	private String getPersonName() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/person/name");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        return node.getTextContent();
	}
	
	private String getPersonLastName() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/person/lastname");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        return node.getTextContent();
	}
}
