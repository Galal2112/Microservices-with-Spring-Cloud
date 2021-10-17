package htwb.ai.songsservice.repository;


import htwb.ai.songsservice.entity.SongListSongs;
import htwb.ai.songsservice.entity.SongListSongsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface SongListSongsRepository extends JpaRepository<SongListSongs, SongListSongsKey> {

    @Transactional
    @Modifying
    @Query("delete from SongListSongs s where s.song.id = :songId")
    void deleteBySongId(int songId);

    @Transactional
    @Modifying
    @Query("delete from SongListSongs s where s.songList.id = :songListId")
    void deleteBySongListId(int songListId);
}
