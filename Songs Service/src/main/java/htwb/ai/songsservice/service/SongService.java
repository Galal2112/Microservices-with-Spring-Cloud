package htwb.ai.songsservice.service;

import htwb.ai.songsservice.entity.Song;
import htwb.ai.songsservice.repository.SongListSongsRepository;
import htwb.ai.songsservice.repository.SongRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SongService {

    private final SongRepository repository;
    private final SongListSongsRepository songsRelationRepo;

    public Song saveSong(Song song) throws IllegalArgumentException {
        if (song.getTitle() == null || song.getTitle().isEmpty()) {
            throw new IllegalArgumentException();
        }
        return repository.save(song);
    }

    public List<Song> getAllSongs() {
        return repository.findAll();
    }

    public Optional<Song> findSongById(Integer id) {
        return repository.findById(id);
    }

    public boolean deleteSong(Integer id) {
        Optional<Song> songOptional = repository.findById(id);
        if (songOptional.isPresent()) {
            Song song = songOptional.get();
            songsRelationRepo.deleteBySongId(song.getId());
            repository.delete(song);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateSong(Integer id, Song song) throws IllegalArgumentException {
        if (song.getTitle() == null || song.getTitle().isEmpty()
                || song.getId() == null || !song.getId().equals(id)) {
            throw new IllegalArgumentException();
        }
        Optional<Song> songOptional = repository.findById(id);
        if (songOptional.isPresent()) {
            Song dbSong = songOptional.get();
            dbSong.setTitle(song.getTitle());
            dbSong.setArtist(song.getArtist());
            dbSong.setLabel(song.getLabel());
            dbSong.setReleased(song.getReleased());
            repository.save(dbSong);
            return true;
        } else {
            return false;
        }
    }
}
