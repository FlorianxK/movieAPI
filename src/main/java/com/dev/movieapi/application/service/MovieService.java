package com.dev.movieapi.application.service;

import com.dev.movieapi.application.repositries.MovieRepository;
import com.dev.movieapi.application.repositries.MovieServiceRepository;
import com.dev.movieapi.db.dtos.MovieDto;
import com.dev.movieapi.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService implements MovieServiceRepository {

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${projects.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieService(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        String uploadedFileName = fileService.uploadFile(path, file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(
                movieDto.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie savedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        return new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie with id " + movieId + " not found") );

        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }

    @Override
    public List<MovieDto> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();

        for(Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();

            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    }
}
