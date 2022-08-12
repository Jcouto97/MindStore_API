package MindStore.persistence.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //em vez de identity porque quero + que uma tabela(herança n dava)
    @Column(nullable = false, unique = true, updatable = false)
    private int id;

    @Column(nullable = false)
    private String roleType;

    @OneToMany(mappedBy = "roleId")
    private List<Person> personList;
}
