package htwb.ai.songsservice.service;

import htwb.ai.songsservice.common.User;
import htwb.ai.songsservice.entity.*;
import htwb.ai.songsservice.repository.SongListRepository;
import htwb.ai.songsservice.repository.SongListSongsRepository;
import htwb.ai.songsservice.repository.SongRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SongListService {

    private final SongListRepository songListRepo;
    private final SongRepository songRepo;
    private final SongListSongsRepository songsRelationRepo;
    private final RestTemplate template;

    public Optional<SongList> findSongList(String authorization, Integer id) throws IllegalAccessException, IllegalArgumentException {
        User user = getUserUsingToken(authorization);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        Optional<SongList> songListOptional = songListRepo.findById(id);
        if (songListOptional.isPresent()) {
            SongList songList = songListOptional.get();
            if (songList.getAccessibility() == SongListAccessLevel.PRIVATE &&
                    !songList.getOwnerId().equals(user.getUserId())) {
                throw new IllegalAccessException();
            }
            songList.setPrivate(songList.getAccessibility() == SongListAccessLevel.PRIVATE);
            return Optional.of(songList);
        } else {
            return Optional.empty();
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public SongList saveSongList(String authorization, SongList songList) throws IllegalArgumentException {
        if (songList.getName() == null || songList.getName().isEmpty()) {
            throw new IllegalArgumentException();
        }
        User user = getUserUsingToken(authorization);
        if (user == null || songList.getId() != null) {
            throw new IllegalArgumentException();
        }

        List<Integer> songIds = songList.getLinkedSongs().stream().map(Song::getId).collect(Collectors.toList());
        int count = songRepo.countOfSongsForIds(songIds);
        if (count < songIds.size()) {
            throw new IllegalArgumentException();
        }
        songList.setOwnerId(user.getUserId());
        songList.setAccessibility(songList.isPrivate() ? SongListAccessLevel.PRIVATE : SongListAccessLevel.PUBLIC);
        List<Song> songsList = songList.getLinkedSongs();
        songList.setLinkedSongs(new ArrayList<>());
        SongList savedSongList = songListRepo.save(songList);

        List<SongListSongs> listSongs = songsList.stream().map(s -> {
            SongListSongsKey id = new SongListSongsKey(s.getId(), songList.getId());
            return new SongListSongs(id, songList, s);
        }).collect(Collectors.toList());
        songsRelationRepo.saveAll(listSongs);
        return savedSongList;
    }

    public boolean deleteSongList(String authorization, Integer id) throws IllegalAccessException, IllegalArgumentException {
        User user = getUserUsingToken(authorization);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        Optional<SongList> songListOptional = songListRepo.findById(id);
        if (songListOptional.isPresent()) {
            SongList songList = songListOptional.get();
            if (!songList.getOwnerId().equals(user.getUserId())) {
                throw new IllegalAccessException();
            }
            songListRepo.delete(songList);
            return true;
        } else {
            return false;
        }
    }

    public List<SongList> getSongLists(String authorization, String userId) throws IllegalArgumentException {
        User user = getUserUsingToken(authorization);
        if (user == null) {
            throw new IllegalArgumentException();
        }

        User owner = null;
        if (userId.equals(user.getUserId())) {
            owner = user;
        } else {
            owner = getUserUsingUserId(userId, authorization);
        }
        if (owner == null) {
            return null;
        }
        String ownerId = owner.getUserId();
        String userRequestedId = user.getUserId();
        List<SongListAccessLevel> accessLevels = ownerId.equals(userRequestedId) ?
                Arrays.asList(SongListAccessLevel.PRIVATE, SongListAccessLevel.PUBLIC)
                : Collections.singletonList(SongListAccessLevel.PUBLIC);
        List<SongList> songLists = songListRepo.findSongListsOfUser(userId, accessLevels);
        songLists.forEach(s -> s.setPrivate(s.getAccessibility() == SongListAccessLevel.PRIVATE));
        return songLists;
    }

    @Transactional(rollbackFor=Exception.class)
    public boolean updateSongList(String authorization, Integer id, SongList songListUpdate) throws IllegalAccessException, IllegalArgumentException {
        if (songListUpdate.getName() == null || songListUpdate.getName().isEmpty()
                || songListUpdate.getId() == null || !songListUpdate.getId().equals(id)) {
            throw new IllegalArgumentException();
        }
        User user = getUserUsingToken(authorization);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        Optional<SongList> songListOptional = songListRepo.findById(id);
        if (songListOptional.isPresent()) {
            SongList songList = songListOptional.get();
            if (!songList.getOwnerId().equals(user.getUserId())) {
                throw new IllegalAccessException();
            }

            List<Integer> songIds = songListUpdate.getLinkedSongs().stream().map(Song::getId).collect(Collectors.toList());
            int count = songRepo.countOfSongsForIds(songIds);
            if (count < songIds.size()) {
                throw new IllegalArgumentException();
            }
            songListUpdate.setId(id);
            songListUpdate.setOwnerId(user.getUserId());
            songListUpdate.setAccessibility(songListUpdate.isPrivate() ? SongListAccessLevel.PRIVATE : SongListAccessLevel.PUBLIC);
            songListRepo.save(songListUpdate);

            // update relation
            songsRelationRepo.deleteBySongListId(songListUpdate.getId());

            List<Song> songsList = songListUpdate.getLinkedSongs();
            List<SongListSongs> listSongs = songsList.stream().map(s -> {
                SongListSongsKey relationId = new SongListSongsKey(s.getId(), songListUpdate.getId());
                return new SongListSongs(relationId, songListUpdate, s);
            }).collect(Collectors.toList());
            songsRelationRepo.saveAll(listSongs);

            return true;
        } else {
            return false;
        }
    }

    private User getUserUsingToken(String token) {
        return template.getForObject("http://AUTH-SERVICE/auth?auth_token=" + token, User.class);
    }

    private User getUserUsingUserId(String userId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<User> response = template.exchange("http://AUTH-SERVICE/auth/" + userId, HttpMethod.GET, request, User.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
