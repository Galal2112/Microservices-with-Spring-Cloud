package htwb.ai.songsservice.controller;

import htwb.ai.songsservice.common.SongLists;
import htwb.ai.songsservice.entity.SongList;
import htwb.ai.songsservice.service.SongListService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/songs/playlists")
@AllArgsConstructor
public class SongListController {

    private final SongListService service;

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<SongList>> getUserSongLists(@RequestHeader("Authorization") String authorization, @RequestParam(name = "userId") String userId) {
        return getUserSongLists(authorization, userId, songLists -> new ResponseEntity<>(songLists, HttpStatus.OK));
    }

    @GetMapping(produces = {"application/xml"})
    public ResponseEntity<SongLists> getUserSongListsXml(@RequestHeader("Authorization") String authorization, @RequestParam(name = "userId") String userId) {
        return getUserSongLists(authorization, userId, songLists -> new ResponseEntity<>(new SongLists(songLists), HttpStatus.OK));
    }

    @GetMapping(value = "/{id}", produces = {"application/json",
            "application/xml"})
    public ResponseEntity<SongList> getSongListById(@RequestHeader("Authorization") String authorization, @PathVariable(value = "id") Integer id) {

        try {
            Optional<SongList> songListOptional = service.findSongList(authorization, id);
            if (songListOptional.isPresent()) {
                return new ResponseEntity<>(songListOptional.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> createSongList(@RequestHeader("Authorization") String authorization, @RequestBody SongList songList) {

        try {
            SongList savedSongList = service.saveSongList(authorization, songList);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", "/songLists/" + savedSongList.getId());
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteSongList(@RequestHeader("Authorization") String authorization, @PathVariable(value = "id") Integer id) {

        try {
            if (service.deleteSongList(authorization, id)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<SongList> updateSong(@RequestHeader("Authorization") String authorization, @PathVariable(value = "id") Integer id, @RequestBody SongList songListUpdate) {
        try {
            if (service.updateSongList(authorization, id, songListUpdate)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private <T> ResponseEntity<T> getUserSongLists(String authorization, String userId, ISongListResponseConstructor<T> constructor) {
        try {
            List<SongList> songLists = service.getSongLists(authorization, userId);
            if (songLists == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return constructor.getResponse(songLists);
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private interface ISongListResponseConstructor<T> {
        ResponseEntity<T> getResponse(List<SongList> songLists);
    }
}
