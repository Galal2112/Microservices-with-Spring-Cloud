package htwb.ai.lyricsservice.common;

public class LyricsResponse {
    public Message message;

    public Lyrics getLyrics() {
        return message.body.lyrics;
    }
}