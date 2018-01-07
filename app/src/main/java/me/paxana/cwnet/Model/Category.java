package me.paxana.cwnet.Model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paxie on 10/30/17.
 */

public class Category {

    private String mCategoryName;
    private int mCategoryVotesYes;
    private int mCategoryVotesTotal;
    private ArrayList<Trigger> mTriggerList;

    public Category() {
    }

    public Category(String categoryName, int categoryVotesYes, int categoryVotesTotal, ArrayList<Trigger> triggerList) {
        mCategoryName = categoryName;
        mCategoryVotesYes = categoryVotesYes;
        mCategoryVotesTotal = categoryVotesTotal;
        mTriggerList = triggerList;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    public int getCategoryVotesYes() {
        return mCategoryVotesYes;
    }

    public void setCategoryVotesYes(int categoryVotesYes) {
        mCategoryVotesYes = categoryVotesYes;
    }

    public int getCategoryVotesTotal() {
        return mCategoryVotesTotal;
    }

    public void setCategoryVotesTotal(int categoryVotesTotal) {
        mCategoryVotesTotal = categoryVotesTotal;
    }

    public ArrayList<Trigger> getTriggerList() {
        return mTriggerList;
    }

    public void setTriggerList(ArrayList<Trigger> triggerList) {
        mTriggerList = triggerList;
    }

    public void addToTriggerList(Trigger newTrigger) {
        mTriggerList.add(newTrigger);
    }
}
