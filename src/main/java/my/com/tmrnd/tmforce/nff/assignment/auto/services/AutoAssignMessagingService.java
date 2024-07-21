/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import my.com.tmrnd.tmforce.common.api.json.mobile_pusher.NotificationInfo;
import my.com.tmrnd.tmforce.common.client.MobilePusherClient;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoResourceSessionLog;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.entity.CoUserDeviceStatusLog;
import my.com.tmrnd.tmforce.common.db.entity.custom.FGetListOfSupervisor;
import my.com.tmrnd.tmforce.nff.assignment.AssignmentSingleton;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.messaging.MessagingService;
import my.com.tmrnd.tmforce.nff.assignment.messaging.NotificationLevel;
import my.com.tmrnd.tmforce.nff.assignment.messaging.Receipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class AutoAssignMessagingService extends MessagingService {

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    public boolean sendTaskAcceptanceMessage(CoResources coResources, AtActivity atActivity) {
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
                //additionalInfoMap.put("Notification", notification);

                NotificationInfo appNotification = new NotificationInfo();
                appNotification.setSound("alarm");
                additionalInfoMap.put("appNotification", appNotification);

                result = mobilePusherClient.push(eventName, tokenId, additionalInfoMap, notification);
                log.debug(activityId + " Task Acceptancet notification sent to : " + icNo);
            } else {
                log.info(activityId + "|" + icNo + " UpdateMobilePusher: Token Id is null");
                return false;
            }
        } catch (Exception e) {
            log.error(activityId + " error sendTaskAcceptanceMessage", e);
        }
        return result;
    }

    public boolean sendTaskUnAcceptedMessageToSupervisor(AtActivity atActivity, String pendingAcceptCount) {
        CoResources assignTo = atActivity.getAssignTo();
        String icNo = assignTo.getIcNo();
        String staffNo = assignTo.getStaffNo();

        String activityId = atActivity.getActivityId();
        String ticketId = atActivity.getTicketId().getTicketId();

        List<NotificationLevel> notificationLevelList = AssignmentSingleton.getNotificationLevelList();

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
                    sendMessage(supervisor, "task-unaccepted", "Task UnAccepted", message);
                }

            }

        } else {
            log.debug("{} - {} supervisor not found", activityId, icNo);
        }

        return true;
    }


}
