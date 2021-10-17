package htwb.ai.lyricsservice.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class User {
    public String userId;
    public String firstName;
    public String lastName;
    public String password;
    public String authToken;
}
