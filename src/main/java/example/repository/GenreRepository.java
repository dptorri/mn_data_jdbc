package example.repository;

import example.domain.Genre;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.micronaut.core.annotation.NonNull;

@JdbcRepository(dialect = Dialect.H2)
public interface GenreRepository extends PageableRepository<Genre, Long> {

    Genre save(@NonNull @NotBlank String name);

    @Transactional
    default Genre saveWithException(@NonNull @NotNull String name) {
        save(name);
        throw new DataAccessException("Oops DataAccessException occurred!");
    }

    int update(@NonNull @NotNull @Id Long id, @NonNull @NotNull String name);

}
