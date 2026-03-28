package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с жанрами")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository repository;

    @Autowired
    private TestEntityManager em;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        dbGenres = List.of(
                em.find(Genre.class, 1L),
                em.find(Genre.class, 2L),
                em.find(Genre.class, 3L)
        );
    }

    @Test
    @DisplayName("должен загружать список всех жанров")
    void shouldReturnCorrectGenresList() {
        var actualGenres = repository.findAll();
        assertThat(actualGenres).containsExactlyElementsOf(dbGenres);
    }

    @Test
    @DisplayName("должен загружать жанр по id")
    void shouldReturnCorrectGenreById() {
        var expectedGenre = dbGenres.get(0);
        var actualGenre = repository.findById(expectedGenre.getId());
        assertThat(actualGenre)
                .isPresent()
                .get()
                .isEqualTo(expectedGenre);
    }

    @Test
    @DisplayName("должен возвращать empty при поиске несуществующего жанра")
    void shouldReturnEmptyWhenGenreNotFound() {
        var actualGenre = repository.findById(999L);
        assertThat(actualGenre).isEmpty();
    }
}
