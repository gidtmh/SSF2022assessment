package com.example.booksearchservice.controllers;

import com.example.booksearchservice.model.Book;
import com.example.booksearchservice.services.BookService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/book", produces = MediaType.TEXT_HTML_VALUE)
public class BookController {

    private final Logger logger = Logger.getLogger(BookController.class.getName());
    @Autowired
    BookService bookSvc;

    @GetMapping("/{worksId}")
    public String showBookInfo(@PathVariable String worksId, Model model) {
        Book bookObj = new Book();
        bookObj.setCached(String.valueOf(bookSvc.isCached(worksId)));
        String bookJson = bookSvc.get(worksId);
        logger.log(Level.INFO, "book json data is: " + bookJson);
        Book bookObjFromJson = bookSvc.jsonToBook(bookJson);
        bookObj.setDescription(bookObjFromJson.getDescription());
        bookObj.setExcerpt(bookObjFromJson.getExcerpt());
        bookObj.setTitle(bookObjFromJson.getTitle());
        bookObj.setKey(bookObjFromJson.getKey());
        model.addAttribute("bookObj", bookObj);
        return "bookinfo";
    }
}
