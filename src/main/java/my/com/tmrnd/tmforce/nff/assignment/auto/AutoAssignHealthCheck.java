/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto;

import com.codahale.metrics.health.HealthCheck;
import java.util.Date;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentConstant;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignHealthCheck extends HealthCheck {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected Result check() throws Exception {
    
        Integer threshold = AssignmentConstant.MAINLOOP_DELAY * 2;

        Date mainLoopLastExec = AssignmentSingleton.getMainLoopLastExec();
        if (mainLoopLastExec != null) {
            Integer diff = TimeUtil.timeDifferenceInMinutes(new Date(), mainLoopLastExec);
            log.debug("~ mainLoop healthcheck. loop lapse = " + diff+"/"+threshold);
            if (diff > threshold) {
                return Result.unhealthy("mainLoop irresponsive. minute = " + diff+"/"+threshold);
            } else {
                return Result.healthy();
            }
        } else {
            return Result.unhealthy("mainLoopLastExec is null");
        }
    }

}
