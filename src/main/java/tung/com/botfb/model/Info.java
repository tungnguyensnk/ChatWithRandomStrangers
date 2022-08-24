package tung.com.botfb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Info {
    @Id
    @Lob
    @Column(name = "pid", nullable = false)
    private String id;

    @Lob
    @Column(name = "gioitinh")
    private String gioitinh;

}