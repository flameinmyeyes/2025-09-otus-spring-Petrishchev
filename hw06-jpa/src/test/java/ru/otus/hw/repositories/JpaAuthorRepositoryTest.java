package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с авторами")
@DataJpaTest
@Import(JpaAuthorRepository.class)
class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository repository;

    @Autowired
    private TestEntityManager em;

    private List<Author> dbAuthors;

    @BeforeEach
    void setUp() {
        dbAuthors = List.of(
                em.find(Author.class, 1L),
                em.find(Author.class, 2L),
                em.find(Author.class, 3L)
        );
    }

    @Test
    @DisplayName("должен загружать список всех авторов")
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repository.findAll();
        assertThat(actualAuthors).containsExactlyElementsOf(dbAuthors);
    }

    @Test
    @DisplayName("должен загружать автора по id")
    void shouldReturnCorrectAuthorById() {
        var expectedAuthor = dbAuthors.get(0);
        var actualAuthor = repository.findById(expectedAuthor.getId());
        assertThat(actualAuthor)
                .isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }

    @Test
    @DisplayName("должен возвращать empty при поиске несуществующего автора")
    void shouldReturnEmptyWhenAuthorNotFound() {
        var actualAuthor = repository.findById(999L);
        assertThat(actualAuthor).isEmpty();
    }
}
