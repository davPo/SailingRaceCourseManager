package com.aayaffe.sailingracecoursemanager.communication;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBEnvironment;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.location.request.SortField;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aayaffe on 22/09/2015.
 */
public class QuickBlox implements ICommManager {

    private static final String TAG = "QuickBlox";
    private Context c;
    private Resources r;
    Location otherLocation;
    public QuickBlox(Context c, Resources r){
        this.c = c;
        this.r = r;
    }


    @Override
    public int login(String user, String password, String nickname) {
        QBSettings.getInstance().fastConfigInit("28642", "GmfbggxbOfzby7R", "9hJert3mjFyZcKu");
        final QBUser u = new QBUser();
        u.setLogin(user);
        u.setPassword(password);
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle params) {

                QBUsers.signIn(u, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        // success
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Log.e(TAG, "Error Signing in");
                    }
                });

            }

            @Override
            public void onError(List<String> errors) {
                Log.e(TAG, "Error creating session");
            }
        });
        return 0;
    }

    @Override
    public int sendLoc(Location l) {
        double latitude = 32.9;
        double longitude = 34.9;
        String status = "Checked here!";
//
        final QBLocation location = new QBLocation(latitude, longitude, status);

        QBLocations.createLocation(location, new QBEntityCallbackImpl<QBLocation>() {
            @Override
            public void onSuccess(QBLocation qbLocation, Bundle args) {

                Log.d(TAG, "Success sending Loc");
            }

            @Override
            public void onError(List<String> errors) {
                Log.e(TAG, "Error sending Loc");
                for (String error: errors){
                    Log.e(TAG, error);
                }

            }
        });

        return 0;
    }

    @Override
    public Location getLoc(int id) {

        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setPerPage(10);
        getLocationsBuilder.setLastOnly();
        getLocationsBuilder.setHasStatus();
        getLocationsBuilder.setSort(SortField.CREATED_AT);
        QBLocations.getLocations(getLocationsBuilder, new QBEntityCallbackImpl<ArrayList<QBLocation>>() {
            @Override
            public void onSuccess(ArrayList<QBLocation> locations, Bundle params) {
                for(QBLocation l:locations){
                    Log.d(TAG,"Got Location of user: " + l.getUserId());
                    otherLocation = new Location("QuickBlox 5546553");
                    otherLocation.setLongitude(l.getLongitude());
                    otherLocation.setLatitude(l.getLatitude());
                }
            }

            @Override
            public void onError(List<String> errors) {

            }
        });



//        QBLocations.getLocation(7031588, new QBEntityCallbackImpl<QBLocation>() {
//            @Override
//            public void onSuccess(QBLocation qbLocation, Bundle args) {
//                Log.d(TAG, "getLocation Success" + qbLocation.toString());
//                otherLocation = new Location("QuickBlox 5546553");
//                otherLocation.setLongitude(qbLocation.getLongitude());
//                otherLocation.setLatitude(qbLocation.getLatitude());
//
//            }
//
//            @Override
//            public void onError(List<String> errors) {
//                Log.e(TAG, "Error getting Loc");
//                for (String error: errors){
//                    Log.e(TAG, error);
//                }
//            }
//        });
        return otherLocation;
    }

    @Override
    public int sendAction(RaceManagerAction a) {
        return 0;
    }
}
