/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import my.com.tmrnd.tmforce.common.db.entity.AtActivity;
import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.test.FacadeTest;
import my.com.tmrnd.tmforce.nff.assignment.db.DatabaseService;
import my.com.tmrnd.tmforce.nff.assignment.db.facade.AtActivityFacade;
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
public class ActivityServiceTest extends FacadeTest {

    public ActivityServiceTest() {

        DatabaseService databaseService = new DatabaseService(getSessionFactory());
        DatabaseService.setDatabaseService(databaseService);
        getSession().getTransaction().begin();

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
      //  getSession().getTransaction().commit();
    }

    /**
     * Test of assignActivity method, of class ActivityService.
     */
    public void testAssignActivity() {
        System.out.println("assignActivity");
        AtActivity atActivity = null;
        ActivityService instance = null;
        instance.assignActivity(atActivity);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCandidateList method, of class ActivityService.
     */
    public void testGetCandidateList() {
        System.out.println("getCandidateList");
        List<CoResources> resourceList = null;
        AtActivity atActivity = null;
        Date plannedStart = null;
        Date plannedEnd = null;
        String shift = "";
        ActivityService instance = null;
        List<Candidate> expResult = null;
        List<Candidate> result = instance.getCandidateList(resourceList, atActivity, plannedStart, plannedEnd, shift);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of processPendingAccept method, of class ActivityService.
     */
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
    public void testAssignResource() {
        System.out.println("assignResource");
        AtActivity atActivity = null;
        CoResources assignTo = null;
        String method = "";
        ActivityService instance = null;
        instance.assignResource(atActivity, assignTo, method);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStaffNoIcNo method, of class ActivityService.
     */
    public void testGetStaffNoIcNo() {
        System.out.println("getStaffNoIcNo");
        CoResources coResources = null;
        ActivityService instance = null;
        String expResult = "";
        String result = instance.getStaffNoIcNo(coResources);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateNext method, of class ActivityService.
     */
    @Test
    public void testUpdateNext() {
        System.out.println("updateNextOnAssign");
        AtActivityFacade atActivityFacade = new AtActivityFacade(getSessionFactory());
        AtActivity activity = atActivityFacade.find("A-0000007873");
        String activityId = "A-0000007873";
        assertNotNull(activity);
        CoArea zone = new CoArea(BigDecimal.ZERO);
        ActivityService instance = new ActivityService(DatabaseService.getDatabaseService(), zone, activityId);
        instance.updateNextOnAssign(activity);

    }

    /**
     * Test of getOrdinal method, of class ActivityService.
     */
    public void testGetOrdinal() {
        System.out.println("getOrdinal");
        String numberStr = "";
        String expResult = "";
        String result = ActivityService.getOrdinal(numberStr);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
