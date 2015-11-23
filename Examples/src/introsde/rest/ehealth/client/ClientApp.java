package introsde.rest.ehealth.client;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import introsde.rest.ehealth.model.Person;
import introsde.rest.ehealth.model.HealthMeasureHistory;
import introsde.rest.ehealth.model.LifeStatus;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Random;

public class ClientApp {
    private static int id = 0;   

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        Client client = ClientBuilder.newClient(clientConfig);
        WebTarget service = client.target(getBaseURI());
        Response response;
        String body;
        int status;     

        
  
        //STEP 3.1
        System.out.println("\n step 3.1");
        List<Person> personsXml = getPersonList(service, MediaType.APPLICATION_XML);//step 3.1 XML
        List<Person> personsJson = getPersonList(service, MediaType.APPLICATION_JSON);//step 3.1 JSON

        if(personsXml.size() > 0 && personsJson.size() > 0){
            System.out.println("step 3.1 : OK");
        } else {
            System.out.println("step 3.1 : ERROR -> STOP TASK");
            return;
        }
        
        Person firstPerson = personsXml.get(0);//get first person
        Person lastPerson = personsXml.get(personsXml.size() - 1);//get last person
        
        //STEP 3.2
        System.out.println("\n step 3.2");
        Person firstPersonXml = getPersonById(service, firstPerson.getIdPerson(), MediaType.APPLICATION_XML);
        Person firstPersonJson = getPersonById(service, firstPerson.getIdPerson(), MediaType.APPLICATION_JSON);
	       
        if(firstPersonXml != null && firstPersonJson != null){
            System.out.println("step 3.2 : OK");
        } else {
            System.out.println("step 3.2 : ERROR -> STOP TASK");
            return;
        }

        //STEP 3.3
        System.out.println("\n step 3.3");
        Random rand = new Random();
        String oldName = firstPersonJson.getName();
        String newName = oldName + " " + rand.nextInt();
        firstPersonJson.setName(newName);        
		
		boolean done = updatePerson(service, firstPersonJson, MediaType.APPLICATION_JSON);  

        Person firstPersonJsonUpdated;
        if(done){
            firstPersonJsonUpdated = getPersonById(service, firstPersonJson.getIdPerson(), MediaType.APPLICATION_JSON);

            if(firstPersonJsonUpdated.getName().equals(newName)){
                System.out.println("step 3.3 JSON : OK");
            } else {
                System.out.println("step 3.3 JSON : ERROR -> STOP TASK");
                return;
            }
        } else {
            System.out.println("step 3.3 JSON : ERROR -> STOP TASK");
            return;
        }        

        oldName = firstPersonJson.getName();
        newName = oldName + " " + rand.nextInt();
        firstPersonJsonUpdated.setName(newName);    

		done = updatePerson(service, firstPersonJsonUpdated, MediaType.APPLICATION_XML);
        
        Person firstPersonXMLUpdated;
        if(done){
            firstPersonXMLUpdated = getPersonById(service, firstPersonJson.getIdPerson(), MediaType.APPLICATION_XML);

            if(firstPersonJsonUpdated.getName().equals(newName)){
                System.out.println("step 3.3 XML : OK");
            } else {
                System.out.println("step 3.3 XML : ERROR -> STOP TASK");
                return;
            }
        } else {
            System.out.println("step 3.3 XML : ERROR -> STOP TASK");
            return;
        }  

		//steps Step 3.4 and Step 3.5
        System.out.println("\n steps 3.4 and 3.5");
		if(createChuckNorris(service)) {
            System.out.println("steps 3.4 and 3.5 : OK");
        }
		
        //steps Step 3.6
        System.out.println("\n steps 3.6");
        List<String> namesJson = getMeasureNames(service, MediaType.APPLICATION_JSON);
        List<String> namesXml = getMeasureNames(service, MediaType.APPLICATION_XML);

		if(namesJson != null && namesXml != null){
            System.out.println("step 3.6 : OK");
        } else {
            System.out.println("step 3.6 : ERROR -> STOP TASK");
            return;
        }
		
