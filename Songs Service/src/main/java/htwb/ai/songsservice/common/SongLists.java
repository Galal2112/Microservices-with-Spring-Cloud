package htwb.ai.songsservice.common;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import htwb.ai.songsservice.entity.SongList;

import java.util.List;

@JacksonXmlRootElement(localName = "song-lists")
public class SongLists {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "song-list")
    public List<SongList> songLists;

    public SongLists() {}
    public SongLists(List<SongList> songLists) {
        this.songLists = songLists;
    }
}
