package com.vena;

import java.util.List;

import com.mongodb.BulkWriteError;
import com.mongodb.BulkWriteException;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBObject;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class PersonMongoClient extends BasicDAO<Person, Long> {
	private final String collname = "person";
	public String getCollectionName() {
		return collname;
	}

	public PersonMongoClient(Datastore dataStore) {
		super(dataStore);
		createIndexes();
	}

	public void createIndexes() {
		getDatastore().ensureIndex(getEntityClass(), "primary_name_bday", "name, bday", /*unique*/true, /*dropOnCreate*/false);
	}

	private DBObject entityToDBObj(Person p) {
		return ((DatastoreImpl) getDatastore()).getMapper().toDBObject(p);
	}

	public Person getPerson(Person dupe) {
		Query<Person> q = createQuery();
		q.field("name").equal(dupe.getName());
		q.field("bday").equal(dupe.getBday());
		return q.get();
	}

	public void savePerson(Person toSave) {
		super.save(toSave);
	}

	public void bulkInsertPerson(List<Person> personsToInsert) {
		BulkWriteOperation builder = getCollection().initializeUnorderedBulkOperation();
		for (Person person : personsToInsert) {
			DBObject insObj = entityToDBObj(person);
			builder.insert(insObj);
		}

		try {
			builder.execute().isAcknowledged();
		} catch (BulkWriteException bwe) {
			for (BulkWriteError e : bwe.getWriteErrors()) {
				if (e.getCode() == 11000) { //duplicate key exception
					Person duplicatePerson = personsToInsert.get(e.getIndex());
					Person fromMongo = getPerson(duplicatePerson);  // look it up in the collection

					if (fromMongo != null) {
						duplicatePerson.setId(fromMongo.getId());
						savePerson(duplicatePerson);
					}
				} else {
					//only handle bulkwrite errors if they're all duplicate key exceptions
					throw bwe;
				}
			}
		}
	}
}
