package me.paxana.cwnet.Model;

import java.util.ArrayList;

/**
 * Created by paxie on 10/11/17.
 */

public class Trigger {

    private String mTriggerName;
    private ArrayList<String> mCategory;
    private String mId;

    public Trigger(String triggerName, ArrayList<String> category, String id) {
        mTriggerName = triggerName;
        mCategory = category;
        mId = id;
    }

    public Trigger() {
    }

    public Trigger(String name, String testString, int i, int i1, ArrayList<String> testCatList){
    }

    public String getTriggerName() {
        return mTriggerName;
    }

    public void setTriggerName(String triggerName) {
        mTriggerName = triggerName;
    }

    public ArrayList<String> getCategory() {
        return mCategory;
    }

    public void setCategory(ArrayList<String> category) {
        mCategory = category;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
}
