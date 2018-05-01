package com.vena;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PersonMongoClientMockitoTest {

	private static PersonMongoClient personDb;
	@Spy private static PersonMongoClient fakePersonDb;

	@BeforeClass
	public static void setup() {
		Datastore dataStore = Mockito.mock(DatastoreImpl.class);
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

		// mockito magic goes here
		// maybe something like this?
		// Mockito.when(personDb.getPerson(jSmith)).thenReturn(jSmithDupe);

		personDb.bulkInsertPerson(personList);

		assertEquals(1000.00, personDb.getPerson(jSmith).getAccountBalance(), 1E-15);
	}
}
