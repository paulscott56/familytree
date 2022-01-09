package com.paulscott.familytree.services;

import com.paulscott.familytree.models.FamilyTreePerson;
import com.paulscott.familytree.repositories.PersonRepository;
import org.familysearch.platform.ordinances.Ordinance;
import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.parser.ModelParser;
import org.gedcomx.Gedcomx;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conversion.GedcomxConversionResult;
import org.gedcomx.conversion.gedcom.dq55.GedcomMapper;
import org.gedcomx.conversion.gedcom.dq55.MappingConfig;
import org.gedcomx.fileformat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;

@Service
public class GedcomXParser {

    private final static Logger log = LoggerFactory.getLogger(GedcomXParser.class);

    @Autowired
    private PersonRepository personRepo;

    public Gedcomx convertGedcomToGedcomXFile(String gedcomFile, String gedcomXFile) throws IOException, SAXParseException {
        String outputFileName = gedcomXFile;
        OutputStream outputStream = new FileOutputStream(outputFileName);
        File inFile = new File(gedcomFile);
        // first we convert to gedcomX
        //Gedcom2Gedcomx converter = new Gedcom2Gedcomx();
        ModelParser modelParser = new ModelParser();
        Gedcom gedcom = modelParser.parseGedcom(inFile);
        gedcom.createIndexes();
        MappingConfig mappingConfig = new MappingConfig(inFile.getName(), false);
        GedcomMapper mapper = new GedcomMapper(mappingConfig);
        GedcomxEntrySerializer serializer = new JacksonJsonSerialization(new Class[]{Ordinance.class});
        GedcomxConversionResult result = mapper.toGedcomx(gedcom);

        GedcomxOutputStream output = new GedcomxOutputStream(outputStream, serializer);
        output.addAttribute("User-Agent", "Gedcom To Gedcomx Java Conversion Utility/1.0");
        output.addAttribute("X-DC-conformsTo", "http://gedcomx.org/file/v1");
        output.addAttribute("X-DC-created", GedcomxTimeStampUtil.formatAsXmlUTC(new Date()));
        if (result.getDatasetContributor() != null && result.getDatasetContributor().getId() != null) {
            output.addAttribute("X-DC-creator", outputFileName + "#" + result.getDatasetContributor().getId());
        }
        output.addResource(outputFileName, result.getDataset(), (Date) null);
        output.close();

        return result.getDataset();
    }

    public void getPeople(Gedcomx gedcomxfile) {
        List<Person> people = gedcomxfile.getPersons();
        for (Person p : people) {
            FamilyTreePerson treePerson = new FamilyTreePerson();
            treePerson.setNames(p.getNames());
            treePerson.setFacts(p.getFacts());
            treePerson.setFields(p.getFields());
            treePerson.setLiving(p.getLiving());
            treePerson.setPrivate(p.getPrivate());
            treePerson.setGender(p.getGender());
            treePerson.setPrincipal(p.getPrincipal());
            treePerson.setDisplay(p.getDisplayExtension());
            personRepo.save(treePerson);
        }
    }

    private List<Gedcomx> readGedcomXFile(String gedcomXFile) {
        try {
            JarFile jar = new JarFile(gedcomXFile);
            GedcomxFile gxFile = new GedcomxFile(jar);
            Iterable<GedcomxFileEntry> entries = gxFile.getEntries();
            List<Gedcomx> list = new ArrayList<>();
            for (GedcomxFileEntry entry : entries) {
                //for each entry, read the model.
                Gedcomx gx = (Gedcomx) gxFile.readResource(entry);
                list.add(gx);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
