package me.paxana.cwnet.Model;

/**
 * Created by paxie on 10/11/17.
 */

public class Trigger {

    private String mTriggerName;
    private int mTriggerVotesYes;
    private int mTriggerVotesTotal;

    public Trigger(String triggerName, int triggerVotesYes, int triggerVotesTotal) {
        mTriggerName = triggerName;
        mTriggerVotesYes = triggerVotesYes;
        mTriggerVotesTotal = triggerVotesTotal;
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

    public void setTriggerVotesTotal(int triggerVotesTotal) {
        mTriggerVotesTotal = triggerVotesTotal;
    }
}
