package introsde.rest.ehealth.resources;

import introsde.rest.ehealth.model.HealthMeasureHistory;
import introsde.rest.ehealth.model.Person;
import introsde.rest.ehealth.model.LifeStatus;

import java.util.List;

import java.io.IOException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class HealthMeasureHistoryResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;
    String measureType;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    
    public HealthMeasureHistoryResource(UriInfo uriInfo, Request request,int id, String measureType) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.measureType = measureType;
        
    }

    // Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<HealthMeasureHistory> getHealthMeasureHistory2() {
        List<HealthMeasureHistory> hmh = HealthMeasureHistory.getMeasureTypeById(id, measureType);
        if (hmh == null)
            throw new RuntimeException("Get: health measure history with " + id + " not found");
        return hmh;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    public HealthMeasureHistory newHealthMeasureHistory(HealthMeasureHistory newHealthMeasureHistory) throws IOException {
        System.out.println("Creating new measure...");
        Person person = Person.getPersonById(id);
        List<LifeStatus> lifeStatusList = person.getLifeStatus();
        LifeStatus lifeStatus = null;
            for (int i = 0; i<lifeStatusList.size(); i++) {
                LifeStatus lifeStatusTemp = lifeStatusList.get(i); 
                String measureName = lifeStatusTemp.getMeasure();  
                if (measureName.equals(measureType)) {
                    lifeStatus = lifeStatusTemp;
                }

            }

        String oldMeasureValue = lifeStatus.getValue();
        LifeStatus newLifeStatus = lifeStatus;
        newLifeStatus.setValue(newHealthMeasureHistory.getValue());
        lifeStatus.updateLifeStatus(newLifeStatus);           
        newHealthMeasureHistory.setValue(oldMeasureValue);
        newHealthMeasureHistory.setPerson(person);
        newHealthMeasureHistory.setMeasureName(measureType);
        return HealthMeasureHistory.saveHealthMeasureHistory(newHealthMeasureHistory);
    }

    @Path("{mid}")
    public HealthMeasureResource getHealthMeasureResource(@PathParam("mid") int mid) {
        return new HealthMeasureResource(uriInfo, request, id, measureType, mid);
    }
}