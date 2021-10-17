package htwb.ai.songsservice.repository;

import htwb.ai.songsservice.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Integer> {
    @Query("select count(s) from Song s where s.id in :ids")
    int countOfSongsForIds(List<Integer> ids);
}
