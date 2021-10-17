package htwb.ai.songsservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import htwb.ai.songsservice.common.SongLists;
import htwb.ai.songsservice.entity.Song;
import htwb.ai.songsservice.entity.SongList;
import htwb.ai.songsservice.entity.SongListAccessLevel;
import htwb.ai.songsservice.service.SongListService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class SongListControllerTest {

    private MockMvc mockMvc;
    private SongListService serviceMock;

    @BeforeEach
    public void setup() {
        serviceMock = Mockito.mock(SongListService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SongListController(serviceMock)).build();
    }

    @Test
    void userGetHisListsShouldReturnAllLists() throws Exception {
        String authToken = "authToken";
        String userId = "userId";
        List<SongList> expectedSongLists = new ArrayList<>(
                Arrays.asList(
                        dummySongList(1, userId, 2, SongListAccessLevel.PUBLIC),
                        dummySongList(2, userId, 3, SongListAccessLevel.PRIVATE))
        );
        when(serviceMock.getSongLists(authToken, userId)).thenReturn(expectedSongLists);
        ResultActions resultActions = mockMvc.perform(get("/songs/playlists?userId=" + userId)
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonMapper jsonMapper = new JsonMapper();
        List<SongList> songLists = jsonMapper.readValue(contentAsString, new TypeReference<List<SongList>>() {
        });
        Assertions.assertEquals(expectedSongLists.size(), songLists.size());
        Optional<SongList> privateSongListOptional = songLists.stream().filter(SongList::isPrivate).findFirst();
        Assertions.assertTrue(privateSongListOptional.isPresent());

        Optional<SongList> publicSongListOptional = songLists.stream().filter(s -> !s.isPrivate()).findFirst();
        Assertions.assertTrue(publicSongListOptional.isPresent());
    }

    @Test
    void getUnknownUserListsShouldReturn404Error() throws Exception {
        when(serviceMock.getSongLists(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(get("/songs/playlists?userId=notRegistered")
                .header("Authorization", "authToken"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userGetHisXMLListsShouldReturnAllLists() throws Exception {
        String authToken = "authToken";
        String userId = "userId";
        List<SongList> expectedSongLists = new ArrayList<>(
                Arrays.asList(
                        dummySongList(1, userId, 2, SongListAccessLevel.PUBLIC),
                        dummySongList(2, userId, 3, SongListAccessLevel.PRIVATE))
        );
        when(serviceMock.getSongLists(authToken, userId)).thenReturn(expectedSongLists);
        ResultActions resultActions = mockMvc.perform(get("/songs/playlists?userId=" + userId)
                .accept(MediaType.APPLICATION_XML)
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        XmlMapper xmlMapper = new XmlMapper();
        SongLists response = xmlMapper.readValue(contentAsString, SongLists.class);
        List<SongList> songLists = response.songLists;
        Assertions.assertEquals(expectedSongLists.size(), songLists.size());
        Optional<SongList> privateSongListOptional = songLists.stream().filter(SongList::isPrivate).findFirst();
        Assertions.assertTrue(privateSongListOptional.isPresent());

        Optional<SongList> publicSongListOptional = songLists.stream().filter(s -> !s.isPrivate()).findFirst();
        Assertions.assertTrue(publicSongListOptional.isPresent());
    }

    @Test
    void userGetHisPrivateListShouldReturnOk() throws Exception {
        String authToken = "authToken";
        int songListId = 1;
        SongList expected = dummySongList(songListId, "userId", 3, SongListAccessLevel.PRIVATE);
        when(serviceMock.findSongList(authToken, songListId)).thenReturn(Optional.of(expected));
        ResultActions resultActions = mockMvc.perform(get("/songs/playlists/" + songListId)
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonMapper jsonMapper = new JsonMapper();
        SongList songList = jsonMapper.readValue(contentAsString, SongList.class);
        Assertions.assertNotNull(songList);
        Assertions.assertEquals(expected.getName(), songList.getName());
        Assertions.assertEquals(expected.getLinkedSongs().size(), songList.getLinkedSongs().size());
    }

    @Test
    void userGetOtherUserPrivateListShouldReturnForbidden() throws Exception {
        when(serviceMock.findSongList(anyString(), anyInt())).thenThrow(new IllegalAccessException());
        mockMvc.perform(get("/songs/playlists/1")
                .header("Authorization", "authToken"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userGetOtherUserPublicListShouldReturnOk() throws Exception {
        String authToken = "authToken";
        int songListId = 1;
        SongList expected = dummySongList(songListId, "userId", 3, SongListAccessLevel.PUBLIC);
        when(serviceMock.findSongList(authToken, songListId)).thenReturn(Optional.of(expected));
        ResultActions resultActions = mockMvc.perform(get("/songs/playlists/" + songListId)
                .header("Authorization", authToken))
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        SongList songList = mapper.readValue(contentAsString, new TypeReference<SongList>() {
        });
        Assertions.assertNotNull(songList);
        Assertions.assertEquals(expected.getName(), songList.getName());
        Assertions.assertEquals(expected.getLinkedSongs().size(), songList.getLinkedSongs().size());
    }

    @Test
    void userPostSongListShouldReturnCreated() throws Exception {
        SongList expected = dummySongList(1, "userId", 2, SongListAccessLevel.PRIVATE);
        when(serviceMock.saveSongList(anyString(), any(SongList.class))).thenReturn(expected);
        mockMvc.perform(post("/songs/playlists")
                .header("Authorization", "userToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song0\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 2,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/songLists/" + expected.getId()));
    }

    @Test
    void userPostSongListWithNonExistingSongShouldReturnBadRequest() throws Exception {
        when(serviceMock.saveSongList(anyString(), any(SongList.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(post("/songs/playlists")
                .header("Authorization", "userToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": " + Integer.MAX_VALUE + ",\n" +
                        "        \"title\": \"SongUnkown\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userDeletePrivateListReturnNoContent() throws Exception {
        when(serviceMock.deleteSongList(anyString(), anyInt())).thenReturn(true);
        mockMvc.perform(delete("/songs/playlists/1")
                .header("Authorization", "userToken"))
                .andExpect(status().isNoContent());
    }

    @Test
    void userDeleteAnotherUserListReturnForbidden() throws Exception {
        when(serviceMock.deleteSongList(anyString(), anyInt())).thenThrow(new IllegalAccessException());
        mockMvc.perform(delete("/songs/playlists/1")
                .header("Authorization", "otherToken"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userDeleteNonExistingListReturnNotFount() throws Exception {
        when(serviceMock.deleteSongList(anyString(), anyInt())).thenReturn(false);
        mockMvc.perform(delete("/songs/playlists//" + Integer.MAX_VALUE)
                .header("Authorization", "userToken"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userPutSongListShouldReturnNoContent() throws Exception {
        when(serviceMock.updateSongList(anyString(), anyInt(), any(SongList.class))).thenReturn(true);
        mockMvc.perform(put("/songs/playlists/1")
                .header("Authorization", "userToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"id\": 1,\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song0\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 2,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isNoContent());
    }

    @Test
    void userPutSongListWithNonExistingSongShouldReturnBadRequest() throws Exception {
        when(serviceMock.updateSongList(anyString(), anyInt(), any(SongList.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(put("/songs/playlists/1")
                .header("Authorization", "userToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"id\": 1,\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": " + Integer.MAX_VALUE + ",\n" +
                        "        \"title\": \"SongUnkown\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userTryUpdatingOtherUserSongListShouldReturnForbidden() throws Exception {
        when(serviceMock.updateSongList(anyString(), anyInt(), any(SongList.class))).thenThrow(new IllegalAccessException());
        mockMvc.perform(put("/songs/playlists/1")
                .header("Authorization", "otherToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"id\": 1,\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song0\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 2,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userPutNonExistingSongListShouldReturnNotFound() throws Exception {
        when(serviceMock.updateSongList(anyString(), anyInt(), any(SongList.class))).thenReturn(false);
        mockMvc.perform(put("/songs/playlists/1")
                .header("Authorization", "userToken")
                .contentType("application/json")
                .content("{\n" +
                        " \"id\": " + Integer.MAX_VALUE + ",\n" +
                        " \"isPrivate\": true,\n" +
                        " \"name\": \"mmusterPrivate\",\n" +
                        " \"songList\": [\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"title\": \"Song0\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 2,\n" +
                        "        \"title\": \"Song1\"\n" +
                        "    }\n" +
                        " ]\n" +
                        " }"))
                .andExpect(status().isNotFound());
    }

    private SongList dummySongList(int id, String ownerId, int numOfSongs, SongListAccessLevel accessLevel) {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < numOfSongs; i ++) {
            Song s = new Song();
            s.setId(i);
            s.setTitle("Song" + i);
            songs.add(s);
        }
        SongList songList = new SongList();
        songList.setId(id);
        songList.setName(ownerId + " list - " + accessLevel);
        songList.setAccessibility(accessLevel);
        songList.setPrivate(accessLevel == SongListAccessLevel.PRIVATE);
        songList.setOwnerId(ownerId);
        songList.setLinkedSongs(songs);
        return songList;
    }
}