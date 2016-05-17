package foo.bar.joinsupersub.entities;

import javax.persistence.*;

@Entity
@Table(name="PROJECT")
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE")
@DiscriminatorValue("super")
public class SuperClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;

    public SuperClass() {
    }

    public SuperClass(String name) {
        this.name = name;
    }
}
