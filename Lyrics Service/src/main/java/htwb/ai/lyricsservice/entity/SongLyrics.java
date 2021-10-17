package htwb.ai.lyricsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongLyrics {
    @Id
    private int id;
    private String lyricsBody;
    private String lyricsCopyright;
}
