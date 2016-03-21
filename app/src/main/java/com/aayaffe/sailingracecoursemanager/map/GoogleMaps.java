package com.aayaffe.sailingracecoursemanager.map;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.geographical.GeoUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.download.TileDownloadLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aayaffe on 04/10/2015.
 */
public class GoogleMaps implements GoogleMap.OnInfoWindowClickListener,OnMapReadyCallback {
    private static final String TAG = "OpenSeaMap";
    public GoogleMap mapView;
    private Context c;
    private Activity a;
    private LatLong lastTapLatLong;
    private SharedPreferences sp;
    private TileDownloadLayer downloadLayer;
    private HashMap<String, Marker> markers = new HashMap<>();
    private Marker lastOpenned = null;
    public DialogFragment df;

    public void Init(Activity a, Context c, SharedPreferences sp)
    {
        this.c =c;
        this.a = a;
        this.sp = sp;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity)a).getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        Location center = new Location("Manual");
        center.setLatitude(32.831653);
        center.setLongitude(35.019216);
        ZoomToMarks();
        setCenter(center);
        mapView.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Check if there is an open info window
                if (lastOpenned != null) {
                    // Close the info window
                    lastOpenned.hideInfoWindow();
                    // Is the marker the same marker that was already open
                    if (lastOpenned.equals(marker)) {
                        // Nullify the lastOpenned object
                        lastOpenned = null;
                        // Return so that the info window isn't openned again
                        return true;
                    }
                }
                // Open the info window for the marker
                marker.showInfoWindow();
                // Re-assign the last openned such that we can close it later
                lastOpenned = marker;
                // Event was handled by our code do not launch default behaviour.
                return true;
            }
        });
        mapView.setOnInfoWindowClickListener(this);
        mapView.setPadding(0,170,0,0);


    }


    public void setCenter(Location l) {
        setCenter(GeoUtils.toLatLong(l));
    }

    public void setCenter(LatLong ll){
        setCenter(GeoUtils.toLatLng(ll));
    }
    public void setCenter(LatLng ll){
        mapView.animateCamera(CameraUpdateFactory.newLatLng(ll));
    }

    public void setCenter(double lat, double lon) {
        setCenter(new LatLong(lat, lon));
    }

    public void setZoomLevel(int zoom){
        //mapView.
    }

    public void destroy(){

    }

    public Marker addMark(LatLng ll,float cog, String name,String caption, int ResourceID){
        if (ll==null) return null;
        try{
            if (markers.containsKey(name)){
                Marker m = markers.get(name);
                boolean infoWindows=m.isInfoWindowShown();

                m.setPosition(ll);
                m.setIcon(BitmapDescriptorFactory.fromResource(ResourceID));
                m.setSnippet(caption);
                m.setRotation(cog);
                if (infoWindows) {
                    m.showInfoWindow();
                }
                return m;
            }
            if (null != mapView) {
                Marker m = mapView.addMarker(new MarkerOptions().position(ll).title(name).snippet(caption).icon(BitmapDescriptorFactory.fromResource(ResourceID)));
                markers.put(name,m);
                return m;
            } else {
                Log.d(TAG, "mapView is null");
                return null;
            }
        }catch(Exception e)
        {
            Log.d(TAG,"Failed to add mark",e);
        }
        return null;
    }



    public void removeMark(Marker m){
        m.remove();
        GoogleMapsActivity.commManager.removeBueyObject(m.getTitle());
        markers.remove(m);
    }

    public void ZoomToMarks(){
        ArrayList<Marker> ms = new ArrayList<>(markers.values());
        if (ms.size()==1) {
            setCenter((ms.get(0).getPosition()));
            return;
        }
        if (ms.size()==0){
            return;
        }
        ZoomToBounds(new ArrayList<>(markers.values()));
    }
    public void ZoomToBounds(List<Marker> marks){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : marks) {
            builder.include(marker.getPosition());
        }
        try{
            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            //googleMap.moveCamera(cu);
            mapView.animateCamera(cu);
        } catch (Exception e){

        }
    }

    public LatLong getLastTapLatLong() {
        return lastTapLatLong;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "Plus Fab Clicked");
        try{
            String t = marker.getTitle();
            Pattern p = Pattern.compile("-?\\d+");
            Matcher m = p.matcher(t);
            m.find();
            long id = Integer.parseInt(m.group());
//        df = BuoyEditDialog.newInstance(id);
//        df.show(a.getFragmentManager(), "Edit_Buoy");
            if (marker.getTitle().contains("Buoy")){
                if (deleteMark){
                    removeMark(marker);
                    deleteMark = false;
                }
                else {
                    Toast.makeText(c, "Press the buoys info window again to delete.", Toast.LENGTH_SHORT).show();
                    deleteMark = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteMark = false;
                        }
                    }, 3 * 1000);
                }

            }}catch(Exception e){
            Log.d(TAG,"Failed on info click", e);
        }

    }
    private Boolean deleteMark = false;



    public void removeMark(long id) {
        removeMark(markers.get("Buoy" + id));

    }
}
