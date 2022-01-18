package com.example.booksearchservice.controllers;

import com.example.booksearchservice.model.Book;
import com.example.booksearchservice.services.BookService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/search", produces = MediaType.TEXT_HTML_VALUE)
public class SearchController {

    private final Logger logger = Logger.getLogger(SearchController.class.getName());

    @Autowired
    BookService bookSvc;

    @GetMapping
    public String getSearchResult(@RequestParam(required = true) String searchTitle, Model model) {
        model.addAttribute("searchTitle", searchTitle);
        String jsonDataString = bookSvc.search(searchTitle);
        logger.log(Level.INFO, "search result in json: " + jsonDataString);
        List<Book> bookListObj = bookSvc.jsonToBookList(jsonDataString);
        for (Book book : bookListObj) {
            logger.log(Level.INFO, "book title in booklist: " + book.getTitle());
        }
        model.addAttribute("bookListObj", bookListObj);
        return "results";
    }

    @GetMapping(path = "/main")
    public String backToSearch() {
        return "index";
    }

}
