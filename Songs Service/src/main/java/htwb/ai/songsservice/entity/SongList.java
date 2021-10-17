package htwb.ai.songsservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@Entity
@Table(name = "songs_list")
@XmlRootElement(name = "song-list")
public class SongList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    @XmlTransient
    @JsonIgnore
    private String ownerId;
    private String name;
    @JsonIgnore
    private SongListAccessLevel accessibility;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "songList_songs",
            joinColumns = @JoinColumn(name = "songListId"),
            inverseJoinColumns = @JoinColumn(name = "songId"))
    @JsonProperty("songList")
    @JacksonXmlElementWrapper(localName = "songs")
    @JacksonXmlProperty(localName = "song")
    private List<Song> linkedSongs;
    @JsonProperty("isPrivate")
    @Transient
    private boolean isPrivate;

    public Integer getId() {
        return id;
    }

    @JsonProperty
    public void setId(Integer id) {
        this.id = id;
    }

    @XmlTransient
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public SongListAccessLevel getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(SongListAccessLevel accessibility) {
        this.accessibility = accessibility;
    }

    @XmlElement(name = "song")
    public List<Song> getLinkedSongs() {
        return linkedSongs;
    }

    public void setLinkedSongs(List<Song> songs) {
        this.linkedSongs = songs;
    }

    @XmlElement(name = "isPrivate")
    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
