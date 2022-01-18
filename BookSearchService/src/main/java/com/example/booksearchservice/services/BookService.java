package com.example.booksearchservice.services;

import static com.example.booksearchservice.Constants.URL_OPENLIBARARY_BASE;

import com.example.booksearchservice.model.Book;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BookService {

    private final Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    BookCacheService bookCacheSvc;

    public String search(String searchTerm) {
        String encodedSearchTerm = searchTerm.replace(" ", "+");
        // https://openlibrary.org/search.json?q=harry%20potter&fields=docs,title,key&limit=20
        String url = UriComponentsBuilder
            .fromUriString(URL_OPENLIBARARY_BASE + "/search.json")
            .queryParam("title", encodedSearchTerm)
            .queryParam("fields", "title,key")
            .queryParam("limit", "20")
            .toUriString();
        logger.log(Level.INFO, "search url is: " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException(
                "Error: status code %s".formatted(resp.getStatusCode().toString()));
        }
        String jsonDataString = resp.getBody();

        return jsonDataString;
    }

    public List<Book> jsonToBookList(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();
            final JsonArray readings = result.getJsonArray("docs");

            LinkedList<Book> bookList = new LinkedList<Book>();
            for (JsonValue jv : readings) {
                JsonObject jo = jv.asJsonObject();
                String key = jo.getString("key").replace("/works/", "");
                String title = jo.getString("title");
                Book book = new Book(key, title);
                bookList.add(book);
            }

            return bookList;
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new LinkedList<>();
        }

    }

    public boolean isCached(String worksId) {
        return bookCacheSvc.hasKey(worksId);
    }

    public String get(String worksId) {
        if (bookCacheSvc.hasKey(worksId)) {
            //get from redis cache
            logger.log(Level.INFO, "Cache hit for works id: " + worksId);
            return bookCacheSvc.get(worksId);
        } else {
            //get from openlibrary api
            String url = URL_OPENLIBARARY_BASE + "/works/" + worksId + ".json";
            logger.log(Level.INFO, "book url is: " + url);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.getForEntity(url, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new IllegalArgumentException(
                    "Error: status code %s".formatted(resp.getStatusCode().toString()));
            }
            String jsonDataString = resp.getBody();
            //cache data from openlibrary api
            bookCacheSvc.cache(worksId, jsonDataString);
            return jsonDataString;
        }

    }

    public Book jsonToBook(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();

            Book book = new Book();
            book.setTitle(result.getString("title"));

            String description = "Not available";
            if (result.getString("description").trim().length() > 0) {
                description = result.getString("description");
                logger.log(Level.INFO, "description found: " + description);
            }
            book.setDescription(description);

            String excerpt = "Not Available";
            book.setExcerpt(excerpt);

            return book;
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new Book();
        }
    }
}