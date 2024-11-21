/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.nff.assignment.service.auto.AutoService;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.service.auto.AutoZoneService;
import my.com.tmrnd.tmforce.nff.assignment.service.messaging.MessagingService;

/**
 *
 * @author Alam
 */
public class AutoAssignService extends AutoService {

    @Override
    public AutoZoneService getZoneService(CoArea zone) {
        return new AutoAssignZoneService(zone);
    }

    public boolean setSingletonConfig() {
        log.debug("load AutoAssign SingletonConfig");
        boolean isCommonOk = super.setSingletonConfig();
        
        AutoAssignMessagingService.failedAcceptLevelList = new MessagingService().getNotificationLevelList("nff.failedaccept.1st.notified.user.level");
        AutoAssignMessagingService.lastFailedAcceptLevelList = new MessagingService().getNotificationLevelList("nff.failedaccept.2nd.notified.user.level");
        return isCommonOk;
    }

}
