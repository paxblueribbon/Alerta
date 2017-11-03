package me.paxana.cwnet.Model;

import java.util.ArrayList;

/**
 * Created by paxie on 10/11/17.
 */

public class Trigger {

    private String mTriggerName;
    private int mTriggerVotesYes;
    private int mTriggerVotesTotal;
    private ArrayList<String> mCategory;
    private String mId;

    public Trigger(String triggerName, String id, int triggerVotesYes, int triggerVotesTotal) {
        mTriggerName = triggerName;
        mId = id;
        mTriggerVotesYes = triggerVotesYes;
        mTriggerVotesTotal = triggerVotesTotal;
    }

    public Trigger(String triggerName, String id, int triggerVotesYes, int triggerVotesTotal, ArrayList<String> category) {
        mTriggerName = triggerName;
        mTriggerVotesYes = triggerVotesYes;
        mTriggerVotesTotal = triggerVotesTotal;
        mCategory = category;
        mId = id;
    }

    public Trigger(){
    }

    public String getTriggerName() {
        return mTriggerName;
    }

    public void setTriggerName(String triggerName) {
        mTriggerName = triggerName;
    }

    public int getTriggerVotesYes() {
        return mTriggerVotesYes;
    }

    public void setTriggerVotesYes(int triggerVotesYes) {
        mTriggerVotesYes = triggerVotesYes;
    }

    public int getTriggerVotesTotal() {
        return mTriggerVotesTotal;
    }

    public void setTriggerVotesTotal(int triggerVotesTotal) { mTriggerVotesTotal = triggerVotesTotal; }

    public ArrayList<String> getCategory() {
        return mCategory;
    }

    public void setCategory(ArrayList<String> category) {
        mCategory = category;
    }


}
