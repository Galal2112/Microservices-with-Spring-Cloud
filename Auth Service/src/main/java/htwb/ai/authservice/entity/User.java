package htwb.ai.authservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User {

    @Id
    private String userId;
    private String firstName;
    private String lastName;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String authToken;
}
