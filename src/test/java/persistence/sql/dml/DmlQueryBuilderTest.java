package persistence.sql.dml;

import org.junit.jupiter.api.Test;
import persistence.entity.Person;

import static org.assertj.core.api.Assertions.assertThat;

class DmlQueryBuilderTest {

    @Test
    void insertSql() {
        final Person jeongwon = new Person(
                null,
                "정원",
                15,
                "a@a.com",
                1
        );

        final String insertSql = new DmlQueryBuilder(Person.class)
                .insert(jeongwon);

        assertThat(insertSql).isEqualTo("insert into users (id,nick_name,old,email) values (null,'정원',15,'a@a.com')");
    }

    @Test
    void findAllSql() {
        final String findAllSql = new DmlQueryBuilder(Person.class).findAll();

        assertThat(findAllSql).isEqualTo("select id,nick_name,old,email from users");
    }

    @Test
    void findByIdSql() {
        final String findByIdSql = new DmlQueryBuilder(Person.class).findById(1L);

        assertThat(findByIdSql).isEqualTo("select id,nick_name,old,email from users where id=1");
    }

    @Test
    void deleteByIdSql() {
        final Person jeongwon = new Person(1L, "정원", 15, "a@a.com", 1);
        final String deleteByIdSql = new DmlQueryBuilder(Person.class).delete(jeongwon);

        assertThat(deleteByIdSql).isEqualTo("delete from users where id=1");
    }

}
