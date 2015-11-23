package introsde.rest.ehealth.resources;

import introsde.rest.ehealth.model.HealthMeasureHistory;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class HealthMeasureResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;
    String measureType;
    int mid; 

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    
    public HealthMeasureResource(UriInfo uriInfo, Request request,int id, String measureType, int mid) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.measureType = measureType;
        this.mid = mid;
    }

     //Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<HealthMeasureHistory> getHealthMeasureHistory3() {
        List<HealthMeasureHistory> hmh = HealthMeasureHistory.getMeasureTypeByMid(id, measureType, mid);
        if (hmh == null)
            throw new RuntimeException("Get: health measure type with " + mid + " not found");
        return hmh;
    }

    
}