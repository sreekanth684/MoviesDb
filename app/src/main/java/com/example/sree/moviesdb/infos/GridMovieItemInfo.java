package com.example.sree.moviesdb.infos;

/**
 * Created by Sree on 11/22/15.
 */
public class GridMovieItemInfo {
    private String movieName;
    private String moviePosterUrl;
    private String movieId;

    public GridMovieItemInfo(String movieName, String movieUrl, String movieId) {
        this.movieName = movieName;
        if(movieUrl==null || "null".equalsIgnoreCase(movieUrl) ){
            this.moviePosterUrl = null;
        }else {
            this.moviePosterUrl = "http://image.tmdb.org/t/p/w154/" + movieUrl;
        }
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    @Override
    public String toString() {
        return "GridMovieItemInfo{" +
                "movieName='" + movieName + '\'' +
                ", moviePosterUrl='" + moviePosterUrl + '\'' +
                ", movieId='" + movieId + '\'' +
                '}';
    }
}
