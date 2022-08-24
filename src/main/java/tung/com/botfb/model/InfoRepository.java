package tung.com.botfb.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface InfoRepository extends JpaRepository<Info, String> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into info (pid, gioitinh) values (?1,?2)")
    void them(String pid, String gioiTinh);

    @Query(nativeQuery = true, value = "select true from info where pid = ?1")
    Object check(String pid);

    @Query(nativeQuery = true, value = "select gioitinh from info where pid = ?1")
    String checkGioiTinh(String pid);
}