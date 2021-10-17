package htwb.ai.lyricsservice.repository;

import htwb.ai.lyricsservice.entity.SongLyrics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongLyricsRepository extends MongoRepository<SongLyrics, Integer> {

}
