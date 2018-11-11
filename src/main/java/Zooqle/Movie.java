package Zooqle;

public class Movie {
    private int imdbId;
    private String url;
    private String quality;

    public Movie(int imdbId, String url, String quality) {
        this.imdbId = imdbId;
        this.url = url;
        this.quality = quality;
    }

    public Movie(int imdbId, String prefix, String url, String quality) {
        this.imdbId = imdbId;
        this.url = prefix + url;
        this.quality = quality;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImdbId() {
        return imdbId;
    }

    public void setImdbId(int imdbId) {
        this.imdbId = imdbId;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String toString() {
        return this.url + " - " + this.imdbId + " - " + this.quality;
    }
}
