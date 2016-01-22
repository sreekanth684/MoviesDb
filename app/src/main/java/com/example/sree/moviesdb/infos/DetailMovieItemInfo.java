package com.example.sree.moviesdb.infos;

import java.util.ArrayList;

/**
 * Created by Sree on 12/4/15.
 */
public class DetailMovieItemInfo {
    private String movieId;
    private String originalTitle;
    private String moviePosterUrl;
    private String movieOverview;
    private String movieRating;
    private String movieReleaseDate;
    private ArrayList<TrailerInfo> movieTrailers;
    private ArrayList<ReviewInfo> movieReviews;

    public class TrailerInfo {
        private String trailerName;
        private String trailerSource;

        public String getTrailerName() {
            return trailerName;
        }

        public void setTrailerName(String trailerName) {
            this.trailerName = trailerName;
        }

        public String getTrailerSource() {
            return trailerSource;
        }

        public void setTrailerSource(String trailerSource) {
            this.trailerSource = trailerSource;
        }

        @Override
        public String toString() {
            return "TrailerInfo{" +
                    "trailerName='" + trailerName + '\'' +
                    ", trailerSource='" + trailerSource + '\'' +
                    '}';
        }
    }

    public class ReviewInfo {
        private String reviewAuthor;
        private String reviewContent;

        public String getReviewAuthor() {
            return reviewAuthor;
        }

        public void setReviewAuthor(String reviewAuthor) {
            this.reviewAuthor = reviewAuthor;
        }

        public String getReviewContent() {
            return reviewContent;
        }

        public void setReviewContent(String reviewContent) {
            this.reviewContent = reviewContent;
        }

        @Override
        public String toString() {
            return "ReviewInfo{" +
                    "reviewAuthor='" + reviewAuthor + '\'' +
                    ", reviewContent='" + reviewContent + '\'' +
                    '}';
        }
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = (moviePosterUrl == null || "null".equalsIgnoreCase(moviePosterUrl)) ? null : "http://image.tmdb.org/t/p/w92/" + moviePosterUrl;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public String getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(String movieRating) {
        this.movieRating = movieRating == null ? "" : movieRating + "/10";
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    public ArrayList<TrailerInfo> getMovieTrailers() {
        return movieTrailers;
    }

    public void setMovieTrailers(ArrayList<TrailerInfo> movieTrailers) {
        this.movieTrailers = movieTrailers;
    }

    public ArrayList<ReviewInfo> getMovieReviews() {
        return movieReviews;
    }

    public void setMovieReviews(ArrayList<ReviewInfo> movieReviews) {
        this.movieReviews = movieReviews;
    }

    @Override
    public String toString() {
        return "DetailMovieItemInfo{" +
                "movieId='" + movieId + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", moviePosterUrl='" + moviePosterUrl + '\'' +
                ", movieOverview='" + movieOverview + '\'' +
                ", movieRating='" + movieRating + '\'' +
                ", movieReleaseDate='" + movieReleaseDate + '\'' +
                ", movieTrailers=" + movieTrailers +
                ", movieReviews=" + movieReviews +
                '}';
    }

}
