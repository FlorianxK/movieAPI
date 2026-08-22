package com.dev.movieapi.controller;

import com.dev.movieapi.application.repositries.MovieService;
import com.dev.movieapi.db.dtos.MovieDto;
import com.dev.movieapi.db.dtos.MoviePageResponse;
import com.dev.movieapi.exceptions.EmptyFileException;
import com.dev.movieapi.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file,
                                                    @RequestPart String movieDto) throws IOException, EmptyFileException {
        if(file.isEmpty()){
            throw new EmptyFileException("file is empty");
        }
        MovieDto dto = convertMovieDto(movieDto);
        return new ResponseEntity<>(movieService.addMovie(dto,file), HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovie(movieId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMovieHandler(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId,
                                                       @RequestPart MultipartFile file,
                                                       @RequestPart String movieDtoObj) throws IOException {
        if(file.isEmpty()) file = null;
        MovieDto movieDto = convertMovieDto(movieDtoObj);
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }

    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse> getMoviesWithPage(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPage(pageNumber, pageSize));
    }

    @GetMapping("/allMoviesPageSort")
    public ResponseEntity<MoviePageResponse> getMoviesWithPageAndSorting(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPageAndSorting(pageNumber, pageSize, sortBy, sortDirection));
    }

    private MovieDto convertMovieDto(String movieDtoObject){
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(movieDtoObject, MovieDto.class);
    }
}
