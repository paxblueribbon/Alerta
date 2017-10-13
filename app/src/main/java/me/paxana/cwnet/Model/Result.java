package me.paxana.cwnet.Model;

/**
 * Created by paxie on 10/5/17.
 */

public class Result {
    private String mTitle;
    private String mYear;
    private String mImageURL;
    private String mImdbID;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String imageURL) {
        mImageURL = imageURL;
    }

    public String getImdbID() {
        return mImdbID;
    }

    public void setImdbID(String imdbID) {
        mImdbID = imdbID;
    }
}
