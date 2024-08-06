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
        log.info("isReassign="+isReassign);

        DistanceService distanceService = new DistanceService(activityId);

        boolean isAppointment = atActivity.getPlannedStart() != null;

        Date startWorkingHour = AssignmentSingleton.getStartWorkingHour();
        Date endWorkingHour = AssignmentSingleton.getEndWorkingHour();

        Date plannedStart = atActivity.getPlannedStart();
        Date plannedEnd = atActivity.getPlannedEnd();

        Date time = isAppointment ? plannedStart : new Date();

        boolean isOfficeHour = time.after(startWorkingHour) && time.before(endWorkingHour);
        

        boolean useDistance = distanceService.isTicketCoordinateOk(atTicket);

        List<CoResources> resourceList = isOfficeHour ? databaseService.getNormalResourceList(activityId) : databaseService.getStandbyResourceList(activityId);

        if (resourceList == null || resourceList.isEmpty()) {
            log.info("no resources found");
            return;
        } else {
            log.info("{} resources found", resourceList.size());
        }

        List<Candidate> candidateList = new ArrayList<>();

        for (CoResources coResources : resourceList) {
            String icNo =  coResources.getIcNo();
            boolean isWorking = isAppointment && databaseService.checkResourceIsWorking(icNo, plannedStart, plannedEnd);
            isWorking = isWorking || (!isAppointment && databaseService.checkResourceIsWorking(icNo, time, endWorkingHour));
            boolean isQualify = (!isReassign && isWorking) || (isReassign && isWorking && !icNo.equals(atActivity.getAssignTo().getIcNo()));
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
                log.info("{} added", candidate);
                candidateList.add(candidate);
            } else if(isWorking){
                log.info("{} is on leave",icNo);
            }else{
                log.info("{} is not qualify",icNo);
            }
        }

        CandidateSorter sorter = new CandidateSorter();

        candidateList = sorter.getLeastInHandCountList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources);
            return;
        }

        candidateList = sorter.getLeastYesterdayCountList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources);
            return;
        }

        candidateList = sorter.getLeastDistanceList(candidateList);

        if (candidateList.size() == 1) {
            CoResources coResources = candidateList.get(0).getCoResources();
            assignResource(atActivity, coResources);
            return;
        }

        Collections.shuffle(candidateList, new Random());

        CoResources coResources = candidateList.get(0).getCoResources();
        assignResource(atActivity, coResources);

        //start-datetime = planned start else sysdate
        //check start-datetime office hour
        //select resoruce Normal
        //eslse
        //select resource Stanby
        //remove cuti
        //sort workload
        //sort lesser pevious task
        //not first task
        //distance
    }

    public boolean processPendingAccept(AtActivity atActivity) {
        log.info("processPendingAccept");
        String maxPendingAccept = AssignmentSingleton.getMaxPendingAccept();
        String pendingAcceptCount = databaseService.getPendingAcceptCount(atActivity);
        boolean assign = !maxPendingAccept.equals(pendingAcceptCount);
        if (!assign) {
            databaseService.updatePendingAcceptMaxed(atActivity, "TRUE");
        }
        log.info("pending accept count = {},assign = {}", pendingAcceptCount + "/" + maxPendingAccept, assign);

        new AutoAssignMessagingService().sendTaskUnAcceptedMessageToSupervisor(atActivity, pendingAcceptCount);
        return assign;

    }

    public void assignResource(AtActivity atActivity, CoResources assignTo) {
        log.info("assignResource({})", assignTo.getIcNo());
        String activityId = atActivity.getActivityId();
        String ticketId = atActivity.getTicketId().getTicketId();
        String staffNo = assignTo.getStaffNo();

        CoListofvalue oldActivityStatusLov = atActivity.getActivityStatus();
        String oldActivityStatus = oldActivityStatusLov.getLovName();
        BigDecimal pendingAcceptLovId = AssignmentSingleton.getLovIdPendingAccept();
        CoListofvalue pendingAcceptActivityStatus = databaseService.getCoListofvalue(pendingAcceptLovId);
        if (pendingAcceptActivityStatus == null) {
            log.error(activityId + " assignedActivityStatus LOV not found lovId : " + pendingAcceptLovId);
        }
        atActivity.setAssignTo(assignTo);
        atActivity.setActivityStatus(pendingAcceptActivityStatus);
        databaseService.updateActivity(atActivity);

        AtStatusLog atStatusLog = new AtStatusLog();

        atStatusLog.setActivityId(activityId);
        atStatusLog.setDescription("Auto Assign Accept Pending - " + staffNo);
        atStatusLog.setLogDatetime(new Date());
        atStatusLog.setNewStatus(pendingAcceptActivityStatus.getLovName());
        atStatusLog.setOldStatus(oldActivityStatus);
        atStatusLog.setTicketId(ticketId);
        databaseService.insertAtStatusLog(atStatusLog);
        databaseService.updatePendingAcceptDateTime(atActivity);
        databaseService.addPendingAcceptCount(atActivity, PENDING_ASSIGN.equals(oldActivityStatus));
        databaseService.updatePendingAcceptMaxed(atActivity, "FALSE");
        boolean isNotified = new AutoAssignMessagingService().sendTaskAcceptanceMessage(assignTo, atActivity);
        log.info(activityId + " updated with status = " + pendingAcceptActivityStatus.getLovName());
    }

}
