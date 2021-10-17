package htwb.ai.songsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "songList_songs")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SongListSongs {

    @EmbeddedId
    private SongListSongsKey id;

    @ManyToOne
    @MapsId("songListId")
    @JoinColumn(name = "songListId")
    private SongList songList;

    @ManyToOne
    @MapsId("songId")
    @JoinColumn(name = "songId")
    private Song song;
}
