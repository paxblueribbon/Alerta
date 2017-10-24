package me.paxana.cwnet.Model;

/**
 * Created by paxie on 10/7/17.
 */

public class Movie {

    private String imdbID;
    private String title;
    private String year;
    private String summary;
    private String mpaaRating;
    private String imdbRating;
    private String posterURL;

    public Movie(String imdbID) {
        this.imdbID = imdbID;
    }

    public Movie(String imdbID, String title) {
        this.imdbID = imdbID;
        this.title = title;
    }

    public Movie() {
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(String mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }
}
