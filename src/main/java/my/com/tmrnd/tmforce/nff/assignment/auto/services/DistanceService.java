/*
 *  This software is the confidential and proprietary information
 *  of Telekom Research & Development Sdn. Bhd.
 */
package my.com.tmrnd.tmforce.nff.assignment.auto.services;

import my.com.tmrnd.tmforce.common.db.entity.AtTicket;
import my.com.tmrnd.tmforce.common.db.entity.CoArea;
import my.com.tmrnd.tmforce.common.db.entity.CoResources;
import my.com.tmrnd.tmforce.common.db.entity.CoUserDeviceStatusLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author R10249
 */
public class DistanceService {

    private Logger log;
    private CoArea zone;
    private String id;

    public DistanceService(String id) {
        this.id = id;
        log = LoggerFactory.getLogger(getClass().getName() + " - "+id);
    }

    public boolean isTicketCoordinateOk(AtTicket atTicket) {

        try {
            Double.valueOf(atTicket.getLongitude());
            Double.valueOf(atTicket.getLatitude());
        } catch (Exception exception) {
            return false;
        }
        return true;

    }

    public Double getDistance(CoResources coResources, AtTicket atTicket) {
        Double distance = -1.0;
        Double ticketLongitude = 0.0;
        Double ticketLatitude = 0.0;
        Double resourceLongitude = 0.0;
        Double resourceLatitude = 0.0;

        try {
            ticketLongitude = Double.valueOf(atTicket.getLongitude());
            ticketLatitude = Double.valueOf(atTicket.getLatitude());

            CoUserDeviceStatusLog coUserDeviceStatusLog = coResources.getCoUserDeviceStatusLog();
            if (coUserDeviceStatusLog != null) {
                resourceLongitude = coUserDeviceStatusLog.getLongitude().doubleValue();
                resourceLatitude = coUserDeviceStatusLog.getLatitude().doubleValue();
            }

            distance = getGreatCircleDistance(resourceLongitude, resourceLatitude, ticketLongitude, ticketLatitude);
        } catch (Exception exception) {
            log.error("error getting distance " + coResources.getIcNo() + " " + atTicket.getTicketId(), exception);
        }

        return distance;
    }

    public static Double getGreatCircleDistance(Double fromLongitude, Double fromLatitude, Double toLongitude, Double toLatitude) {
        Double distance;
        if ((fromLatitude == null) || (fromLongitude == null)) {
            distance = Double.valueOf(-1);
            return distance;
        } else if ((Double.valueOf(fromLatitude) == 0) || (Double.valueOf(fromLongitude) == 0)) {
            distance = Double.valueOf(-1);
            return distance;
        }
        //  Convert Standpoint (Customer geo-location) Decimal Degree values to Radian
        Double spLat = fromLatitude * (Math.PI / 180);
        Double spLong = fromLongitude * (Math.PI / 180);

        //  Convert Forepoint (BumbleBee geo-location) Decimal Degree values to Radian
        Double fpLat = toLatitude * (Math.PI / 180);
        Double fpLong = toLongitude * (Math.PI / 180);

        Double x1, x2, y1, y2;

        //  Formula untuk kira jarak atas mukabumi.
        //  Rujuk http://en.wikipedia.org/wiki/Great-circle_distance
        //  Jarak  = sqrt(x1 + x2) / (y1 + y2)
        x1 = Math.pow(Math.cos(fpLat) * Math.sin(fpLong - spLong), 2);
        x2 = Math.pow(((Math.cos(spLat) * Math.sin(fpLat)) - (Math.sin(spLat) * Math.cos(fpLat) * Math.cos(fpLong - spLong))), 2);
        y1 = Math.sin(spLat) * Math.sin(fpLat);
        y2 = Math.cos(spLat) * Math.cos(fpLat) * Math.cos(fpLong - spLong);

        distance = Double.valueOf((double) (6371.01 * Math.atan(Math.sqrt(x1 + x2) / (y1 + y2))));

        if (distance < 0) {
            distance = distance * -1;
        }

        return distance;
    }
}
