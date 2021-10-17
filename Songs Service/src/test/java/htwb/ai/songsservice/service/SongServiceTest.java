package htwb.ai.songsservice.service;

import htwb.ai.songsservice.entity.Song;
import htwb.ai.songsservice.repository.SongListSongsRepository;
import htwb.ai.songsservice.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SongServiceTest {
    private SongRepository songRepository;
    private SongListSongsRepository songsRelationRepo;
    private SongService songService;

    @BeforeEach
    public void setup() {
        songRepository = Mockito.mock(SongRepository.class);
        songsRelationRepo = Mockito.mock(SongListSongsRepository.class);
        songService = new SongService(songRepository, songsRelationRepo);
    }

    @Test
    void saveSong() {
        Song song = Mockito.mock(Song.class);
        when(song.getTitle()).thenReturn("Test title");
        songService.saveSong(song);
        verify(songRepository).save(song);
    }

    @Test
    void saveSongWithoutTitleShouldThrowException() {
        Song song = Mockito.mock(Song.class);
        assertThrows(IllegalArgumentException.class, () -> {
            songService.saveSong(song);
        });
    }

    @Test
    void getAllSongs() {
        songService.getAllSongs();
        verify(songRepository).findAll();
    }

    @Test
    void findSongById() {
        int id = 10;
        songService.findSongById(id);
        verify(songRepository).findById(id);
    }

    @Test
    void deleteExistingSong() {
        int id = 10;
        when(songRepository.findById(id)).thenReturn(Optional.of(Mockito.mock(Song.class)));
        boolean success = songService.deleteSong(id);
        assertTrue(success);
        verify(songRepository).findById(id);
    }

    @Test
    void deleteNonExistingSong() {
        int id = 10;
        when(songRepository.findById(id)).thenReturn(Optional.empty());
        boolean success = songService.deleteSong(id);
        assertFalse(success);
        verify(songRepository).findById(id);
    }

    @Test
    void updateSong() {
        int id = 10;
        Song song = Mockito.mock(Song.class);
        when(song.getTitle()).thenReturn("Test title");
        when(song.getId()).thenReturn(id);
        when(songRepository.findById(id)).thenReturn(Optional.of(song));
        boolean success = songService.updateSong(id, song);
        assertTrue(success);
        verify(songRepository).findById(id);
        verify(songRepository).save(song);
    }

    @Test
    void updateSongNonExistingSong() {
        int id = Integer.MAX_VALUE;
        Song song = Mockito.mock(Song.class);
        when(song.getTitle()).thenReturn("Nonexisting title");
        when(song.getId()).thenReturn(id);
        when(songRepository.findById(id)).thenReturn(Optional.empty());
        boolean success = songService.updateSong(id, song);
        assertFalse(success);
        verify(songRepository).findById(id);
        verify(songRepository, never()).save(song);
    }

    @Test
    void updateSongWithMismatchedIdShouldThrowException() {
        int id = 10;
        Song song = Mockito.mock(Song.class);
        when(song.getTitle()).thenReturn("Test title");
        when(song.getId()).thenReturn(id);
        when(songRepository.findById(id)).thenReturn(Optional.of(song));
        assertThrows(IllegalArgumentException.class, () -> {
            songService.updateSong(id + 1, song);
        });
        verify(songRepository, never()).save(song);
    }

    @Test
    void updateSongWithoutTitleShouldThrowException() {
        int id = 10;
        Song song = Mockito.mock(Song.class);
        when(song.getId()).thenReturn(id);
        assertThrows(IllegalArgumentException.class, () -> {
            songService.updateSong(id, song);
        });
        verify(songRepository, never()).save(song);
    }
}