package com.paulscott.familytree.controllers;

import com.paulscott.familytree.services.GedcomXParser;
import org.gedcomx.Gedcomx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXParseException;

import java.io.IOException;

@RestController
public class DataController {

    @Autowired
    private GedcomXParser parser;

    @GetMapping("/data")
    public String readData() {
        try {
            Gedcomx gedcomXFile = parser.convertGedcomToGedcomXFile("src/main/java/011c9n_456494c5xh61gu194061cx_A.ged",
                    "outputgedcomx.json");
            parser.getPeople(gedcomXFile);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
