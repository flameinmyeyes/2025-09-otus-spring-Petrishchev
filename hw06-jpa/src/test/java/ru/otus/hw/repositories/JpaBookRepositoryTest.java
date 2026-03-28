package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с книгами")
@DataJpaTest
@Import(JpaBookRepository.class)
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository repository;

    @Autowired
    private TestEntityManager em;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbBooks = List.of(
                em.find(Book.class, 1L),
                em.find(Book.class, 2L),
                em.find(Book.class, 3L)
        );
    }

    @Test
    void shouldFindAll() {
        var actual = repository.findAll();
        assertThat(actual).containsExactlyElementsOf(dbBooks);
        actual.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenre()).isNotNull();
        });
    }

    @Test
    void shouldFindById() {
        var expected = dbBooks.get(0);
        var actual = repository.findById(expected.getId());
        assertThat(actual).isPresent().get().isEqualTo(expected);
    }

    @Test
    void shouldInsert() {
        var author = em.find(Author.class, 1L);
        var genre = em.find(Genre.class, 1L);
        var newBook = new Book(0, "New Book", author, genre);
        var saved = repository.save(newBook);
        assertThat(saved.getId()).isGreaterThan(0);
        var found = em.find(Book.class, saved.getId());
        assertThat(found).isNotNull()
                .usingRecursiveComparison().ignoringFields("id").isEqualTo(newBook);
    }

    @Test
    void shouldUpdate() {
        var existing = dbBooks.get(0);
        var newTitle = "Updated Title";
        existing.setTitle(newTitle);
        repository.save(existing);
        var updated = em.find(Book.class, existing.getId());
        assertThat(updated.getTitle()).isEqualTo(newTitle);
    }

    @Test
    void shouldDelete() {
        var book = dbBooks.get(0);
        repository.deleteById(book.getId());
        assertThat(em.find(Book.class, book.getId())).isNull();
    }
}
