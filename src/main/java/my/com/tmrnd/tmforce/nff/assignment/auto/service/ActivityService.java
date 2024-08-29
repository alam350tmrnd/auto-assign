/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.AtStatusLog;
import my.com.tmrnd.tmforce.common.db.entity.AtTicket;
import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.common.db.entity.CoListofvalue;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import static my.com.tmrnd.tmforce.nff.assignment.AssignmentConstant.ACTIVITY_STATUS.PENDING_ACCEPT;
import static my.com.tmrnd.tmforce.nff.assignment.AssignmentConstant.ACTIVITY_STATUS.PENDING_ASSIGN;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author R10249
 */
public class ActivityService {

    private DatabaseService databaseService;
    private Logger log;
    private String activityId;

    public ActivityService(DatabaseService databaseService, CoArea zone, String activityId) {
        this.databaseService = databaseService;
        this.activityId = activityId;
        log = LoggerFactory.getLogger(getClass().getName() + " - " + activityId);
    }

    public void assignActivity(AtActivity atActivity) {
        log.info("Assigning activity..");
        AtTicket atTicket = atActivity.getTicketId();
        String activityId = atActivity.getActivityId();
        String status = atActivity.getActivityStatus().getLovValue();

        boolean isReassign = PENDING_ACCEPT.equals(status) && atActivity.getAssignTo() != null;
        log.info("isReassign=" + isReassign);

        DistanceService distanceService = new DistanceService(activityId);

        Date startWorkingHour = null;
        Date endWorkingHour = null;
        Date tonight = null;
        if (AssignmentSingleton.getWorkStartTime() == null || AssignmentSingleton.getWorkEndTime() == null) {
            log.error("error WorkStartTime/WorkEndTime not set. assignment aborted");
            return;
        }
        try {
            tonight = TimeUtil.getTodaysTime("23:59:59");
            startWorkingHour = TimeUtil.getTodaysTime(AssignmentSingleton.getWorkStartTime() + ":00");
            endWorkingHour = TimeUtil.getTodaysTime(AssignmentSingleton.getWorkEndTime() + ":00");
            String pattern = "dd/MM/yyyy HH:mm:ss";
            log.info("tonight={}, startWorkingHour={}, endWorkingHour={}",
                    TimeUtil.getTimeString(tonight, pattern),
                    TimeUtil.getTimeString(startWorkingHour, pattern),
                    TimeUtil.getTimeString(endWorkingHour, pattern));
        } catch (Exception ex) {
            log.error("error converting date. assignment aborted", ex);
            return;
        }

        Date plannedStart = atActivity.getPlannedStart() == null ? new Date() : atActivity.getPlannedStart();

        boolean isOnOfficeHour = plannedStart.after(startWorkingHour) && plannedStart.before(endWorkingHour);

        Date plannedEnd = isOnOfficeHour ? endWorkingHour : tonight;

        boolean useDistance = distanceService.isTicketCoordinateOk(atTicket);

        List<CoResources> resourceList = isOnOfficeHour ? databaseService.getNormalResourceList(activityId) : databaseService.getStandbyResourceList(activityId);

        if (resourceList == null || resourceList.isEmpty()) {
            log.info("no resources found");
            return;
        } else {
            log.info("{} resources found", resourceList.size());
        }

        List<Candidate> candidateList = new ArrayList<>();

        for (CoResources coResources : resourceList) {
            String icNo = coResources.getIcNo();
            String icNoStaffNo = getStaffNoIcNo(coResources);

            boolean isWorking = databaseService.checkResourceIsWorking(icNo, plannedStart, plannedEnd);
            boolean isSamePerson = isReassign && icNo.equals(atActivity.getAssignTo().getIcNo());
            boolean isQualify = (!isReassign && isWorking) || (isReassign && isWorking && !isSamePerson);
            
            if (isQualify) {
                Candidate candidate = new Candidate();
                candidate.setCoResources(coResources);
                Integer inHand = databaseService.getInHand(icNo);
                candidate.setInHandCount(inHand);
                Integer yesterdayInHand = databaseService.getYesterdayInHand(icNo);
                candidate.setYesterdayInHandCount(yesterdayInHand);

                if (useDistance) {
                    Double distance = distanceService.getDistance(coResources, atTicket);
                    candidate.setDistance(distance);
                }
                log.info("{} - {} added", icNoStaffNo, candidate);
                candidateList.add(candidate);
            } else if (!isWorking) {
                log.info("{} is on leave", icNoStaffNo);
            } else if (isSamePerson) {
                log.info("{} is same person", icNoStaffNo);
            } else {
                log.info("{} is not qualify", icNoStaffNo);
            }
        }

        if (candidateList.isEmpty()) {
            log.info("candidateList is empty");
            return;
        }

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources, "P0. Only");
            return;
        }

        CandidateSorter sorter = new CandidateSorter();
        
        candidateList = sorter.getLeastYesterdayCountList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources, "P5. Least Yesterday Count");
            return;
        }

        candidateList = sorter.getLeastInHandCountList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources, "P6. Least in Hand");
            return;
        }

        candidateList = sorter.getLeastDistanceList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources, "P7. Least Distance");
            return;
        }

        Collections.shuffle(candidateList, new Random());

        CoResources coResources = candidateList.get(0).getCoResources();
        assignResource(atActivity, coResources, "P8. Random");

    }

    public boolean processPendingAccept(AtActivity atActivity) {
        log.info("processPendingAccept");
        String maxPendingAccept = AssignmentSingleton.getMaxPendingAccept();
        String pendingAcceptCount = databaseService.getPendingAcceptCount(atActivity);
        boolean maxed = maxPendingAccept.equals(pendingAcceptCount);
        if (maxed) {
            databaseService.updatePendingAcceptMaxed(atActivity, "TRUE");
        }else{
            databaseService.addPendingAcceptCount(atActivity, false);
        }
        log.info("pending accept count = {},assign = {}", pendingAcceptCount + "/" + maxPendingAccept, maxed);
        
        new AutoAssignMessagingService().sendTaskUnAcceptedMessageToSupervisor(atActivity, pendingAcceptCount);
        return !maxed;

    }

    public void assignResource(AtActivity atActivity, CoResources assignTo, String method) {
        String staffNoIcNo = getStaffNoIcNo(assignTo);
        log.info("assignResource({}, {})", staffNoIcNo, method);
        String activityId = atActivity.getActivityId();
        String ticketId = atActivity.getTicketId().getTicketId();
        String staffNo = assignTo.getStaffNo();

        CoListofvalue oldActivityStatusLov = atActivity.getActivityStatus();
        String oldActivityStatus = oldActivityStatusLov.getLovName();
        log.debug("oldActivityStatus=" + oldActivityStatus);
        BigDecimal pendingAcceptLovId = AssignmentSingleton.getLovIdPendingAccept();
        CoListofvalue pendingAcceptActivityStatus = new CoListofvalue(pendingAcceptLovId);
        if (pendingAcceptActivityStatus == null) {
            log.error(activityId + " assignedActivityStatus LOV not found lovId : " + pendingAcceptLovId);
        }
        atActivity.setAssignTo(assignTo);
        atActivity.setActivityStatus(pendingAcceptActivityStatus);
        databaseService.updateActivity(atActivity);

        AtStatusLog atStatusLog = new AtStatusLog();

        atStatusLog.setActivityId(activityId);
        atStatusLog.setDescription("Auto Assign to " + staffNo+", "+method);
        atStatusLog.setLogDatetime(new Date());
        atStatusLog.setNewStatus(PENDING_ACCEPT);
        atStatusLog.setOldStatus(oldActivityStatus);
        atStatusLog.setTicketId(ticketId);
        databaseService.insertAtStatusLog(atStatusLog);
        databaseService.updatePendingAcceptDateTime(atActivity);
        boolean isNew = PENDING_ASSIGN.equals(oldActivityStatus);
        if (isNew) {
            databaseService.addPendingAcceptCount(atActivity, isNew);
        }
        databaseService.updatePendingAcceptMaxed(atActivity, "FALSE");
        boolean isNotified = new AutoAssignMessagingService().sendTaskAcceptanceMessage(assignTo, atActivity);
        log.info(activityId + " updated with status = " + PENDING_ACCEPT);
    }

    public String getStaffNoIcNo(CoResources coResources) {
        String icNo = coResources.getIcNo();
        String staffNo = coResources.getStaffNo();
        return staffNo + "/" + icNo;
    }

}
