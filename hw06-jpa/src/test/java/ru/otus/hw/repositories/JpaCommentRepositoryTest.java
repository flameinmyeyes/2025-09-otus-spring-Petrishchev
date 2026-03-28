package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с комментариями")
@DataJpaTest
@Import(JpaCommentRepository.class)
class JpaCommentRepositoryTest {

    @Autowired
    private JpaCommentRepository repository;

    @Autowired
    private TestEntityManager em;

    private List<Comment> dbComments;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = em.find(Book.class, 1L);

        dbComments = List.of(
                new Comment(0, "Great book!", testBook),
                new Comment(0, "Very interesting", testBook),
                new Comment(0, "I recommend", testBook)
        );

        dbComments = dbComments.stream()
                .map(comment -> em.persist(comment))
                .toList();
    }

    @Test
    @DisplayName("должен загружать комментарий по id")
    void shouldReturnCorrectCommentById() {
        var expectedComment = dbComments.get(0);
        var actualComment = repository.findById(expectedComment.getId());

        assertThat(actualComment)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(expectedComment);

        assertThat(actualComment.get().getBook()).isNotNull();
    }

    @Test
    @DisplayName("должен загружать все комментарии по id книги")
    void shouldReturnCommentsByBookId() {
        var actualComments = repository.findAllByBookId(testBook.getId());

        assertThat(actualComments).hasSize(3);
        assertThat(actualComments)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("book")
                .containsExactlyElementsOf(dbComments);

        actualComments.forEach(comment ->
                assertThat(comment.getBook()).isNotNull()
        );
    }

    @Test
    @DisplayName("должен возвращать пустой список для книги без комментариев")
    void shouldReturnEmptyListWhenNoComments() {
        var actualComments = repository.findAllByBookId(999L);
        assertThat(actualComments).isEmpty();
    }

    @Test
    @DisplayName("должен сохранять новый комментарий")
    void shouldSaveNewComment() {
        var newComment = new Comment(0, "New comment", testBook);
        var savedComment = repository.save(newComment);

        assertThat(savedComment.getId()).isGreaterThan(0);

        var foundComment = em.find(Comment.class, savedComment.getId());
        assertThat(foundComment)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("book")
                .isEqualTo(newComment);
    }

    @Test
    @DisplayName("должен обновлять существующий комментарий")
    void shouldUpdateComment() {
        var comment = dbComments.get(0);
        var newText = "Updated text";
        comment.setText(newText);

        repository.save(comment);
        em.flush();
        em.clear();

        var updatedComment = em.find(Comment.class, comment.getId());
        assertThat(updatedComment.getText()).isEqualTo(newText);
    }

    @Test
    @DisplayName("должен удалять комментарий по id")
    void shouldDeleteComment() {
        var comment = dbComments.get(0);
        assertThat(em.find(Comment.class, comment.getId())).isNotNull();

        repository.deleteById(comment.getId());
        em.flush();

        assertThat(em.find(Comment.class, comment.getId())).isNull();
    }

    @Test
    @DisplayName("должен возвращать empty при поиске несуществующего комментария")
    void shouldReturnEmptyWhenCommentNotFound() {
        var actualComment = repository.findById(999L);
        assertThat(actualComment).isEmpty();
    }
}