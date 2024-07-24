/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto;

import my.com.tmrnd.tmforce.db.config.DbConfiguration;


/**
 *
 * @author Alam
 */
public class AutoAssignConfig extends DbConfiguration{


    private boolean logPayload;


    
    private static AutoAssignConfig config;
    

    /**
     * @return the logPayload
     */
    public boolean isLogPayload() {
        return logPayload;
    }

    /**
     * @param logPayload the logPayload to set
     */
    public void setLogPayload(boolean logPayload) {
        this.logPayload = logPayload;
    }

      
         /**
     * @return the config
     */
    public static AutoAssignConfig getConfig() {
        return config;
    }

    /**
     * @param aComposerConfig the config to set
     */
    public static void setConfig(AutoAssignConfig aComposerConfig) {
        config = aComposerConfig;
    }    
}
