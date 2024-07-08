/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.AtTicket;
import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import static my.com.tmrnd.tmforce.nff.assignment.AssignmentConstant.ACTIVITY_STATUS.PENDING_ACCEPT;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.ZoneService;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignZoneService implements ZoneService {

    DatabaseService databaseService;
    private Logger log;
    CoArea zone;

    public AutoAssignZoneService(CoArea zone) {
        this.zone = zone;
        databaseService = DatabaseService.getDatabaseService();
        log = LoggerFactory.getLogger(getClass().getName() + "(" + zone.getAreaId() + ")");
    }

    public void printActivity(AtActivity atActivity) {
        AtTicket atTicket = atActivity.getTicketId();

        try {
            log.info(":: {} {} {} {} hrs since {} {} {}",
                    atTicket.getTicketId(),
                    atActivity.getActivityId(),
                    atTicket.getPriorityId().getLovValue(),
                    String.format("%,8.2f", (double) TimeUtil.timeDifferenceInMinutes(atTicket.getCreatedDatetime(), new Date()) / 60),
                    TimeUtil.getTimeString(atTicket.getCreatedDatetime(), "dd/MM/yyyy HH:mm:ss"),
                    atActivity.getActivityStatus().getLovValue(),
                    atActivity.getPlannedStart() == null ? "" : "appt:" + TimeUtil.getTimeString(atActivity.getPlannedStart(), "dd/MM/yyyy HH:mm:ss")
            );
        } catch (Exception exception) {
            log.error("error printing log " + atActivity.getActivityId() + " " + exception.getMessage());
        }
    }

    public int processZone() {
        BigDecimal zoneId = zone.getAreaId();
        String zoneName = zone.getName();
        log.debug("start zone");
        int activityCount = 0;

        String appointmentInterval = AssignmentSingleton.getAppointmentInterval();
        String unacceptInterval = AssignmentSingleton.getUnacceptInterval();
        String maxPendingAccept = AssignmentSingleton.getMaxPendingAccept();

        List<AtActivity> activityList = databaseService.getNffCorrectiveActivityListByZoneId(zoneId, appointmentInterval, unacceptInterval, maxPendingAccept);

        if (activityList != null && !activityList.isEmpty()) {

            log.debug("activityList found = " + activityList.size());

            for (AtActivity atActivity : activityList) {
                printActivity(atActivity);

            }

            for (AtActivity atActivity : activityList) {

                String activityId = atActivity.getActivityId();
                ActivityService activityService = new ActivityService(databaseService, zone, activityId);

                String status = atActivity.getActivityStatus().getLovValue();
                printActivity(atActivity);
                boolean assign = true;
                if (PENDING_ACCEPT.equals(status)) {
                    assign = activityService.processPendingAccept(atActivity);
                }
                if (assign) {
                    log.info(status);
                    activityService.assignActivity(atActivity);
                }
            }
            activityList.clear();
        }

        log.debug("done zone");
        return activityCount;
    }

    @Override
    public void endZone() {

    }

}
