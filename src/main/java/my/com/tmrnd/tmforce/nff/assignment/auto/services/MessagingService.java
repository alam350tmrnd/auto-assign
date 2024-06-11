/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import my.com.tmrnd.tmforce.common.api.json.mobile_pusher.NotificationInfo;
import my.com.tmrnd.tmforce.common.client.MobilePusherClient;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoResourceSessionLog;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.entity.CoUserDeviceStatusLog;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alam
 */
public class MessagingService {

    MobilePusherClient mobilePusherClient;

    DatabaseService databaseService;

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    public MessagingService() {
        databaseService = DatabaseService.getDatabaseService();
        mobilePusherClient = new MobilePusherClient(ClientBuilder.newClient());
    }

    public boolean sendTaskAcceptanceMessage(CoResources coResources, AtActivity atActivity) {
        boolean result = false;

        String eventName = "task-accept";
        String message = "A new task (${ticketId}) been assigned to you (${staffNo}).";
        String activityId = atActivity.getActivityId();

        try {
            String icNo = coResources.getIcNo();
            String staffNo = coResources.getStaffNo();
            String ticketId = atActivity.getTicketId().getTicketId();

            String tokenId = getTokenId(coResources);
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

    public boolean sendTaskUnAcceptedMessageToSupervisor(CoResources superVisor, AtActivity atActivity, String pendingAcceptCount) {
        boolean result = false;

        String eventName = "task-unaccepted";
        String message = "Task ${ticketId} is not accepted by ${staffNo}. Assign attempt = ${pendingAcceptCount}";
        String staffNo = atActivity.getAssignTo().getStaffNo();

        String icNo = superVisor.getIcNo();
        String activityId = atActivity.getActivityId();
        String ticketId = atActivity.getTicketId().getTicketId();

        try {
            String tokenId = getTokenId(superVisor);
            if (tokenId != null) {
                //set additional info to send
                Map<String, Object> additionalInfoMap = new HashMap();
                additionalInfoMap.put("ticketId", ticketId);
                additionalInfoMap.put("activityId", activityId);
                additionalInfoMap.put("activityStatus", "Pending Accept");
                additionalInfoMap.put("pendingAcceptCount", pendingAcceptCount);
                NotificationInfo notification = new NotificationInfo();
                notification.setTitle("Task UnAccepted");
                notification.setBody(message.replace("${ticketId}", ticketId)
                        .replace("${staffNo}", staffNo)
                        .replace("${pendingAcceptCount}", pendingAcceptCount)
                );

                result = mobilePusherClient.push(eventName, tokenId, additionalInfoMap, notification);
                log.debug(activityId + " Task UnAccepted notification sent to : " + icNo);
            } else {
                log.info(activityId + "|" + icNo + " UpdateMobilePusher: Token Id is null");
                return false;
            }
        } catch (Exception e) {
            log.error(activityId + " error sendTaskUnAcceptedMessageToSupervisor", e);
        }
        return result;
    }

    public String getTokenId(CoResources coResources) {
        String tokenId = null;
        String icNo = coResources.getIcNo();

        CoUserDeviceStatusLog coUserDeviceStatusLog = databaseService.getCoUserDeviceStatusLog(icNo);
        tokenId = coUserDeviceStatusLog != null ? coUserDeviceStatusLog.getFbTokenId() : null;
        if (tokenId == null) {
            CoResourceSessionLog coResourceSessionLog = databaseService.getCoResourceSessionLogLatestPortalLogin(icNo);
            tokenId = coResourceSessionLog != null ? coResourceSessionLog.getFbTokenId() : null;
            if (tokenId != null) {
                log.debug("Found portal tokenId for {}", icNo);
            } else {
                log.debug("TokenId not found for {}", icNo);
            }
        } else {
            log.debug("Found mobile tokenId for {}", icNo);
        }

        return tokenId;
    }
}
