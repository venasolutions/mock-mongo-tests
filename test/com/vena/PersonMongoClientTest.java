package com.vena;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class PersonMongoClientTest {
	private static PersonMongoClient personDb;

	@BeforeClass
	public static void setup() {
		Fongo fongo = new Fongo("fongo mock server");
		MongoClient mongoClient =  fongo.getMongo();

		// comment in the line below to test against a real MongoDB instance running at localhost:27017
//		mongoClient =  new MongoClient();

		Morphia morphia = new Morphia();
		Datastore dataStore = new DatastoreImpl(morphia, mongoClient, "mydb");
		personDb = new PersonMongoClient(dataStore);
	}

	@Test
	public void saveDuplicates() {
		Person jSmith = new Person("John Smith", "1985-02-25", 1234.56);
		personDb.save(jSmith);

		List<Person> personList = new ArrayList<>();
		Person alice = new Person("Alice", "1990-04-18", 400.00);
		personList.add(alice);
		Person jSmithDupe = new Person("John Smith", "1985-02-25", 1000.00);
		personList.add(jSmithDupe);
		personDb.bulkInsertPerson(personList);

		assertEquals(1000.00, personDb.getPerson(jSmith).getAccountBalance(), 1E-15);
	}
}