package com.paulscott.familytree.repositories;

import com.paulscott.familytree.models.FamilyTreePerson;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PersonRepository extends Neo4jRepository<FamilyTreePerson, Long> {

}