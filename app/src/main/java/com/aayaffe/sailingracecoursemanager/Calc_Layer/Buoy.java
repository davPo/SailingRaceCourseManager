package com.aayaffe.sailingracecoursemanager.Calc_Layer;

import android.location.Location;

import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.aayaffe.sailingracecoursemanager.geographical.GeoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Jonathan on 27/08/2016.
 */
public class Buoy {
    private String name;
    private AviLocation aviLocation;
    public String color = "Black";
    public Long lastUpdate;
    public long id;
    private UUID _uuid;
    private UUID _raceCourseUUID;
    private BuoyType buoyType; //replaces ObjectTypes

    public Buoy(){
        _uuid = UUID.randomUUID();
    }

    public Buoy(String name, AviLocation loc){
        _uuid = UUID.randomUUID();
        this.name=name;
        this.aviLocation=loc;
        this.lastUpdate = new Date().getTime();
        this.buoyType = BuoyType.Other;
    }
//    public Buoy(String name, AviLocation loc, String color){
//        _uuid = UUID.randomUUID();
//        this.name=name;
//        this.aviLocation=loc;
//        this.color=color;
//        this.lastUpdate = new Date().getTime();
//    }
    public Buoy(String name, AviLocation loc, BuoyType buoyType){
        _uuid = UUID.randomUUID();
        this.name=name;
        this.aviLocation=loc;
        this.buoyType=buoyType;
        this.lastUpdate = new Date().getTime();

        switch (buoyType){
            case FinishLine:
                this.color="Blue";
                break;
            case StartLine:
                this.color="Orange";
                break;
            case Gate:
                this.color="Yellow";
                break;
            case Buoy:
                this.color="Red";
                break;
        }
    }
    public Buoy(String name, AviLocation loc, String color, BuoyType buoyType){
        _uuid = UUID.randomUUID();
        this.name=name;
        this.aviLocation=loc;
        this.color=color;
        this.buoyType=buoyType;
        this.lastUpdate = new Date().getTime();
    }

    public String getName() {
        return name;
    }
    public void setAviLocation(AviLocation aviLocation) {
        this.aviLocation = aviLocation;
    }

    public AviLocation getAviLocation() {
        return aviLocation;
    }

    @Exclude
    public void setEnumBuoyType(BuoyType buoyType) {
        this.buoyType = buoyType;
    }
    @Exclude
    public BuoyType getEnumBuoyType() {
        return buoyType;
    }
    public String getType() {
        return buoyType.toString();
    }

    public void setType(String type) {
        this.buoyType = BuoyType.valueOf(type);
    }
    @Exclude
    public UUID getRaceCourseUUID() {
        return _raceCourseUUID;
    }
    @Exclude
    public UUID getUUID() {
        return _uuid;
    }

    public void setUuid(String uuid) {
        this._uuid = UUID.fromString(uuid);
    }
    public UUID get_raceCourseUUID() {
        return _raceCourseUUID;
    }

    public void set_raceCourseUUID(UUID _raceCourseUUID) {
        this._raceCourseUUID = _raceCourseUUID;
    }

    @Exclude
    public Date getLastUpdate() {
        return new Date(lastUpdate);
    }
    @Exclude
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate.getTime();
    }

    @Exclude
    public void setLoc(Location loc) {
        this.aviLocation = GeoUtils.toAviLocation(loc);
    }
    @Exclude
    public Location getLoc() {
        return GeoUtils.toLocation(aviLocation);
    }
    public LatLng getLatLng() {
        return GeoUtils.toLatLng(aviLocation);
    }


    public int getResourceId() {
        if(this.getEnumBuoyType() ==BuoyType.FlagBuoy||this.getEnumBuoyType() ==BuoyType.FinishLine||this.getEnumBuoyType() ==BuoyType.StartLine) {
            switch(this.color){
                case "Red":
                    return R.mipmap.flag_buoy_red;
                case "Blue":
                    return R.mipmap.flag_buoy_blue;
                case "Yellow":
                    return R.mipmap.flag_buoy_yellow;
                case "Orange":
                default:
                    return R.mipmap.flag_buoy_orange;
            }
        }
        else if(this.getEnumBuoyType() ==BuoyType.TomatoBuoy||this.getEnumBuoyType() ==BuoyType.Buoy||this.getEnumBuoyType() ==BuoyType.Gate) {
            switch(this.color) {
                case "Red":
                    return R.mipmap.tomato_buoy_red;
                case "Blue":
                    return R.mipmap.tomato_buoy_blue;
                case "Yellow":
                    return R.mipmap.tomato_buoy_yellow;
                case "Orange":
                default:
                    return R.mipmap.tomato_buoy_orange;
            }
        }
        else if(this.getEnumBuoyType() ==BuoyType.TriangleBuoy) {
            switch(this.color) {
                case "Red":
                    return R.mipmap.triangle_buoy_red;
                case "Blue":
                    return R.mipmap.triangle_buoy_blue;
                case "Yellow":
                    return R.mipmap.triangle_buoy_yellow;
                case "Orange":
                    return R.mipmap.triangle_buoy_orange;
                default:
                    return R.mipmap.triangle_buoy;

            }
        }
            else
            return R.mipmap.tomato_buoy_black_empty;
    }
}