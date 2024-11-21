/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.test.FacadeTest;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.db.facade.AtActivityFacade;
import my.com.tmrnd.tmforce.nff.assignment.db.facade.CoResourcesFacade;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author R10249
 */
public class AutoAssignMessagingServiceTest extends FacadeTest{
    
    public AutoAssignMessagingServiceTest() {
        
        DatabaseService databaseService = new DatabaseService(getSessionFactory());
        DatabaseService.setDatabaseService(databaseService);
        getSession().getTransaction().begin();
        AutoAssignService autoAssignService = new AutoAssignService();
        autoAssignService.setSingletonConfig();
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
         
    }
    
    @After
    public void tearDown() {
          getSession().getTransaction().commit();
    }

    /**
     * Test of sendTaskAcceptanceMessage method, of class AutoAssignMessagingService.
     */
    @Test
    public void testSendTaskAcceptanceMessage() {
        System.out.println("sendTaskAcceptanceMessage");
        CoResourcesFacade coResourcesFacade = new CoResourcesFacade(getSessionFactory());
        AtActivityFacade atActivityFacade = new AtActivityFacade(getSessionFactory());
        CoResources coResources = coResourcesFacade.find("840710145303");
        assertNotNull(coResources);
        AtActivity atActivity = atActivityFacade.find("A-0000010228");
        assertNotNull(atActivity);
        AutoAssignMessagingService instance = new AutoAssignMessagingService();
        boolean expResult = false;
        boolean result = instance.sendTaskAcceptanceMessage(coResources, atActivity);
      //  assertEquals(expResult, result);

    }

    /**
     * Test of sendTaskUnAcceptedMessageToSupervisor method, of class AutoAssignMessagingService.
     */
    //@Test
    public void testSendTaskUnAcceptedMessageToSupervisor() {
        System.out.println("sendTaskUnAcceptedMessageToSupervisor");
        AtActivityFacade atActivityFacade = new AtActivityFacade(getSessionFactory());
        AtActivity atActivity = atActivityFacade.find("A-0000008446");
        assertNotNull(atActivity);
        String pendingAcceptCount = "12";
        AutoAssignMessagingService instance = new AutoAssignMessagingService();
        boolean expResult = false;
        boolean result = instance.sendTaskUnAcceptedMessageToSupervisor(atActivity, pendingAcceptCount);
        assertEquals(expResult, result);

    }
    
}
