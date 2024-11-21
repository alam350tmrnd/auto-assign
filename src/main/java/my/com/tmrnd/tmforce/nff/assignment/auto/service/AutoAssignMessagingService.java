/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import my.com.tmrnd.tmforce.common.api.json.mobile_pusher.NotificationInfo;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.entity.custom.FGetListOfSupervisor;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.service.messaging.MessagingService;
import my.com.tmrnd.tmforce.nff.assignment.service.messaging.NotificationLevel;
import my.com.tmrnd.tmforce.nff.assignment.service.messaging.Receipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignMessagingService extends MessagingService {
    
    public static List<NotificationLevel> failedAcceptLevelList;
    public static List<NotificationLevel> lastFailedAcceptLevelList;
    

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    public boolean sendTaskAcceptanceMessageUnlogged(CoResources coResources, AtActivity atActivity) {
        boolean result = false;

        String eventName = "task-accept";
        String message = "A new task (${ticketId}) been assigned to you (${staffNo}).";
        String activityId = atActivity.getActivityId();

        try {
            String icNo = coResources.getIcNo();
            String staffNo = coResources.getStaffNo();
            String ticketId = atActivity.getTicketId().getTicketId();

            String tokenId = getTokenId(icNo);
            if (tokenId != null) {
                //set additional info to send
                Map<String, Object> additionalInfoMap = new HashMap();
                additionalInfoMap.put("ticketId", ticketId);
                additionalInfoMap.put("activityId", activityId);
                additionalInfoMap.put("activityStatus", "Pending Accept");

                NotificationInfo notification = new NotificationInfo();
                notification.setTitle("Task Acceptance");
                notification.setBody(message.replace("${ticketId}", ticketId).replace("${staffNo}", staffNo));

                NotificationInfo appNotification = new NotificationInfo();
                appNotification.setSound("alarm");
                additionalInfoMap.put("appNotification", appNotification);
                additionalInfoMap.put("expireAfter", "900");
                result = mobilePusherClient.push(eventName, tokenId, additionalInfoMap, notification);

            } else {
                log.info(activityId + "|" + icNo + " UpdateMobilePusher: Token Id is null");
                return false;
            }
        } catch (Exception e) {
            log.error(activityId + " error sendTaskAcceptanceMessage", e);
        }
        return result;
    }

    public boolean sendTaskAcceptanceMessage(CoResources coResources, AtActivity atActivity) {
        String icNo = coResources.getIcNo();
        
        String staffNo = coResources.getStaffNo();
        boolean result = sendTaskAcceptanceMessageUnlogged(coResources, atActivity);
        String status = (result ? "Success" : "Failed");
        String logMsg = status + ": Sending Task Acceptance notification to FE : " + staffNo;
        insertMessagingStatusLog(atActivity, icNo, logMsg);

        return result;
    }
    
      public boolean sendTaskUnAcceptedMessageToSupervisor(AtActivity atActivity,String pendingAcceptCount){
          log.info("sendTaskUnAcceptedMessageToSupervisor({})",atActivity.getActivityId());
          return AutoAssignMessagingService.this.sendTaskUnAcceptedMessageToSupervisor(atActivity, failedAcceptLevelList,pendingAcceptCount);
      }
      
      public boolean sendLastTaskUnAcceptedMessageToSupervisor(AtActivity atActivity,String pendingAcceptCount){
          log.info("sendLastTaskUnAcceptedMessageToSupervisor({})",atActivity.getActivityId());
          return AutoAssignMessagingService.this.sendTaskUnAcceptedMessageToSupervisor(atActivity, lastFailedAcceptLevelList, pendingAcceptCount);
      }

    public boolean sendTaskUnAcceptedMessageToSupervisor(AtActivity atActivity, List<NotificationLevel> notificationLevelList,String pendingAcceptCount) {
        CoResources assignTo = atActivity.getAssignTo();
        String icNo = assignTo.getIcNo();
        String staffNo = assignTo.getStaffNo();

        String activityId = atActivity.getActivityId();
        String ticketId = atActivity.getTicketId().getTicketId();


        if (notificationLevelList == null) {
            log.error("notificationLevelList is null. exit");
            return false;
        }
        List<Receipient> superVisorList = null;

        List<FGetListOfSupervisor> dbSupervisorList = databaseService.getSupervisorList(icNo);

        if (dbSupervisorList != null && !dbSupervisorList.isEmpty()) {
            superVisorList = getSuperVisorRecipientList(notificationLevelList, dbSupervisorList);

            if (superVisorList != null && !superVisorList.isEmpty()) {
                for (Receipient supervisor : superVisorList) {
                    String messageTemplate = "Task ${ticketId} is not accepted by ${staffNo}. Assign attempt = ${pendingAcceptCount}";
                    String message = messageTemplate.replace("${ticketId}", ticketId)
                            .replace("${staffNo}", staffNo)
                            .replace("${pendingAcceptCount}", pendingAcceptCount);

                    String status = (sendMessage(supervisor, "task-unaccepted", "Task UnAccepted", message) ? "Success" : "Failed");
                    String logMsg = status + ": Sending Un-accepted Task by "+staffNo+" notification to supervisor : " + supervisor.getStaffNo();
                    insertMessagingStatusLog(atActivity, icNo, logMsg);
                    log.info(logMsg);
                }

            }

        } else {
            log.debug("{} - {} supervisor not found", activityId, icNo);
        }

        return true;
    }

}
