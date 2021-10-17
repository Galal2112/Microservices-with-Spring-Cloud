package htwb.ai.songsservice.service;

import htwb.ai.songsservice.common.User;
import htwb.ai.songsservice.entity.*;
import htwb.ai.songsservice.repository.SongListRepository;
import htwb.ai.songsservice.repository.SongListSongsRepository;
import htwb.ai.songsservice.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class SongListServiceTest {

    private SongListService songListService;
    @Autowired
    private SongListRepository songListRepo;
    @Autowired
    private SongRepository songRepo;
    @Autowired
    private SongListSongsRepository songsRelationRepo;
    private RestTemplate restTemplate;

    private int mmusterPrivateListId;
    private int mmusterPublicListId;

    @BeforeEach
    public void setup() {
        songsRelationRepo.deleteAll();
        songListRepo.deleteAll();
        songRepo.deleteAll();
        restTemplate = Mockito.mock(RestTemplate.class);
        insertTestData();
        songListService = new SongListService(songListRepo, songRepo, songsRelationRepo, restTemplate);

        User mmuster = new User();
        mmuster.setUserId("mmuster");
        mmuster.setAuthToken("mmusterToken");
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth?auth_token=" + mmuster.getAuthToken(), User.class)).thenReturn(mmuster);

        User eschuler = new User();
        eschuler.setUserId("eschuler");
        eschuler.setAuthToken("eschulerToken");
        when(restTemplate.getForObject("http://AUTH-SERVICE/auth?auth_token=" + eschuler.getAuthToken(), User.class)).thenReturn(eschuler);

        setupRestTemplateResponseForUserId(mmuster, eschuler);
        setupRestTemplateResponseForUserId(eschuler, mmuster);
    }

    @Test
    void mmusterGetHisListsShouldReturnAllLists() throws Exception {
        List<SongList> songLists = songListService.getSongLists("mmusterToken", "mmuster");
        assertEquals(2, songLists.size());
        Optional<SongList> privateSongListOptional = songLists.stream().filter(SongList::isPrivate).findFirst();
        assertTrue(privateSongListOptional.isPresent());

        Optional<SongList> publicSongListOptional = songLists.stream().filter(s -> !s.isPrivate()).findFirst();
        assertTrue(publicSongListOptional.isPresent());
    }

    @Test
    void eschulerGet_mmusterListsShouldReturnOnlyPublicLists() throws Exception {
        List<SongList> songLists = songListService.getSongLists("eschulerToken", "mmuster");
        assertEquals(1, songLists.size());
        Optional<SongList> songListOptional = songLists.stream().findFirst();
        assertTrue(songListOptional.isPresent());
        assertFalse(songListOptional.get().isPrivate());
    }

    @Test
    void getUnknownUserListsShouldReturnNull() throws Exception {
        String notRegisteredId = "notRegistered";
        String authToken = "eschulerToken";
        ResponseEntity<User> userResponse = Mockito.mock(ResponseEntity.class);
        when(userResponse.getBody()).thenReturn(null);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        when(restTemplate.exchange("http://AUTH-SERVICE/auth/" + notRegisteredId, HttpMethod.GET, request, User.class)).thenReturn(userResponse);
        List<SongList> songLists = songListService.getSongLists(authToken, notRegisteredId);
        assertNull(songLists);
    }

    @Test
    void userGetHisPrivateList() throws Exception {
        Optional<SongList> songList = songListService.findSongList("mmusterToken", mmusterPrivateListId);
        assertTrue(songList.isPresent());
        assertEquals("mmuster-Private", songList.get().getName());
        assertEquals(2, songList.get().getLinkedSongs().size());
    }

    @Test
    void eschulerGetmmusterPrivateListShouldThrowException() throws Exception {
        assertThrows(IllegalAccessException.class, () -> {
            songListService.findSongList("eschulerToken", mmusterPrivateListId);
        });
    }

    @Test
    void eschulerGet_mmusterPublicList() throws Exception {
        Optional<SongList> songList = songListService.findSongList("eschulerToken", mmusterPublicListId);
        assertTrue(songList.isPresent());
        assertEquals("mmuster-Public", songList.get().getName());
        assertEquals(2, songList.get().getLinkedSongs().size());
    }

    @Test
    void mmusterGetListsWithInvalidTokenShouldThrowException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.getSongLists("unkownToken", "mmuster");
        });
    }

    @Test
    void mmusterCreateSongList() throws Exception {
        List<Song> songs = songRepo.findAll();
        SongList songList = new SongList();
        songList.setLinkedSongs(songs);
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        songList.setAccessibility(SongListAccessLevel.PRIVATE);
        int id = songListService.saveSongList("mmusterToken", songList).getId();
        Optional<SongList> dbSongListOptional = songListRepo.findById(id);
        assertTrue(dbSongListOptional.isPresent());
        SongList dbSongList = dbSongListOptional.get();
        assertEquals(songList.getOwnerId(), dbSongList.getOwnerId());
        assertEquals(songList.getName(), dbSongList.getName());
        assertEquals(songs.size(), dbSongList.getLinkedSongs().size());
    }

    @Test
    void mmusterCreateSongListWithNonExistingSongShouldThrowException() throws Exception {
        Song nonExistingSong = new Song();
        nonExistingSong.setTitle("nonExistingSong");
        nonExistingSong.setId(Integer.MAX_VALUE);
        SongList songList = new SongList();
        songList.setLinkedSongs(new ArrayList<>(Collections.singletonList(nonExistingSong)));
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.saveSongList("mmusterToken", songList).getId();
        });
    }

    @Test
    void mmusterCreateSongListWithInvalidTokenShouldThrowException() throws Exception {
        SongList songList = new SongList();
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        songList.setAccessibility(SongListAccessLevel.PRIVATE);
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.saveSongList("unkownToken", songList);
        });
    }

    @Test
    void mmusterDeleteHisList() throws Exception {
        List<Song> songs = songRepo.findAll();
        SongList songList = new SongList();
        songList.setLinkedSongs(songs);
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        songListRepo.save(songList);
        List<SongListSongs> relation = new ArrayList<>();
        songs.forEach(s -> {
            relation.add(new SongListSongs(new SongListSongsKey(s.getId(), songList.getId()), songList, s));
        });
        songsRelationRepo.saveAll(relation);
        boolean deletedSong = songListService.deleteSongList("mmusterToken", songList.getId());
        assertTrue(deletedSong);
        Optional<SongList> dbSongList = songListRepo.findById(songList.getId());
        assertTrue(dbSongList.isEmpty());
    }

    @Test
    void eschulerDelete_mmusterListShouldThrowException() throws Exception {
        assertThrows(IllegalAccessException.class, () -> {
            songListService.deleteSongList("eschulerToken", mmusterPrivateListId);
        });
    }

    @Test
    void eschulerDeleteNonExistingListShouleReturnFalse() throws Exception {
        boolean success = songListService.deleteSongList("eschulerToken", Integer.MAX_VALUE);
        assertFalse(success);
    }

    @Test
    void mmusterDeleteSongListWithInvalidTokenShouldThrowException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.deleteSongList("unkownToken", mmusterPrivateListId);
        });
    }

    @Test
    void userUpdateHisSongList() throws Exception {
        List<Song> songs = songRepo.findAll();
        SongList songList = new SongList();
        songList.setLinkedSongs(songs);
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        String newName = "My updated list";
        SongList tobeUpdated = songListRepo.save(songList);
        tobeUpdated.setLinkedSongs(songs.subList(0, 1));
        tobeUpdated.setName(newName);
        boolean success = songListService.updateSongList("mmusterToken", tobeUpdated.getId(), tobeUpdated);
        assertTrue(success);
        Optional<SongList> dbSongOptional = songListRepo.findById(tobeUpdated.getId());
        assertTrue(dbSongOptional.isPresent());
        SongList dbSong = dbSongOptional.get();
        assertEquals(newName, dbSong.getName());
    }

    @Test
    void userUpdateHisSongListWithMismatchedIdsShouldThrowException() throws Exception {
        SongList songList = new SongList();
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        SongList tobeUpdated = songListRepo.save(songList);
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.updateSongList("mmusterToken", tobeUpdated.getId() + 1, tobeUpdated);
        });
    }

    @Test
    void userUpdateNonExistingSongListShouldReturnFalse() throws Exception {
        SongList songList = new SongList();
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        songList.setId(Integer.MAX_VALUE);
        boolean success = songListService.updateSongList("mmusterToken", songList.getId(), songList);
        assertFalse(success);
    }

    @Test
    void userUpdateSongListWithNonExistingSongShouldThrowException() throws Exception {
        Song nonExistingSong = new Song();
        nonExistingSong.setTitle("nonExistingSong");
        nonExistingSong.setId(Integer.MAX_VALUE);
        SongList songList = new SongList();
        songList.setId(mmusterPublicListId);
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        songList.setLinkedSongs(new ArrayList<>(Collections.singletonList(nonExistingSong)));
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.updateSongList("mmusterToken", songList.getId(), songList);
        });
    }

    @Test
    void eschulerUpdate_mmusterSongListShouldThrowException() throws Exception {
        SongList songList = new SongList();
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        SongList tobeUpdated = songListRepo.save(songList);
        assertThrows(IllegalAccessException.class, () -> {
            songListService.updateSongList("eschulerToken", tobeUpdated.getId(), tobeUpdated);
        });
    }

    @Test
    void mmusterUpdateSongListWithInvalidTokenShouldThrowException() throws Exception {
        SongList songList = new SongList();
        songList.setOwnerId("mmuster");
        songList.setName("my Fav List");
        SongList tobeUpdated = songListRepo.save(songList);
        assertThrows(IllegalArgumentException.class, () -> {
            songListService.updateSongList("unknownToken", tobeUpdated.getId(), tobeUpdated);
        });
    }

    private void insertTestData() {
        // insert songs
        int songsCount = 8;
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < songsCount; i ++) {
            Song s = new Song();
            s.setTitle("Song" + i);
            songs.add(s);
        }
        songRepo.saveAll(songs);

        List<SongListSongs> mmusterLists = new ArrayList<>();

        List<Song> mmusterPrivateListSongs = songs.subList(0, 2);
        SongList mmusterPrivateSongList = new SongList();
        mmusterPrivateSongList.setName("mmuster-Private");
        mmusterPrivateSongList.setAccessibility(SongListAccessLevel.PRIVATE);
        mmusterPrivateSongList.setOwnerId("mmuster");
        mmusterPrivateSongList.setLinkedSongs(mmusterPrivateListSongs);
        songListRepo.save(mmusterPrivateSongList);
        mmusterPrivateListId = mmusterPrivateSongList.getId();
        mmusterPrivateListSongs.forEach(s -> {
            mmusterLists.add(new SongListSongs(new SongListSongsKey(s.getId(), mmusterPrivateSongList.getId()), mmusterPrivateSongList, s));
        });

        List<Song> mmusterPublicListSongs = songs.subList(2, 4);
        SongList mmusterPublicSongList = new SongList();
        mmusterPublicSongList.setName("mmuster-Public");
        mmusterPublicSongList.setAccessibility(SongListAccessLevel.PUBLIC);
        mmusterPublicSongList.setOwnerId("mmuster");
        mmusterPublicSongList.setLinkedSongs(mmusterPublicListSongs);
        songListRepo.save(mmusterPublicSongList);
        mmusterPublicListId = mmusterPublicSongList.getId();
        mmusterPublicListSongs.forEach(s -> {
            mmusterLists.add(new SongListSongs(new SongListSongsKey(s.getId(), mmusterPublicSongList.getId()), mmusterPublicSongList, s));
        });

        songsRelationRepo.saveAll(mmusterLists);

        List<SongListSongs> eschulerLists = new ArrayList<>();

        List<Song> eschulerPrivateListSongs = songs.subList(4, 6);
        SongList eschulerPrivateSongList = new SongList();
        eschulerPrivateSongList.setName("eschuler-Private");
        eschulerPrivateSongList.setAccessibility(SongListAccessLevel.PRIVATE);
        eschulerPrivateSongList.setOwnerId("eschuler");
        eschulerPrivateSongList.setLinkedSongs(eschulerPrivateListSongs);
        songListRepo.save(eschulerPrivateSongList);
        eschulerPrivateListSongs.forEach(s -> {
            eschulerLists.add(new SongListSongs(new SongListSongsKey(s.getId(), eschulerPrivateSongList.getId()), eschulerPrivateSongList, s));
        });

        List<Song> eschulerPublicListSongs = songs.subList(6, 8);
        SongList eschulerPublicSongList = new SongList();
        eschulerPublicSongList.setName("eschuler-Public");
        eschulerPublicSongList.setAccessibility(SongListAccessLevel.PUBLIC);
        eschulerPublicSongList.setOwnerId("eschuler");
        eschulerPublicSongList.setLinkedSongs(eschulerPublicListSongs);
        songListRepo.save(eschulerPublicSongList);
        eschulerPublicListSongs.forEach(s -> {
            eschulerLists.add(new SongListSongs(new SongListSongsKey(s.getId(), eschulerPublicSongList.getId()), eschulerPublicSongList, s));
        });

        songsRelationRepo.saveAll(eschulerLists);
    }

    private void setupRestTemplateResponseForUserId(User user, User response) {
        ResponseEntity<User> userResponse = Mockito.mock(ResponseEntity.class);
        when(userResponse.getBody()).thenReturn(response);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", user.getAuthToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        when(restTemplate.exchange("http://AUTH-SERVICE/auth/" + response.getUserId(), HttpMethod.GET, request, User.class)).thenReturn(userResponse);
    }
}