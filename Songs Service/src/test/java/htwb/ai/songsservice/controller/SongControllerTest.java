package htwb.ai.songsservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import htwb.ai.songsservice.common.Songs;
import htwb.ai.songsservice.entity.Song;
import htwb.ai.songsservice.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SongControllerTest {
    private MockMvc mockMvc;
    private SongService serviceMock;

    @BeforeEach
    public void setup() {
        serviceMock = Mockito.mock(SongService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SongController(serviceMock)).build();
    }

    @Test
    void getSongsShouldReturnOKAndAllSongs() throws Exception {
        List<Song> expected = songList();
        when(serviceMock.getAllSongs()).thenReturn(expected);
        ResultActions resultActions = mockMvc.perform(get("/songs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<Song> response = mapper.readValue(contentAsString, new TypeReference<List<Song>>() {
        });
        assertEquals(expected.size(), response.size());
        for (int i = 0; i < response.size(); i++) {
            assertEquals(expected.get(i).getId(), response.get(i).getId());
            assertEquals(expected.get(i).getTitle(), response.get(i).getTitle());
            assertEquals(expected.get(i).getAlbum(), response.get(i).getAlbum());
            assertEquals(expected.get(i).getArtist(), response.get(i).getArtist());
            assertEquals(expected.get(i).getReleased(), response.get(i).getReleased());
        }
    }

    @Test
    void getXMLSongsShouldReturnOKAndAllSongs() throws Exception {
        List<Song> expected = songList();
        when(serviceMock.getAllSongs()).thenReturn(expected);
        ResultActions resultActions = mockMvc.perform(get("/songs").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        XmlMapper xmlMapper = new XmlMapper();
        Songs response = xmlMapper.readValue(contentAsString, Songs.class);
        assertEquals(expected.size(), response.songs.size());
        for (int i = 0; i < response.songs.size(); i++) {
            assertEquals(expected.get(i).getId(), response.songs.get(i).getId());
            assertEquals(expected.get(i).getTitle(), response.songs.get(i).getTitle());
            assertEquals(expected.get(i).getArtist(), response.songs.get(i).getArtist());
            assertEquals(expected.get(i).getReleased(), response.songs.get(i).getReleased());
        }
    }

    @Test
    void getSongShouldReturnOKAndSong() throws Exception {
        Song expected = songList().get(0);
        when(serviceMock.findSongById(expected.getId())).thenReturn(Optional.of(expected));

        mockMvc.perform(get("/songs/" + expected.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(jsonPath("$.title").value(expected.getTitle()))
                .andExpect(jsonPath("$.album").value(expected.getAlbum()))
                .andExpect(jsonPath("$.artist").value(expected.getArtist()))
                .andExpect(jsonPath("$.label").value(expected.getLabel()))
                .andExpect(jsonPath("$.released").value(expected.getReleased()));
    }

    @Test
    void getXMLSongShouldReturnOKAndSong() throws Exception {
        Song expected = songList().get(0);
        when(serviceMock.findSongById(expected.getId())).thenReturn(Optional.of(expected));

        mockMvc.perform(get("/songs/" + expected.getId()).accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("song/id").string(expected.getId().toString()))
                .andExpect(xpath("song/title").string(expected.getTitle()))
                .andExpect(xpath("song/album").string(expected.getAlbum()))
                .andExpect(xpath("song/artist").string(expected.getArtist()))
                .andExpect(xpath("song/released").string(expected.getReleased().toString()));
    }

    @Test
    void getSongShouldReturnNotFound() throws Exception {
        when(serviceMock.findSongById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/songs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postSongShouldReturnCreated() throws Exception {
        int id = 100;
        Song song = new Song();
        song.setId(id);
        when(serviceMock.saveSong(any())).thenReturn(song);
        mockMvc.perform(post("/songs")
                .contentType("application/json")
                .content("{ \"title\" : \"Can’t Stop the Feeling\", \"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/songs/" + id));
    }

    @Test
    void postSongWithoutTitleShouldReturnBadRequest() throws Exception {
        when(serviceMock.saveSong(any(Song.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(post("/songs")
                .contentType("application/json")
                .content("{ \"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSongReturnNoContent() throws Exception {
        Song expected = songList().get(0);
        when(serviceMock.deleteSong(expected.getId())).thenReturn(true);
        mockMvc.perform(delete("/songs/" + expected.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSongReturnNotFound() throws Exception {
        Song expected = songList().get(0);
        when(serviceMock.findSongById(expected.getId())).thenReturn(Optional.empty());
        mockMvc.perform(delete("/songs/" + expected.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void putSongShouldReturnCreated() throws Exception {
        Song song = songList().get(0);
        when(serviceMock.updateSong(anyInt(), any(Song.class))).thenReturn(true);
        mockMvc.perform(put("/songs/" + song.getId())
                .contentType("application/json")
                .content("{ \"id\" : " + song.getId() + ",\"title\" : \"Can’t Stop the Feeling\", \"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isNoContent());
    }

    @Test
    void putSongWithoutTitleShouldReturnBadRequest() throws Exception {
        int id = anyInt();
        when(serviceMock.updateSong(id, any(Song.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(put("/songs/" + id)
                .contentType("application/json")
                .content("{ \"id\": " + id + ",\"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putNonExistingSongShouldReturnNotFound() throws Exception {
        int id = 100;
        when(serviceMock.findSongById(id)).thenReturn(Optional.empty());
        mockMvc.perform(put("/songs/" + id)
                .contentType("application/json")
                .content("{ \"id\" : " + id + ", \"title\" : \"Can’t Stop the Feeling\", \"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putMismatchedIdShouldReturnBadRequest() throws Exception {
        int id = anyInt();
        when(serviceMock.updateSong(id, any(Song.class))).thenThrow(new IllegalArgumentException());
        mockMvc.perform(put("/songs/" + id)
                .contentType("application/json")
                .content("{ \"id\" : 2222, \"title\" : \"Can’t Stop the Feeling\", \"artist\" : \"Britney Spears\", \"album\" : \"Glory\", \"released\" : 2016 }"))
                .andExpect(status().isBadRequest());
    }

    private List<Song> songList() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("songs.json")) {
                return objectMapper.readValue(is, new TypeReference<List<Song>>() {
                });
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}