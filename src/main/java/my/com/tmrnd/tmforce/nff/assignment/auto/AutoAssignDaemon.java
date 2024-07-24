/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto;
import my.com.tmrnd.tmforce.nff.assignment.auto.service.AutoAssignService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignDaemon {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
   AutoAssignService autoAssignService;

    public AutoAssignDaemon() {
        autoAssignService = new AutoAssignService();
    }

    public void startAutoAssignService() {
        new Thread(() -> {
            autoAssignService.start();

        }).start();
    }    

}
