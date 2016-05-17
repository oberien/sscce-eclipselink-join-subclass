package foo.bar.joinsupersub.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("sub")
public class SubClass extends SuperClass {
    public SubClass() {
    }

    public SubClass(String name) {
        super(name);
    }
}
