package htwb.ai.lyricsservice.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(value = { "explicit", "script_tracking_url", "pixel_tracking_url", "updated_time" })
@AllArgsConstructor
@NoArgsConstructor
public class Lyrics {
    @JsonProperty("lyrics_id")
    public int lyricsId;
    @JsonProperty("lyrics_body")
    public String lyricsBody;
    @JsonProperty("lyrics_copyright")
    public String lyricsCopyright;
}