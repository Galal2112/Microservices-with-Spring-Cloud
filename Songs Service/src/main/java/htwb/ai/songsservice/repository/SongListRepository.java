package htwb.ai.songsservice.repository;

import htwb.ai.songsservice.entity.SongList;
import htwb.ai.songsservice.entity.SongListAccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongListRepository extends JpaRepository<SongList, Integer> {

    @Query("select s from SongList s where s.ownerId = :userId and s.accessibility in :accessLevel")
    List<SongList> findSongListsOfUser(String userId, List<SongListAccessLevel> accessLevel);

    @Override
    @Query("select s from SongList s left join fetch s.linkedSongs where s.id = :id")
    Optional<SongList> findById(Integer id);
}
