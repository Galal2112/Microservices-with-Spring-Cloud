package htwb.ai.songsservice.common;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import htwb.ai.songsservice.entity.Song;

import java.io.Serializable;
import java.util.List;

@JacksonXmlRootElement(localName = "songs") // <songs> </songs>
public class Songs implements Serializable {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "song")
    public List<Song> songs;

    public Songs() {}
    public Songs(List<Song> songs) {
        this.songs = songs;
    }
}
