package htwb.ai.lyricsservice.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "header" })
public class Message {
    public Body body;
}
