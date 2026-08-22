package com.dev.movieapi.application.service;

import com.dev.movieapi.application.repositries.FileService;
import com.dev.movieapi.application.repositries.MovieRepository;
import com.dev.movieapi.application.repositries.MovieService;
import com.dev.movieapi.db.dtos.MovieDto;
import com.dev.movieapi.db.dtos.MoviePageResponse;
import com.dev.movieapi.exceptions.FileExistsException;
import com.dev.movieapi.exceptions.MovieNotFoundException;
import com.dev.movieapi.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${projects.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        boolean exists = Files.exists( Paths.get(path+ File.separator+file.getOriginalFilename()) );
        if(exists){
            throw new FileExistsException("File already exists!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(
                null,
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
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie with id " + movieId + " not found") );

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

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie with id " + movieId + " not found") );

        String fileName = mv.getPoster();
        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        movieDto.setPoster(fileName);

        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie updatedMovie = movieRepository.save(movie);

        String posterUrl = baseUrl + "/file/" + fileName;

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
    public String deleteMovie(Integer movieId) throws IOException {
        Movie mv = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie with id " + movieId + " not found") );
        Integer id = mv.getMovieId();

        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        movieRepository.delete(mv);

        return "Movie with id " + id + " deleted";
    }

    public MoviePageResponse createPage(Pageable pageable, Integer pageNumber,Integer pageSize){
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

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

        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return createPage(pageable, pageNumber, pageSize);
    }

    @Override
    public MoviePageResponse getAllMoviesWithPageAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return createPage(pageable, pageNumber, pageSize);
    }
}
