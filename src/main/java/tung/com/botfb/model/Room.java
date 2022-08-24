package tung.com.botfb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "room")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pid1", nullable = false)
    private Info pid1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pid2", nullable = false)
    private Info pid2;

    public Info getPid2() {
        return pid2;
    }

    public void setPid2(Info pid2) {
        this.pid2 = pid2;
    }

    public Info getPid1() {
        return pid1;
    }

    public void setPid1(Info pid1) {
        this.pid1 = pid1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}