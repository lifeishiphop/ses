package com.web.back;

import com.web.back.demo.DemoDataInitializer;
import com.web.back.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ses-demo-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles({"local", "demo"})
@Transactional
class DemoDataIntegrationTest {
    @Autowired EntityManager em;
    @Autowired DemoDataInitializer initializer;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;

    @Test
    void seedsRelatedLearningStatesAndUsableEncodedAccounts() throws Exception {
        assertThat(count("Member")).isEqualTo(4);
        assertThat(count("Lesson")).isEqualTo(4);
        assertThat(count("Student")).isEqualTo(6);
        assertThat(count("Problem")).isEqualTo(6);
        assertThat(count("Assignment")).isEqualTo(5);
        assertThat(count("Testcase")).isEqualTo(21);
        assertThat(count("Submit")).isEqualTo(4);
        assertThat(count("Post")).isEqualTo(3);
        assertThat(count("Comment")).isEqualTo(3);
        Member student = em.createQuery("select m from Member m where m.number = '20990001'", Member.class).getSingleResult();
        assertThat(student.getPassword()).isNotEqualTo("SesDemo123!");
        assertThat(encoder.matches("SesDemo123!", student.getPassword())).isTrue();
        assertThat(em.createQuery("select count(s) from Student s where s.member.id = :id and s.accept = 1", Long.class)
                .setParameter("id", student.getId()).getSingleResult()).isEqualTo(3);
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"20990001\",\"password\":\"SesDemo123!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.memberDto.identity").value(1));
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"20999901\",\"password\":\"SesDemo123!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.memberDto.identity").value(2));
    }

    @Test
    void repeatStartupPreservesEditsAndDoesNotDuplicateRows() throws Exception {
        Problem problem = em.createQuery("select p from Problem p order by p.id", Problem.class).setMaxResults(1).getSingleResult();
        problem.setTitle("사용자가 수정한 데모 문제");
        em.flush();
        initializer.run(new DefaultApplicationArguments());
        assertThat(count("Member")).isEqualTo(4);
        assertThat(count("Problem")).isEqualTo(6);
        assertThat(count("Assignment")).isEqualTo(5);
        assertThat(count("Submit")).isEqualTo(4);
        assertThat(problem.getTitle()).isEqualTo("사용자가 수정한 데모 문제");
    }

    @Test
    void refusesToOverwriteAnExistingAccountWithoutTheSeedMarker() {
        em.createQuery("delete from Comment").executeUpdate();
        em.createQuery("delete from Post").executeUpdate();
        em.createQuery("delete from Board").executeUpdate();
        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Existing accounts were not changed");
        assertThat(count("Member")).isEqualTo(4);
    }

    private long count(String entity) {
        return em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
    }
}
