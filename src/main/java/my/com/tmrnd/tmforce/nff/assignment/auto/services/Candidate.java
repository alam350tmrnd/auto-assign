/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import my.com.tmrnd.tmforce.common.db.entity.CoResources;

/**
 *
 * @author Alam
 */
public class Candidate {
    private CoResources coResources;
    private Integer inHandCount=0;
    private Integer yesterdayInHandCount=0;
    private Double distance;

    public Candidate() {
    }
    
    public String toString(){
        return "icNo="+coResources.getIcNo()+",inHandCount="+inHandCount+",yesterdayInHandCount="+yesterdayInHandCount+",distance="+distance;
    }

    public Candidate(CoResources coResources, Integer inHandCount) {
        this.coResources = coResources;
        this.inHandCount = inHandCount;
    }

    /**
     * @return the coResources
     */
    public CoResources getCoResources() {
        return coResources;
    }

    /**
     * @param coResources the coResources to set
     */
    public void setCoResources(CoResources coResources) {
        this.coResources = coResources;
    }

    /**
     * @return the inHandCount
     */
    public Integer getInHandCount() {
        return inHandCount;
    }

    /**
     * @param inHandCount the inHandCount to set
     */
    public void setInHandCount(Integer inHandCount) {
        this.inHandCount = inHandCount;
    }

    /**
     * @return the yesterdayInHandCount
     */
    public Integer getYesterdayInHandCount() {
        return yesterdayInHandCount;
    }

    /**
     * @param yesterdayInHandCount the yesterdayInHandCount to set
     */
    public void setYesterdayInHandCount(Integer yesterdayInHandCount) {
        this.yesterdayInHandCount = yesterdayInHandCount;
    }

    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
}
