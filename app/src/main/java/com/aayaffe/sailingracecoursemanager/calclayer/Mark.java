package com.aayaffe.sailingracecoursemanager.calclayer;

import android.util.Log;

import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jonathan on 23/07/2016.
 */
public class Mark {
    private static final String TAG = "Mark";
    private String name;
    private int direction = 0;
    private double distance = 0;
    private boolean distanceFactor = false;  //to multiply the distance with Dist2m1 or not?
    public List<Mark> referedMarks;
    private boolean isGatable = false;
    private String gateType = "BUOY";  //TODO make it enum.
    private int gateDirection = -90; //satellite direction from Main buoy OR port side direction from starboard side
    private double gateDistance = 0;  //distance between gate's buoys

    public Mark(){
        //Empty constructor for Firebase
    }
    public Mark(String name) {
        this.name = name;
        referedMarks = new ArrayList<>();
    }
    public void setName(String name) {
        if (name != null)
            this.name = name;
        else Log.w(TAG, "null name set for Mark named:" + this.name);
    }
    public String getName() {
        return name;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * direction from reference point. clockwise(usually minus, as a result);
     * @param direction
     */
    public void setDirection(String direction) {
        if (direction != null)
            this.direction = Integer.parseInt(direction);
        else Log.w(TAG, "null direction set - Mark named:" + this.name);
    }
    public int getDirection() {
        return direction;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setDistance(String distance) {
        if (distance != null)
            this.distance = Double.parseDouble(distance);
        else Log.w(TAG, "null distance set - Mark named:" + this.name);
    }
    public double getDistance() {
        return distance;
    }
    public void setDistanceFactor(boolean distanceFactor) {
        this.distanceFactor = distanceFactor;
    }
    public boolean getDistanceFactor(){
        return distanceFactor;
    }
    public double getAbsDistance(double multiplication) {
        if (distanceFactor)
            return multiplication*distance;
        return getDistance();
    }
    public void setDistaneFactor(String distanceFactor) {
        if (distanceFactor != null)
            this.distanceFactor = "true".equals(distanceFactor) || "always".equals(distanceFactor);
        else Log.w(TAG, "null relativeDistance set - Mark named:" + this.name);
    }
    @Exclude
    public boolean addReferedMark(Mark referedMark) {
        return referedMarks.add(referedMark);
    }
    public void setIsGatable(boolean isGatable) {
        this.isGatable = isGatable;
    }
    public List<Mark> getReferedMarks() {
        return referedMarks;
    }
    public boolean getIsGatable(){
        return isGatable;
    }
    public void setIsGatable(String isGatable) {
        if (isGatable != null)
            this.isGatable = "true".equals(isGatable) || "always".equals(isGatable);
        else Log.w(TAG, "null isGatable set - Mark named:" + this.name);
    }
    public void setGateDirection(int gateDirection) {
        this.gateDirection = gateDirection;
    }
    public void setGateDirection(String gateDirection) {
        if (gateDirection != null)
            this.gateDirection = Integer.parseInt(gateDirection);
        else Log.w(TAG, "null gateDirection set - Mark named:" + this.name);

    }
    public int getGateDirection() {
        return gateDirection;
    }
    public void setGateDistance(double gateDistance) {
        this.gateDistance = gateDistance;
    }
    public void setGateDistance(String gateDistance) {
        if (gateDistance != null)
            this.gateDistance = Double.parseDouble(gateDistance);
        else Log.w(TAG, "null gateWidth set - Mark named:" + this.name);
    }
    public double getGateDistance() {
        return gateDistance;
    }
    public void setGateType(String gateType) {
        this.gateType = gateType;
    }

    /**
     * referencePoint - the referencePoint location. NOT SIGNAL BOAT
     * multiplication - known also as dist2m1 (the distance toward the first mark)
     * windDir  -wind direction
     *
     * Each Mark is a tree root to marks that uses it's location, so this function must act recursively
     */
    public List<DBObject> parseBuoys(AviLocation referencePoint, double dist2m1, int windDir, float startLineLength, UUID raceCourseUUID) {  //parses the mark and his sons into buoys
        List<DBObject> buoys = new ArrayList<>();
        AviLocation location = new AviLocation(referencePoint, getDirection() + windDir, getAbsDistance(dist2m1));
        if (isGatable || "REFERENCE_POINT".equals(gateType)) {
            switch (gateType) {
                case "BUOY":  //adds a single buoy
                    buoys.add(new DBObject(this.getName(), location, BuoyType.BUOY, raceCourseUUID));
                    Log.i(TAG, "buoy added, gateType BUOY, name:" + this.getName());
                    break;
                case "GATE":
                    buoys.add(new DBObject(this.getName() + " S", new AviLocation(location, windDir - getGateDirection(), getGateDistance() / 2), BuoyType.GATE, raceCourseUUID));
                    buoys.add(new DBObject(this.getName() + " P", new AviLocation(location, windDir + getGateDirection(), getGateDistance() / 2), BuoyType.GATE, raceCourseUUID));
                    Log.i(TAG, "buoys added, gateType GATE, name:" + this.getName());
                    break;
                case "FINISH_LINE":
                    buoys.add(new DBObject(this.getName() + " S", new AviLocation(location, windDir + getGateDirection()-180, getGateDistance() / 2), BuoyType.FINISH_LINE, raceCourseUUID));
                    buoys.add(new DBObject(this.getName() + " P", new AviLocation(location, windDir + getGateDirection(), getGateDistance() / 2), BuoyType.FINISH_LINE, raceCourseUUID));
                    Log.i(TAG, "buoys added, gateType FINISH_LINE, name:" + this.getName());
                    break;
                case "START_LINE":
                    buoys.add(new DBObject(this.getName() + " S", new AviLocation(location, windDir - getGateDirection(), startLineLength / 2), BuoyType.START_LINE, raceCourseUUID));
                    buoys.add(new DBObject(this.getName() + " P", new AviLocation(location, windDir + getGateDirection(), startLineLength / 2), BuoyType.START_LINE, raceCourseUUID));
                    Log.i(TAG, "buoys added, gateType BUOY, name:" + this.getName());
                    break;
                case "SATELLITE":
                    buoys.add(new DBObject(this.getName(), location, BuoyType.BUOY, raceCourseUUID));
                    buoys.add(new DBObject(this.getName() + "a", new AviLocation(location, windDir + getGateDirection(), getGateDistance()), BuoyType.TRIANGLE_BUOY, raceCourseUUID));
                    Log.i(TAG, "buoys added, gateType SATELLITE, name:" + this.getName());
                    break;
                case "REFERENCE_POINT":
                    break;
                default:
                    buoys.add(new DBObject(this.getName(), location,BuoyType.OTHER,raceCourseUUID));
                    Log.e(TAG, "gateType not recognized. default buoy added. failed at" + gateType);
            }
        } else {
            buoys.add(new DBObject(this.getName(), location, BuoyType.BUOY, raceCourseUUID));
            Log.i(TAG, "buoy added (non-gatable), gateType BUOY, name:" + this.getName());
        }

        //parseChildren
        for (int i = 0; i < this.getReferedMarks().size(); i++) {
            Mark child = this.getReferedMarks().get(i);
            buoys.addAll(child.parseBuoys(location, dist2m1, windDir,startLineLength, raceCourseUUID));
        }
        return buoys;
    }

}