	    //Step 3.7	
		getMeasureValuesByName(service, MediaType.APPLICATION_JSON, namesJson, firstPerson);//3.7
        HealthMeasureHistory hmHistory = getMeasureValuesByName(service, MediaType.APPLICATION_JSON, namesJson, lastPerson);//3.7

        //Step 3.8  
        if(hmHistory == null){
            return;
        }

        System.out.println("Step 3.8: " + hmHistory.getIdMeasureHistory() + " " + hmHistory.getMeasureName());
		getOneMeasureTypeValueByName(service, MediaType.APPLICATION_JSON, firstPerson.getIdPerson(), hmHistory.getMeasureName(), hmHistory.getIdMeasureHistory());//3.8	

		//Step 3.9
        List<HealthMeasureHistory> mCurrHistory 
            = getMeasureValueByName(service, MediaType.APPLICATION_JSON, hmHistory.getMeasureName(), firstPerson.getIdPerson());


        int currentSize = mCurrHistory.size();


		
        HealthMeasureHistory newHealthMeasureHistory = new HealthMeasureHistory();
        newHealthMeasureHistory.setTimestamp("2013-06-29 22:00:00");
        newHealthMeasureHistory.setValue("1.78");
        newHealthMeasureHistory.setMeasureName("height");

		createMeasureType(service, firstPerson.getIdPerson(), hmHistory.getMeasureName(), newHealthMeasureHistory, MediaType.APPLICATION_JSON);
		
		List<HealthMeasureHistory> mNewHistory 
            = getMeasureValueByName(service, MediaType.APPLICATION_JSON, hmHistory.getMeasureName(), firstPerson.getIdPerson());

