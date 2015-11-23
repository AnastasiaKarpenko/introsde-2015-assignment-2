package introsde.rest.ehealth;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionListener implements ApplicationEventListener {
    @Override
    public void onEvent(ApplicationEvent event) {
    }
    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new ExceptionRequestEventListener();
    }
    public static class ExceptionRequestEventListener implements RequestEventListener{
        private final Logger logger;
        public ExceptionRequestEventListener(){
            logger = Logger.getLogger(getClass().getName());
        }
        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()){
                case ON_EXCEPTION:
                    Throwable t = event.getException();
                    logger.log(Level.SEVERE, "Found exception for requestType: "+event.getType(), t);
            }
        }
    }
}
