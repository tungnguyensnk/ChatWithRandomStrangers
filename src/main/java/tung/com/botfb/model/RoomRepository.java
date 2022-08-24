package tung.com.botfb.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into room (pid1, pid2) values (?1,?2)")
    void them(String pid1, String pid2);

    @Query(nativeQuery = true, value = "select * from room where pid1 = ?1 or pid2 = ?1")
    Object checkInRoom(String pid);

    @Query(nativeQuery = true, value = "select (case when pid1 = ?1 then pid2 when pid2 = ?1 then pid1 end )from room where pid1 = ?1 or pid2 = ?1")
    String idNguoiKia(String pid);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from room where pid2 = ?1 or pid1 = ?1")
    void xoa(String pid);
}