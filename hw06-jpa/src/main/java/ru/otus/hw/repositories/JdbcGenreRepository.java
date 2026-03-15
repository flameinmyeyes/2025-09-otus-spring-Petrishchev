package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres";
        return namedJdbc.query(sql, new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sql = "SELECT id, name FROM genres WHERE id = :id";
        Map<String, Object> params = Map.of("id", id);
        List<Genre> genres = namedJdbc.query(sql, params, new GenreRowMapper());
        return genres.stream().findFirst();
    }

    private static class GenreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Genre(rs.getLong("id"), rs.getString("name"));
        }
    }
}
