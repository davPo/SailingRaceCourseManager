package com.aayaffe.sailingracecoursemanager.initializinglayer;
import android.content.Context;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.activities.GoogleMapsActivity;
import com.aayaffe.sailingracecoursemanager.calclayer.Mark;
import com.aayaffe.sailingracecoursemanager.calclayer.RaceCourse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonathan on 22/07/2016.
 */

/*
    the CourseParser is parsing the CourseTypes out of the xml file, sent by the server
 */
public class CourseXmlParser {
    private static final String TAG = "CourseXMLParser";

    private XmlPullParserFactory xmlFactory;
    private Context context;
    private String url;
    private List<RaceCourseDescriptor> raceCourseDescriptors;
    private XmlPullParser parser;

    public CourseXmlParser(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    public Mark parseMarks(Map<String, String> selectedOptions) {
        Mark result = new Mark("null");
        try {
            InputStream stream = context.getApplicationContext().getAssets().open(url);
            xmlFactory = XmlPullParserFactory.newInstance();
            parser = xmlFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);

            result =  getMarks(parser, selectedOptions);
            stream.close();
        } catch (Exception e) {
            Log.e(TAG,"Error parsing marks",e);

        }
        return result;
    }

    public List<RaceCourseDescriptor> parseCourseTypes(){
        try {
            InputStream stream = context.getApplicationContext().getAssets().open(url);
            xmlFactory = XmlPullParserFactory.newInstance();
            parser = xmlFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            raceCourseDescriptors = getCourseTypes(parser);
//            for (RaceCourseDescriptor rcd: raceCourseDescriptors){
//                GoogleMapsActivity.commManager.addRaceCourseDescriptor(rcd);
//            }
            stream.close();
        } catch (Exception e) {
            Log.e(TAG,"Error parsing course types",e);
        }
        return raceCourseDescriptors;
    }

    private Mark getMarks(XmlPullParser xmlPullParser, Map<String, String> selectedOptions) {
        int event;
        Mark referenceMark = new Mark("Reference Point"); //reference point is represented as a mark, who is the father of all marks.
        referenceMark.setGateType("REFERENCE_POINT");
        ArrayList<Mark> fathers = new ArrayList<>(); //preforms as a stack //to be able to add children to their father and know your location on the family tree. {grandfather("Reference Point"), father, son, ...)
        Mark currentMark = new Mark("nullMark/debug");  //if a mark named "nullMark" appears - it's a bug!
        boolean preReceiveMode = false;
        boolean receiveMode = false;


        try {
            event = xmlPullParser.getEventType();
            String valueHolder = "";
            String attributeHolder = "";
            String name = "";
            while (event != XmlPullParser.END_DOCUMENT) {
                if (xmlPullParser.getName() != null){
                    name = xmlPullParser.getName();  //name is the xml tag name
                }
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        fathers.add(referenceMark);  //the reference point is the father of all marks, so is the first to be used here.
                        break;
                    case XmlPullParser.START_TAG:
                        if (name.equals("Course") && safeAttributeValue("type").equals(selectedOptions.get("type"))) {
                            Log.i(TAG, "reached course type:"+safeAttributeValue("type"));
                            preReceiveMode = true;
                        } else if (name.equals("Legs") && preReceiveMode && safeAttributeValue("name").equals(selectedOptions.get("Legs"))) {
                            Log.i(TAG, "reached leg type:"+safeAttributeValue("name"));
                            receiveMode=true;
                        } else if (name.equals("Mark")&&receiveMode) {
                            currentMark = new Mark(safeAttributeValue("name")); //new mark
                            fathers.add(currentMark);  //son of his father
                            Log.i(TAG, "son no."+fathers.size()+" added, named "+currentMark.getName());
                        } else if(receiveMode&&name.equals("Distance")){
                            attributeHolder=safeAttributeValue("factor");
                        }
                        break;

                    case XmlPullParser.TEXT:
                        valueHolder = safeTextValue();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("Direction")) {
                            currentMark.setDirection(valueHolder);
                        } else if (name.equals("Distance")) {
                            currentMark.setDistance(valueHolder);
                            currentMark.setDistaneFactor(attributeHolder);
                        } else if (name.equals("GateType")) {
                            currentMark.setGateType(valueHolder);
                        }
                        else if (name.equals("GateDirection")) {
                            currentMark.setGateDirection(valueHolder);
                        }
                        else if (name.equals("GateDistance")) {
                            currentMark.setGateDistance(valueHolder);
                        }
                        else if (name.equals("isGatable")) {
                            currentMark.setIsGatable(valueHolder.equals("always")||safeIsGatable(selectedOptions, currentMark.getName()));
                        }
                        else if (name.equals("Mark")&&receiveMode) {
                            fathers.get(fathers.size() - 2).addReferedMark(fathers.get(fathers.size() - 1));  //attach a son to his father
                            fathers.remove(fathers.size() - 1);  //son have no more children, so no longer necessary here
                        }
                        else if(name.equals("Legs")&&receiveMode) {
                            Log.i(TAG, "leg done");
                            preReceiveMode = false;
                            receiveMode = false;
                        }
                        break;
                }
                if(name.equals("Legs")) {
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e(TAG,"Error getting marks",e);
        }
        Log.d(TAG, "returns reference point");
        return referenceMark;
    }

