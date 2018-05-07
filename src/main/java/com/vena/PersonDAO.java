package com.vena;

import java.util.List;

import com.mongodb.BulkWriteError;
import com.mongodb.BulkWriteException;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBObject;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class PersonDAO extends BasicDAO<Person, Long> {
	public PersonDAO(Datastore dataStore) {
		super(dataStore);
		createIndexes();
	}

	public PersonDAO(MongoClient mongoClient, Morphia morphia, String dbName) {
		super(mongoClient, morphia, dbName);
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

					duplicatePerson.setId(fromMongo.getId());
					save(duplicatePerson);
				} else {
					//only handle bulkwrite errors if they're all duplicate key exceptions
					throw bwe;
				}
			}
		}
	}
}