            if(mNewHistory.size() > currentSize){
                System.out.println("step 3.9 : OK -> NEW SIZE " + mNewHistory.size() + " OLD SIZE: " + currentSize);
            } else {
                System.out.println("step 3.9 : ERROR");
            }
    }

    private static void createMeasureType(WebTarget service, int personId, String measureType, HealthMeasureHistory newHealthMeasureHistory, String type){
    	Entity<HealthMeasureHistory> entity = Entity.entity(newHealthMeasureHistory, type);
    	String url = "person/" + personId + "/" + measureType;
    	
        Response response = service.path(url)
        		.request()
                .accept(type)
                .post(entity);
        
        String body = response.readEntity(String.class);
        int respStatus = response.getStatus();
        
        if(respStatus == 200 || respStatus == 201){
            printStatus("POST", url, type, type, "OK",  body, respStatus);
        } else {
            printStatus("POST", url, type, type, "ERROR",  body, respStatus);
        }
        
    }
    
    private static HealthMeasureHistory getMeasureValuesByName(WebTarget service, String type, List<String> names, Person person){
        HealthMeasureHistory hmHistory = null;
    	for(String name : names){

    		List<HealthMeasureHistory> result = getMeasureValueByName(service, type, name, person.getIdPerson());
            if(result != null){
                if(result.size() > 0){
                  hmHistory = result.get(0);      
                } 
            }                           
    	}    	
        return hmHistory;
    }
    
    private static List<HealthMeasureHistory> getOneMeasureTypeValueByName(WebTarget service, String type, int personId, String measureType, int mid){
    	String url = "person/" + personId + "/" + measureType + "/" + mid;
    	
    	Response response = service.path(url)
                .request()
                .accept(type)
                .get();
    	
    	String body = response.readEntity(String.class);
    	int respStatus = response.getStatus(); 

        if(respStatus == 200){
            List<HealthMeasureHistory> рealthMeasureHistoryList = new ArrayList<HealthMeasureHistory>();

            JSONArray jsonMeasureDef = new JSONArray(body); 
            for (int i = 0; i < jsonMeasureDef.length(); i++) {
                JSONObject row = jsonMeasureDef.getJSONObject(i);

                int idMeasureHistory = row.getInt("idMeasureHistory");
                String timestamp = row.getString("timestamp");
                String value = row.getString("value");
                String measureName = row.getString("measureName");

                HealthMeasureHistory рealthMeasureHistory = new HealthMeasureHistory();
                рealthMeasureHistory.setIdMeasureHistory(idMeasureHistory);
                рealthMeasureHistory.setTimestamp(timestamp);
                рealthMeasureHistory.setValue(value);
                рealthMeasureHistory.setMeasureName(measureName);

                рealthMeasureHistoryList.add(рealthMeasureHistory);
            }
        
            if(рealthMeasureHistoryList.size() > 0){
                printStatus("GET", url, type, type, "OK",  body, respStatus);
            } else {
                printStatus("GET", url, type, type, "ERROR",  body, respStatus);
            }            

            return рealthMeasureHistoryList;
        } else {
            printStatus("GET", url, type, type, "ERROR",  body, respStatus);
        }
        return null;
    }
    
    private static List<HealthMeasureHistory> getMeasureValueByName(WebTarget service, String type, String name, int personId){
    	String url = "person/" + personId + "/" + name;
    	
    	Response response = service.path(url)
                .request()
                .accept(type)
                .get();
    	
    	String body = response.readEntity(String.class);
    	int respStatus = response.getStatus(); 

        
        if(respStatus == 200){
            List<HealthMeasureHistory> рealthMeasureHistoryList = new ArrayList<HealthMeasureHistory>();

            JSONArray jsonMeasureDef = new JSONArray(body); 
            for (int i = 0; i < jsonMeasureDef.length(); i++) {
                JSONObject row = jsonMeasureDef.getJSONObject(i);

                int idMeasureHistory = row.getInt("idMeasureHistory");
                String timestamp = row.getString("timestamp");
                String value = row.getString("value");
                String measureName = row.getString("measureName");

                HealthMeasureHistory рealthMeasureHistory = new HealthMeasureHistory();
                рealthMeasureHistory.setIdMeasureHistory(idMeasureHistory);
                рealthMeasureHistory.setTimestamp(timestamp);
                рealthMeasureHistory.setValue(value);
                рealthMeasureHistory.setMeasureName(measureName);

                рealthMeasureHistoryList.add(рealthMeasureHistory);
            }
        
            if(рealthMeasureHistoryList.size() > 0){
                printStatus("GET", url, type, type, "OK",  body, respStatus);
            } else {
                printStatus("GET", url, type, type, "ERROR",  body, respStatus);
            }            

            return рealthMeasureHistoryList;
        } else {
            printStatus("GET", url, type, type, "ERROR",  body, respStatus);
        }
        return null;
    }
    
    private static List<String> getMeasureNames(WebTarget service, String type) {
    	Response response = service.path("measureNames")
                .request()
                .accept(type)
                .get();
        
    	String body = response.readEntity(String.class);
    	int respStatus = response.getStatus();	
	
    	if(respStatus == 200){
    		List<String> names = null;
    		
    		if(type.equals(MediaType.APPLICATION_JSON)){
    			JSONArray jsonMeasureNames = new JSONArray(body);    			
    			
        		names = parseMeasureNames(jsonMeasureNames);        		
        	} else if (type.equals(MediaType.APPLICATION_XML)) {
        		try {
        			MeasureNamesReader measureNames = new MeasureNamesReader(body);
        			names = measureNames.getAllNames();
        		} catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {
        			return null;
				} 
        	}
    		return names;
    	}
    	return null;
	}

    //steps 3.4 and 3.5
	private static boolean createChuckNorris(WebTarget service) {	
        //Creating person
    	Person chuckNorris = new Person();
    	chuckNorris.setName("Chuck");
    	chuckNorris.setLastname("Norris");
		chuckNorris.setBirthdate("1945/01/01");
		
		LifeStatus weight = new LifeStatus();
		weight.setValue("78.9");
		weight.setMeasure("weight");
		
		LifeStatus height = new LifeStatus();
		height.setValue("172");
		height.setMeasure("height");
		
		List<LifeStatus> lifeStatus = new LinkedList<LifeStatus>();
		lifeStatus.add(weight);
		lifeStatus.add(height);
		
		chuckNorris.setLifeStatus(lifeStatus);
		
        //Step 3.4 for XML
		Person resultingPersonXml = createPerson(service, chuckNorris, MediaType.APPLICATION_XML);

        //Step 3.5 for XML
		if(resultingPersonXml != null){

			if(!deletePerson(service, resultingPersonXml.getIdPerson())){
                return false;
            }

			getPersonById(service, resultingPersonXml.getIdPerson(), MediaType.APPLICATION_XML);
		} else {
            return false;
        }
		
        //Step 3.4 for JSON		
		Person resultingPersonJson = createPerson(service, chuckNorris, MediaType.APPLICATION_JSON);

        //Step 3.5 for JSON
		if(resultingPersonJson != null){

			if(!deletePerson(service, resultingPersonJson.getIdPerson())){
                return false;
            }

			getPersonById(service, resultingPersonXml.getIdPerson(), MediaType.APPLICATION_JSON);
		}else {
            return false;
        }
        return true;
	}

	private static URI getBaseURI() {
        // return UriBuilder.fromUri(
        //         "http://localhost:5700/sdelab").build();

        return UriBuilder.fromUri(
                "https://infinite-taiga-5831.herokuapp.com/sdelab").build();
        
    }    
    
	private static Person getPersonById(WebTarget service, int personId, String type){
		Response response = service.path("person/" + personId)
	                .request()
	                .accept(type)
	                .get();
	        
		String body = response.readEntity(String.class);
		int respStatus = response.getStatus();
		
		Person resultPerson = null;
		
        if(respStatus == 200){             
        	if(type.equals(MediaType.APPLICATION_JSON)){
        		JSONObject jsonPerson = new JSONObject(body);
             	resultPerson = parsePerson(jsonPerson);             	
        	} else if (type.equals(MediaType.APPLICATION_XML)) {
				try {
					PersonReader personXpathReader = new PersonReader(body);
					resultPerson = personXpathReader.getPerson();	        		
				} catch (ParserConfigurationException | SAXException | IOException e) {
					
				}        		
        	}
            printStatus("GET", "person/" + personId, type, type, "OK",  body, respStatus);
        } else if(respStatus == 404){
        	System.out.println("Person not found");
            printStatus("GET", "person/" + personId, type, type, "ERROR",  body, respStatus);
        }
        return resultPerson;
    }
	
    private static boolean updatePerson(WebTarget service, Person person, String type){
    	Entity<Person> entity = Entity.entity(person, type);
		
        Response response = service.path("person/" + person.getIdPerson())
                .request()
                .accept(type)
                .put(entity);
        
        String body = response.readEntity(String.class);
        int respStatus = response.getStatus();

        Person resultPerson = null;
        if(respStatus == 200 || respStatus == 201){             
            printStatus("PUT", "person/" + person.getIdPerson(), type, type, "OK",  body, respStatus);
            return true;
        }    else {
            printStatus("PUT", "person/" + person.getIdPerson(), type, type, "ERROR",  body, respStatus);
        }
        return false;
    }
    
    private static Person createPerson(WebTarget service, Person person, String type){
    	Entity<Person> entity = Entity.entity(person, type);
		
        Response response = service.path("person")
                .request()
                .accept(type)
                .post(entity);
        
        String body = response.readEntity(String.class);
        int respStatus = response.getStatus();
        
        Person resultPerson = null;
        
        if(respStatus == 200){             
        	if(type.equals(MediaType.APPLICATION_JSON)){
        		JSONObject jsonPerson = new JSONObject(body);
             	resultPerson = parsePerson(jsonPerson);             	
        	} else if (type.equals(MediaType.APPLICATION_XML)) {
				try {
					PersonReader personXpathReader = new PersonReader(body);
					resultPerson = personXpathReader.getPerson();	        		
				} catch (ParserConfigurationException | SAXException | IOException e) {
					
				}        		
        	}
            printStatus("POST", "person", type, type, "OK",  body, respStatus);
        } else {
            printStatus("POST", "person", type, type, "ERROR",  body, respStatus);
        }
        return resultPerson;
    }
    
    private static boolean deletePerson(WebTarget service, int personId){
		
    	String path = "person/" + personId;
    	
        Response response = service.path(path)
                .request()
                .delete();
        
        int respStatus = response.getStatus();
        
        if(respStatus == 204){
            printStatus("DELETE", path, "", "", "OK",  "", respStatus);
        	return true;
        } else {
            printStatus("DELETE", path, "", "", "ERROR",  "", respStatus);
        	return false;
        }
    }
    
    private static Person parsePerson(JSONObject jsonPerson){
        List<LifeStatus> lifeStatusList = new ArrayList<>();
    	Person person = new Person();   
        person.setLifeStatus(lifeStatusList);	

        int personId = 0; 
        if(jsonPerson.has("idPerson")){
            personId = jsonPerson.getInt("idPerson");
        }
    	
        String personFirstName = new String();
        if(jsonPerson.has("name")){
            personFirstName = jsonPerson.getString("name");
        }

    	String personLastName = new String();
        if(jsonPerson.has("lastname") && !jsonPerson.isNull("lastname")){
            personLastName = jsonPerson.getString("lastname");
        }
    	
        String birthDate = new String();
    	if(jsonPerson.has("birthdate") && !jsonPerson.isNull("birthdate")){
            birthDate = jsonPerson.getString("birthdate");
        }

    	person.setIdPerson(personId);
    	person.setName(personFirstName);
    	person.setLastname(personLastName);
        person.setBirthdate(birthDate);

        JSONArray jsonLifeStatus = jsonPerson.getJSONArray("lifeStatus");         
        for (int i = 0; i < jsonLifeStatus.length(); i++) {
            JSONObject row = jsonLifeStatus.getJSONObject(i);
            String value = row.getString("value");
            String measure = row.getString("measure");

            LifeStatus lifeStatus = new LifeStatus();
            lifeStatus.setValue(value);
            lifeStatus.setMeasure(measure);
            lifeStatusList.add(lifeStatus);
        }

    	return person;
    }
    
    private static List<String> parseMeasureNames(JSONArray jsonMeasureNames){
    	List<String> names = new ArrayList<String>();
    	
    	for (int i = 0; i < jsonMeasureNames.length(); i++) {
    	    JSONObject row = jsonMeasureNames.getJSONObject(i);
    	    String name = row.getString("measureName");
    	    names.add(name);
    	}
    	
    	return names;
    }
    
    private static List<Person> getPersonList(WebTarget service, String type){
    	List<Person> persons = new ArrayList<Person>();    	
    	
    	Response response = service.path("person")
                .request()
                .accept(type)
                .get();
        
        String body = response.readEntity(String.class);
        int respStatus = response.getStatus();
        
        if(respStatus == 200){
        	
        	if(type.equals(MediaType.APPLICATION_JSON)){
        		JSONArray jsonNames = new JSONArray(body);   
            	
            	for (int i = 0; i < jsonNames.length(); i++) {
            	    JSONObject jsonPerson = jsonNames.getJSONObject(i);
            	    persons.add(parsePerson(jsonPerson));
            	}
            	
        	} else if (type.equals(MediaType.APPLICATION_XML)) {        		
				try {
					PersonReader pReader = new PersonReader(body);
					persons.addAll(pReader.getAllPersonsIds());
					
				} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
					System.out.println("exception");
				}        		
        	}
            printStatus("GET", "person", type, type, "OK", body, respStatus);
        } else {
            printStatus("GET", "person", type, type, "ERROR", body, respStatus);
        }        

        return persons;
    }
        
    private static void printStatus(String method, String url, String aType, String cType, String resuldCode, String body, int resultId){
        id++;

        String format= "\n\n Request #%d: %s %s Accept: %s Content-Type: %s \n => Result: %s \n => HTTP Status: %d\n %s\n\n";

        String logMsg = String.format(format, id, method, url, aType, cType, resuldCode, resultId, body);

    	System.out.print(logMsg);       
    }
}