    private List<RaceCourseDescriptor> getCourseTypes(XmlPullParser xmlPullParser) {
        int event;
        List<RaceCourseDescriptor> raceCourseDescriptors = new ArrayList<>();
        List<RaceCourseLeg> raceCourseLegs = new ArrayList<>(); // this is not a Spaghetti! maybe Penne or other italian names.
        List<List<String>> options = new ArrayList<>();
        try {
            event = xmlPullParser.getEventType();
            String attributeHolder;
            String name;
            String valueHolder="0";
            while (event != XmlPullParser.END_DOCUMENT) {
                name = xmlPullParser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        switch (name){
                            case "Course":
                                options = new ArrayList<>();
                                raceCourseLegs = new ArrayList<>();
                                attributeHolder = safeAttributeValue("type");
                                raceCourseDescriptors.add(new RaceCourseDescriptor(attributeHolder));
                                break;
                            case "Legs":
                                raceCourseLegs.add(new RaceCourseLeg(xmlPullParser.getAttributeValue(null, "name")));
                                options = new ArrayList<>();
                                break;
                            case "Mark":  //check 'isGatable' deeply
                                if (safeAttributeValue("isGatable").equals("true")){  //the is an optional gate
                                        attributeHolder = xmlPullParser.getAttributeValue(null, "gateType");  //attributeHolder restarts
                                        List<String> gatable = new ArrayList<>(Arrays.asList("", "toggle"));
                                        if (attributeHolder != null)
                                            gatable.set(0, xmlPullParser.getAttributeValue(null, "name") + " " + attributeHolder);
                                        else
                                            gatable.set(0, xmlPullParser.getAttributeValue(null, "name") + " GATE");
                                        options.add(gatable);
                                    }
                                break;

                        }
                        break;
                    case XmlPullParser.TEXT:
                        valueHolder = xmlPullParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (name) {
                            case "Upwind":
                                raceCourseLegs.get(raceCourseLegs.size()-1).setCourseFactor(0, Double.parseDouble(valueHolder));
                                break;
                            case "Downwind":
                                raceCourseLegs.get(raceCourseLegs.size()-1).setCourseFactor(1, Double.parseDouble(valueHolder));
                                break;
                            case "Reach":
                                raceCourseLegs.get(raceCourseLegs.size()-1).setCourseFactor(2, Double.parseDouble(valueHolder));
                                break;
                            case "Legs":
                                raceCourseLegs.get(raceCourseLegs.size()-1).setOptions(options);
                                break;
                            case "Course":
                                raceCourseDescriptors.get(raceCourseDescriptors.size()-1).setRaceCourseLegs(raceCourseLegs);
                                break;
                        }
                        break;

                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e(TAG,"Error getCourseTypes",e);
        }
        return raceCourseDescriptors;
    }


    //Both methods don't let nulls to be parsed out of the .xml files
    private String safeAttributeValue(String keyName) {
        String value = parser.getAttributeValue(null, keyName);
        if (value != null)
            return value;
        Log.w(TAG, "null attribute for keyName: " + keyName);
        return "_";  // TODO: the '_' char is just for debug, remove before use.
    }
    private String safeTextValue() {
        String value = parser.getText();
        if (value != null)
            return value;
        Log.w(TAG, "null text found");
        return "_";  // TODO: the '_' char is just for debug, remove before use.
    }

    private boolean safeIsGatable(Map<String , String> selectedOptions, String name){
        boolean b1=false, b2=false;
        if(selectedOptions.containsKey(name+" GATE")) {
            b1=selectedOptions.get(name+" GATE").equals("true");
        }
        if(selectedOptions.containsKey(name+" SATELLITE")) {
            b2=selectedOptions.get(name+" SATELLITE").equals("true");
        }
        return b1||b2;
    }
}
