package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
            SELECT b.id, b.title, b.author_id, a.full_name, b.genre_id, g.name
            FROM books b
                JOIN authors a ON b.author_id = a.id
                JOIN genres g ON b.genre_id = g.id
            WHERE b.id = :id
            """;
        Map<String, Object> params = Map.of("id", id);
        List<Book> books = namedJdbc.query(sql, params, new BookRowMapper());
        return books.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        String sql = """
            SELECT b.id, b.title, b.author_id, a.full_name, b.genre_id, g.name
            FROM books b
                JOIN authors a ON b.author_id = a.id
                JOIN genres g ON b.genre_id = g.id
            """;
        return namedJdbc.query(sql, new BookRowMapper());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM books WHERE id = :id";
        namedJdbc.update(sql, Map.of("id", id));
    }

    private Book insert(Book book) {
        String sql = "INSERT INTO books (title, author_id, genre_id) VALUES (:title, :author_id, :genre_id)";
        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId())
                .addValue("genre_id", book.getGenre().getId());
        var keyHolder = new GeneratedKeyHolder();
        namedJdbc.update(sql, params, keyHolder, new String[]{"id"});
        book.setId(keyHolder.getKeyAs(Long.class));
        return book;
    }

    private Book update(Book book) {
        String sql = "UPDATE books SET title = :title, author_id = :author_id, genre_id = :genre_id WHERE id = :id";
        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId())
                .addValue("genre_id", book.getGenre().getId())
                .addValue("id", book.getId());
        int updated = namedJdbc.update(sql, params);
        if (updated == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }
        return book;
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            Author author = new Author(rs.getLong("author_id"), rs.getString("full_name"));
            Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("name"));
            return new Book(id, title, author, genre);
        }
    }
}
