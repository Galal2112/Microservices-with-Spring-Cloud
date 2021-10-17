package htwb.ai.songsservice.controller;


import htwb.ai.songsservice.common.Songs;
import htwb.ai.songsservice.entity.Song;
import htwb.ai.songsservice.service.SongService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.PersistenceException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/songs")
@AllArgsConstructor
public class SongController {

    private final SongService service;

    @PostMapping(consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> createSong(@RequestBody Song song) {
        try {
            Song savedSong = service.saveSong(song);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", "/songs/" + savedSong.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<Iterable<Song>> getAllSongs() {
        try {
            return new ResponseEntity<>(service.getAllSongs(), HttpStatus.OK);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(produces = "application/xml")
    public ResponseEntity<Songs> getAllSongsXML() {
        try {
            return new ResponseEntity<>(new Songs(service.getAllSongs()), HttpStatus.OK);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "/{id}", produces = {"application/json",
            "application/xml"})
    public ResponseEntity<Song> getSongById(
            @PathVariable(value = "id") Integer id) {
        try {
            Optional<Song> song = service.findSongById(id);
            if (song.isPresent()) {
                return new ResponseEntity<>(song.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Song> deleteSong(@PathVariable(value = "id") Integer id) {
        try {
            if (service.deleteSong(id)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Song> updateSong(@PathVariable(value = "id") Integer id, @RequestBody Song songUpdate) {
        try {
            if (service.updateSong(id, songUpdate)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
