/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author R10249
 */
public class CandidateSorter {
      private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
     List<Candidate> getLeastInHandCountList(List<Candidate> candidateList) {
         log.debug("getLeastInHandCountList({})",candidateList.size());
        candidateList.sort((Candidate o1, Candidate o2) -> o1.getInHandCount().compareTo(o2.getInHandCount()));
        Integer leastInHand = candidateList.get(0).getInHandCount();
        List<Candidate> leastList = new ArrayList<>();

        for (Candidate candidate : candidateList) {
            if (candidate.getInHandCount() == leastInHand) {
                leastList.add(candidate);
            } else {
                break;
            }
        }

        return leastList;
    }
    
    List<Candidate> getLeastYesterdayCountList(List<Candidate> candidateList) {
        log.debug("getLeastYesterdayCountList({})",candidateList.size());
        candidateList.sort((Candidate o1, Candidate o2) -> o1.getYesterdayInHandCount().compareTo(o2.getYesterdayInHandCount()));
        Integer leastInHand = candidateList.get(0).getYesterdayInHandCount();
        List<Candidate> leastList = new ArrayList<>();

        for (Candidate candidate : candidateList) {
            if (candidate.getYesterdayInHandCount() == leastInHand) {
                leastList.add(candidate);
            } else {
                break;
            }
        }

        return leastList;
    }
    
    List<Candidate> getLeastDistanceList(List<Candidate> candidateList) {
        log.debug("getLeastDistanceList({})",candidateList.size());
        
        for (Candidate candidate : candidateList) {
            if (candidate.getDistance() == null) {
                candidate.setDistance(999999.99);
            }
        }

        
        candidateList.sort((Candidate o1, Candidate o2) -> o1.getDistance().compareTo(o2.getDistance()));
        Double leastDistance = candidateList.get(0).getDistance();
        List<Candidate> leastList = new ArrayList<>();

        for (Candidate candidate : candidateList) {
            Double distance = candidate.getDistance();
            if (distance > 0.0 && distance == leastDistance) {
                leastList.add(candidate);
            } else {
                break;
            }
        }

        return leastList;
    }
}
