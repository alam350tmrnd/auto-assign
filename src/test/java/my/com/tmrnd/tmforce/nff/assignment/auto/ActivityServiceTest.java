/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto;

import java.math.BigDecimal;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.test.FacadeTest;
import my.com.tmrnd.tmforce.nff.assignment.auto.service.ActivityService;
import my.com.tmrnd.tmforce.nff.assignment.auto.service.AutoAssignService;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.db.facade.AtActivityFacade;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author R10249
 */
public class ActivityServiceTest extends FacadeTest{
    
    public ActivityServiceTest() {
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
    }

    /**
     * Test of assignActivity method, of class ActivityService.
     */
    @Test
    public void testAssignActivity() {
        System.out.println("assignActivity");
        String activityId = "A-0000004525";
        DatabaseService databaseService = new DatabaseService(getSessionFactory());
        DatabaseService.setDatabaseService(databaseService);
        AutoAssignService autoAssignService = new AutoAssignService();
        
        AtActivityFacade facade = new AtActivityFacade(getSessionFactory());
        getSession().getTransaction().begin();
        autoAssignService.setSingletonConfig();
        
        AtActivity atActivity = facade.find(activityId);
        assertNotNull(atActivity);
        
        CoArea zone = new CoArea(BigDecimal.ZERO);
        ActivityService instance = new ActivityService(databaseService, zone, activityId);
        instance.assignActivity(atActivity);
        getSession().getTransaction().commit();
    }

    /**
     * Test of processPendingAccept method, of class ActivityService.
     */
    //@Test
    public void testProcessPendingAccept() {
        System.out.println("processPendingAccept");
        AtActivity atActivity = null;
        ActivityService instance = null;
        boolean expResult = false;
        boolean result = instance.processPendingAccept(atActivity);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of assignResource method, of class ActivityService.
     */
    //@Test
    public void testAssignResource() {
        System.out.println("assignResource");
        AtActivity atActivity = null;
        CoResources assignTo = null;
        ActivityService instance = null;
        instance.assignResource(atActivity, assignTo);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
