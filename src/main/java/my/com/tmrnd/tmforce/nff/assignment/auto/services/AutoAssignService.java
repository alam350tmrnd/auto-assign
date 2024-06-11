/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentService;
import my.com.tmrnd.tmforce.nff.assignment.ZoneService;

/**
 *
 * @author Alam
 */
public class AutoAssignService extends AssignmentService {

    @Override
    public ZoneService getZoneService(CoArea zone) {
        return new AutoAssignZoneService(zone);
    }
    
}
