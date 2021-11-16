# mn_data_jdbc

Working with micronaut data api


#### 1. Setup gradle and java 
```
mn create-app example --build=gradle --lang=java
```
#### 2. Data Source configuration 
```
//build.gradle

annotationProcessor("io.micronaut.data:micronaut-data-processor") 
implementation("io.micronaut.data:micronaut-data-jdbc") 
implementation("io.micronaut.sql:micronaut-jdbc-hikari") 
runtimeOnly("com.h2database:h2") 

//application.yml

datasources:
  default:
    url: ${JDBC_URL:`jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`}
    username: ${JDBC_USER:sa}
    password: ${JDBC_PASSWORD:""}
    driverClassName: ${JDBC_DRIVER:org.h2.Driver}
```
#### 3. DB Schema creates tables BOOK and GENRE 
```
--- build.gradle ---

implementation("io.micronaut.flyway:micronaut-flyway")

--- application.yml

flyway:
  datasources:
    default:
      enabled: true
      
--- V1__schema.sql ---

DROP TABLE IF EXISTS BOOK;

CREATE TABLE GENRE (
  id    BIGINT SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(255)              NOT NULL UNIQUE
);
```

#### 4. Create Genre domain entity 
```
@MappedEntity
public class Genre {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    @NotNull
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
```
#### 5.Repository Access create interface to define the operations to access the database
The repository extends from PageableRepository. It inherits the hierarchy

`PageableRepository → CrudRepository → GenericRepository`

`PageableRepository`
provides findAll(Pageable) and findAll(Sort)

`CrudRepository`
findAll(), save(Genre), deleteById(Long) and findById(Long

`GenericRepository`
features no methods but defines the entity type and ID type as generic arguments.
```
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
```
#### 6 Create a class to encapsulate the Update operations
```
@Introspected 
public class GenreUpdateCommand {
    @NotNull
    private final Long id;

    @NotBlank
    private final String name;

    public GenreUpdateCommand(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```
#### 7 Implement GenreController
```
@ExecuteOn(TaskExecutors.IO)
@Controller("/genres")
public class GenreController {
    protected final GenreRepository genreRepository;

    public GenreController(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Get("/{id}")
    public Optional<Genre> show(Long id) {
        return genreRepository.findById(id);
    }

    @Put
    public HttpResponse update(@Body @Valid GenreUpdateCommand command) {
        genreRepository.update(command.getId(), command.getName());
        return HttpResponse
                .noContent()
                .header(HttpHeaders.LOCATION, location(command.getId()).getPath());
    }

    @Get(value = "/list")
    public List<Genre> list(@Valid Pageable pageable) {
        return genreRepository.findAll(pageable).getContent();
    }

    @Post
    public HttpResponse<Genre> save(@Body("name") @NotBlank String name) {
        Genre genre = genreRepository.save(name);

        return HttpResponse
                .created(genre)
                .headers(headers -> headers.location(location(genre.getId())));
    }

    @Post("/ex")
    public HttpResponse<Genre> saveExceptions(@Body @NotBlank String name) {
        try {
            Genre genre = genreRepository.saveWithException(name);
            return HttpResponse
                    .created(genre)
                    .headers(headers -> headers.location(location(genre.getId())));
        } catch(DataAccessException e) {
            return HttpResponse.noContent();
        }
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(Long id) {
        genreRepository.deleteById(id);
    }


    protected URI location(Long id) {
        return URI.create("/genres/" + id);
    }

    protected URI location(Genre genre) {
        return location(genre.getId());
    }
}

------------------ ADD A ENTRY ------------------------

curl -X "POST" "http://localhost:8080/genres" \                                                       ─╯
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "name": "music" }'
     
 // RETURNS --> {"id":1,"name":"music"}% 
```
