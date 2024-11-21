/*
 * Developer: Nor Azuan Azis
 * Date: 05/05/2024
 * Change history
 * Date         |By             |Remarks
 * 
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.outbound;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import my.com.tmrnd.tmforce.common.APIConstant;
import my.com.tmrnd.tmforce.common.api.json.model.opr.nff.OprUpdateWorkOrderStatusRequest;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.AtEaiOutbound;
import my.com.tmrnd.tmforce.common.db.entity.AtTicket;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import static my.com.tmrnd.tmforce.nff.assignment.AssignmentConstant.ACTIVITY_STATUS.PENDING_ACCEPT;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.util.TimeUtil;
import org.slf4j.LoggerFactory;

/**
 *
 * @author R10212
 */
public class UpdateNEXT {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());

    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    String evBusinessEvent = "UpdateWorkOrderStatus";
    
    private DatabaseService databaseService;

    public UpdateNEXT(DatabaseService databaseService) {
this.databaseService = databaseService;

 
    }

    public int performUpdateNEXT(AtActivity atActivity,String note)
            throws Exception {

            OprUpdateWorkOrderStatusRequest updtWorkOrderStatusRequest = composeUpdateWorkOrderStatusRequest(atActivity,  note);
            String updateNEXTRequest = updtWorkOrderStatusRequest.toString();


        //trigger update to Next
        Boolean isUpdateSuccess = triggerUpdateToNext(atActivity, updateNEXTRequest);

        return 0;
    }

    private OprUpdateWorkOrderStatusRequest composeUpdateWorkOrderStatusRequest(AtActivity atActivity,String note) {
        
        AtTicket atTicket = atActivity.getTicketId();
        
        String ticketId = atTicket.getTicketId();
        String activityId = atActivity.getActivityId();
        String activityNumber = atActivity.getActivityNumber();
        String activityStatus = PENDING_ACCEPT;
        
        String staffNo = null;
        String staffName = null; 
        String phoneNo = null; 
        String mobileNo = null;
        
        if (atActivity.getAssignTo() != null) {
            CoResources assignTo = atActivity.getAssignTo();
            String icNo = assignTo.getIcNo();
            staffNo = assignTo.getStaffNo();
            staffName = assignTo.getName();
            phoneNo = databaseService.getResourcePhoneNumber(icNo);;
            mobileNo = databaseService.getResourceMobileNumber(icNo);
        }
        
        String nttNo = databaseService.getNttNo(ticketId);
        
        String pendingAcceptDatetime = TimeUtil.getTimeString(new Date(), "dd/MM/yyyy HH:mm");

        OprUpdateWorkOrderStatusRequest updtWorkOrderStatusRequest = new OprUpdateWorkOrderStatusRequest();

        updtWorkOrderStatusRequest.setMid(generateMid());
        updtWorkOrderStatusRequest.setBusinessEvent(evBusinessEvent);
        
        updtWorkOrderStatusRequest.setTicketId(ticketId);       
        
        updtWorkOrderStatusRequest.setNttNo(emptyIfNull(nttNo));
        
        updtWorkOrderStatusRequest.setActivityId(activityId);
        updtWorkOrderStatusRequest.setActivityNo(emptyIfNull(activityNumber));
        updtWorkOrderStatusRequest.setActivityStatus(activityStatus);
        updtWorkOrderStatusRequest.setAssignTo(emptyIfNull(staffNo));
        updtWorkOrderStatusRequest.setAssignToMobileNo(emptyIfNull(mobileNo));

        updtWorkOrderStatusRequest.setNotes(composeNote(ticketId, activityId, activityStatus, note, staffNo, staffName, phoneNo, mobileNo, pendingAcceptDatetime));

        log.info("UpdateWorkOrderStatus request= \n" + updtWorkOrderStatusRequest.toString());

        return updtWorkOrderStatusRequest;
    }
    
    String emptyIfNull(String value){
        return value == null ? "" : value;
    }
    

     private Boolean triggerUpdateToNext(AtActivity atActivity, String updtWorkOrderStatusRequest) {
        //get endpoint
        String endpointNEXT = databaseService.getUpdateWorkOrderStatusEndpoint();

        return insertIntoAtEaiOutbound(evBusinessEvent, endpointNEXT, updtWorkOrderStatusRequest, atActivity.getTicketId(), atActivity,
                "application/json", "POST", "NEW");
    }

    private Boolean insertIntoAtEaiOutbound(String eventName, String urlval, String reqStr, AtTicket atTicket, AtActivity atActivity,
            String payloadType, String method, String txStatus) {

        AtEaiOutbound atEaiOutbound = new AtEaiOutbound();
        atEaiOutbound.setTicketId(atTicket.getTicketId());
        atEaiOutbound.setActivityId(atActivity.getActivityId());
        atEaiOutbound.setEventName(eventName);
        atEaiOutbound.setEaiEndpoint(urlval);
        atEaiOutbound.setRequestPayload(reqStr);
        atEaiOutbound.setInsertDatetime(new Date());
        atEaiOutbound.setRequestContentType(payloadType);
        atEaiOutbound.setHttpMethod(method);
        atEaiOutbound.setTxStatus(txStatus);
        try {
            databaseService.insertAtEaiOutbound(atEaiOutbound);
            log.info("Inserting EaiOutbound.  EaiId= " + atEaiOutbound.getEaiId());
        } catch (Exception ex) {
            log.info("Create EaiOutbound failed",ex);
            return false;
        }
        return true;
    }


    private String composeNote(String ticketId, String activityId, String newStatus, String note, String staffNo, String staffName, String phoneNo, String mobileNo, String pendingAcceptDatetime) {

        StringBuilder noteToNEXT = new StringBuilder();
        noteToNEXT.append(" Ticket Id: ").append(ticketId);
        noteToNEXT.append(" Activity Id: ").append(activityId);
        noteToNEXT.append(" New status: ").append(newStatus);
        noteToNEXT.append(" Note: ").append(note);
        noteToNEXT.append("|");
        noteToNEXT.append(" Staff No: ").append(staffNo);
        noteToNEXT.append("|");
        noteToNEXT.append(" Staff Name: ").append(staffName);
        noteToNEXT.append("|");
        noteToNEXT.append(" Phone No: ").append(emptyIfNull(phoneNo));
        noteToNEXT.append("|");
        noteToNEXT.append(" Mobile No: ").append(emptyIfNull(mobileNo));
        noteToNEXT.append(" Pending Accept Datetime: ").append(pendingAcceptDatetime);

        return noteToNEXT.toString();
    }

    private String generateMid() {
        String _ivrPin = "";
        //UUID uniqueKey = UUID.randomUUID();
        //int key = (int) UUID.randomUUID().getLeastSignificantBits();
        Integer rand1 = 0;
        Integer rand2 = 0;
        Integer rand3 = 0;
        Random randomno = new Random();

        rand1 = randomno.nextInt(999999);
        rand2 = randomno.nextInt(999999);
        rand3 = randomno.nextInt(999999);
        _ivrPin = rand1.toString() + rand2.toString() + rand3.toString();

        return "TMF" + _ivrPin.substring(0, 12);
    }

}
