package htwb.ai.lyricsservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import htwb.ai.lyricsservice.service.LyricsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/lyrics")
@AllArgsConstructor
public class LyricsController {

    private final LyricsService service;

    @GetMapping(produces = "text/plain")
    public ResponseEntity<String> getLyrics(@RequestHeader("Authorization") String authorization,
                                            @RequestParam(name = "songId") int songId) {
        try {
            return new ResponseEntity<>(service.getLyrics(songId, authorization), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
