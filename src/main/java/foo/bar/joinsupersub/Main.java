package foo.bar.joinsupersub;

import foo.bar.joinsupersub.entities.SubClass;
import foo.bar.joinsupersub.entities.SuperClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("leftjoin");

        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        SuperClass p = new SuperClass("Super1");
        em.persist(p);
        p = new SuperClass("Super2");
        em.persist(p);

        SubClass lp = new SubClass("Sub1");
        em.persist(lp);

        em.getTransaction().commit();

        // Joining a superclass with it's subclass results in wrong discriminator-bounds in the SQL Query.
        // This affects both `JOIN ON` as well as `LEFT OUTER JOIN ON`
        // In this example all SuperClasses are joined with a SubClass identified by it's name.
        // All instances of the Superclass (including all extending classes) should be joined with one Subclass.
        // This should result in 3 tuples:
        // ```
        // (Super1, Sub1)
        // (Super2, Sub1)
        // (Sub1, Sub1)
        // ```
        // Instead, the type bound to the generated SQL-Query inserts the discriminator "sub" of the subclass for
        //   both classes.
        // This produces only the following tuple:
        // ```
        // (Sub1, Sub1)
        // ```
        //
        // The correct query would test `SuperClass` against the discriminator of both itself and all it's subclasses:
        // ```
        // SELECT t0.ID, t0.TYPE, t0.NAME, t1.ID, t1.TYPE, t1.NAME
        // FROM PROJECT t0
        //   LEFT OUTER JOIN PROJECT t1
        //     ON (t1.NAME = ?)
        // WHERE (((t0.TYPE = ?) OR (t0.TYPE = ?)) AND (t1.TYPE = ?))
        // bind => [Sub1, super, sub, sub]
        // ```
        // i.e.
        // ```
        // SELECT t0.ID, t0.TYPE, t0.NAME, t1.ID, t1.TYPE, t1.NAME
        // FROM PROJECT t0, PROJECT t1
        // WHERE (((t0.TYPE = ?) OR (t0.TYPE = ?)) AND ((t1.NAME = ?) AND (t1.TYPE = ?)))
        // bind => [super, sub, Sub1, sub]
        // ```

        // SELECT t0.ID, t0.TYPE, t0.NAME, t1.ID, t1.TYPE, t1.NAME
        // FROM PROJECT t0
        //   LEFT OUTER JOIN PROJECT t1
        //     ON (t1.NAME = ?)
        // WHERE ((t0.TYPE = ?) AND (t1.TYPE = ?))
        // bind => [Sub1, sub, sub]
        List<?> result = em.createQuery("SELECT super, sub FROM SuperClass super LEFT OUTER JOIN SubClass sub ON sub.name = 'Sub1'").getResultList();
        System.out.println("Result length: " + result.size());

        // SELECT t0.ID, t0.TYPE, t0.NAME, t1.ID, t1.TYPE, t1.NAME
        // FROM PROJECT t0, PROJECT t1
        // WHERE ((t0.TYPE = ?) AND ((t1.NAME = ?) AND (t1.TYPE = ?)))
        // bind => [sub, Sub1, sub]
        result = em.createQuery("SELECT super, sub FROM SuperClass super JOIN SubClass sub ON sub.name = 'Sub1'").getResultList();
        System.out.println("Result length: " + result.size());

    }
}
