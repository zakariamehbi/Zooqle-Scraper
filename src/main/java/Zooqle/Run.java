package Zooqle;

import Global.*;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Run {
    static Document doc = null;
    static final String PREFIX = "https://zooqle.com";
    static final String MOVIES_PAGE = "https://zooqle.com/mov/?tg=0&v=t&age=any&s=dt&sd=d&pg=";

    /**
     * Used to get all the movies on a listing page, extract the url, imdb id, quality of each movie and return an array list.
     *
     * @param page
     * @return
     */
    public static ArrayList<Movie> getAllMoviesOfPage(int page) {
        ArrayList<Movie> moviesArray = new ArrayList<>();
        doc = Jsoup.getDocument(MOVIES_PAGE + page);
        Elements moviesList = doc.select("#movpanel > div.panel-body > table > tbody > tr");

        for (Element movie : moviesList) {
            try {
                String movieUrl = movie.select("td:nth-child(3) > a").first().attr("href");
                String movieImdbId = movie.select("td:nth-child(4) > span > a").first().attr("href");
                String movieQuality = movie.select("td:nth-child(5) > span").text();

                Movie myMovie = new Movie(Misc.getNumbersOnly(movieImdbId), PREFIX, movieUrl, movieQuality);
                moviesArray.add(myMovie);
            } catch (Exception e) {
                // System.out.println("No imdbId");
            }
        }

        return moviesArray;
    }

    /**
     * Every movie page have a list of torrents, this method return them all.
     *
     * @param torrentsUrl
     * @return
     */
    public static Elements getAllTorrentsOfMoviePage(String torrentsUrl) {
        doc = Jsoup.getDocument(torrentsUrl);
        return doc.select("#body_container > div > div.col-md-9 > div.panel.zq-panel > div > table > tbody > tr");
    }

    /**
     * Return the number of pages we containing movies
     *
     * @return
     */
    public static int getTotalAvailablePages() {
        doc = Jsoup.getDocument(MOVIES_PAGE + 1);
        String totalPages = doc.select("#movpanel > div.panel-body > ul > li:nth-child(7) > a > span").first().text();
        return Misc.getNumbersOnly(totalPages);
    }

    public static void main(String[] args) {
        /**
         * First we get the numbers of pages containing movies and we print the result.
         */
        int totalAvailablePages = getTotalAvailablePages();
        System.out.println("Total available pages: " + totalAvailablePages);

        /**
         * We loop through every page that contains movies
         */
        for (int page = 1; page <= totalAvailablePages; page++) {
            /**
             * moviesArray contain all the movies of a specific page with details (url, imdb id, quality)
             */
            ArrayList<Movie> moviesArray = getAllMoviesOfPage(page);
            System.out.println("Actual page id: " + page);
            System.out.println("Actual page url: " + MOVIES_PAGE + page + "\n");

            /**
             * We loop through every movie in our moviesArray
             */
            for (Movie movie : moviesArray) {
                System.out.println("---------- Start Movie " + movie.getImdbId() + " ----------");

                /**
                 * We ask our website if the movie already exist in our database.
                 * If the get request return the movie, we change his type from String to a Json object.
                 *     We check if the quality of the movie is the same compared to the torrent in our DB.
                 *          If true
                 *              We skip this movie
                 *          Else
                 *              We check if the last update is older than a day.
                 *                  If true
                 *                      We check that we don't work on the same torrent in our DB.
                 *                          If true
                 *                              We download & update the DB.
                 *                          Else
                 *                              We skip this movie
                 * Else
                 *     We continue
                 */
                try {
                    String response = Unirest.get("http://localhost:8000/api/v1/Movie/" + movie.getImdbId());
                    JSONObject jsonResponse = Misc.fromStringToJson(response);

                    String torrentQuality = jsonResponse.getString("torrent_quality");
                    float daysAfterLastUpdate = Misc.getDifferenceBetweenTwoStringDateInDays(jsonResponse.getString("updated_at"), Misc.getDateOfTheDay());

                    System.out.println("Movie " + movie.getImdbId() + " already exist in the DB, in " + torrentQuality + ", days after the last update : " + daysAfterLastUpdate);

                    /**
                     * If the movie in the DB have the same quality of the movie in the website source, we skip it.
                     */
                    int movieQualitySourceId = Qualities.valueOf("_" + movie.getQuality()).getQualityId();
                    int movieQualityDBId = Qualities.valueOf("_" + torrentQuality).getQualityId();

                    System.out.println("Compare: " + movie.getQuality() + " (Source) with " + torrentQuality + " (DB)");

                    if (movieQualityDBId == 4) {
                        System.out.println("--> We already have a 720p movie in the DB, no need to update.");
                        System.out.println("---------- End Movie " + movie.getImdbId() + " ----------");
                        System.out.println("");
                        continue;
                    } else if (movieQualitySourceId == movieQualityDBId) {
                        System.out.println("--> The quality of the source and the DB are the same, no need to update.");
                        System.out.println("---------- End Movie " + movie.getImdbId() + " ----------");
                        System.out.println("");
                        continue;
                    } else if (movieQualitySourceId < movieQualityDBId) {
                        System.out.println("--> The quality of the source is lower than DB, no need to update.");
                        System.out.println("---------- End Movie " + movie.getImdbId() + " ----------");
                        System.out.println("");
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("Movie " + movie.getImdbId() + " doesn't exist in the DB.");
                }

                System.out.println(movie.getUrl());

                Elements torrentsList;
                int movieQualityId;
                boolean retry = false;

                try {
                    System.out.println("Movie " + movie.getImdbId() + " quality as define by the source is: " + movie.getQuality());
                    movieQualityId = Qualities.valueOf("_" + movie.getQuality()).getQualityId();

                } catch (Exception e) {
                    System.out.println("Movie " + movie.getImdbId() + " doesn't have a define quality.");
                    movieQualityId = 0;
                }

                /**
                 * We only want the 720p torrent
                 */
                if (movieQualityId > 4) {
                    movieQualityId = 4;
                    retry = true;
                }

                do {
                    String torrentsListUrl = movie.getUrl() + "?v=t&tg=" + movieQualityId;
                    torrentsList = getAllTorrentsOfMoviePage(torrentsListUrl);

                    if (retry && torrentsList.isEmpty()) {
                        if (movieQualityId == 4) {
                            movieQualityId = 5;
                        } else {
                            movieQualityId = 3;
                            retry = false;
                        }
                    } else
                        movieQualityId--;

                    System.out.println(torrentsListUrl);
                }
                while (torrentsList.isEmpty());

                if (torrentsList.isEmpty()) {
                    System.out.println("No torrents found!");
                } else {
                    try {
                        String torrentPageUrl = PREFIX + torrentsList.select("td.text-nowrap.text-trunc > a").first().attr("href");
                        String torrentDownloadUrl = PREFIX + "/download/" + torrentPageUrl.substring(torrentPageUrl.lastIndexOf('-') + 1).replace(".html", ".torrent");
                        String torrentFileSize = torrentsList.select("tr > td:nth-child(3) > div > div").first().text();
                        String torrentFileQuality = torrentsList.select("td.text-nowrap.text-trunc > div > span.text-nowrap").first().text();

                        System.out.println(torrentPageUrl);

                        /**
                         * We send the data to the webservice.
                         */
                        String body = "imdb_id=" + movie.getImdbId() +
                                "&movie_url=" + movie.getUrl() +
                                "&torrent_url= " + torrentDownloadUrl +
                                "&torrent_size=" + torrentFileSize +
                                "&torrent_quality=" + torrentFileQuality;

                        System.out.println("Webservice response status: " + Unirest.post("http://localhost:8000/api/v1/Movie", body));
                    } catch (Exception e) {
                        System.out.println("Problem with the selected torrent OR the webservice.");
                    }
                }

                System.out.println("---------- End Movie " + movie.getImdbId() + " ----------\n");
            }
        }
    }
}