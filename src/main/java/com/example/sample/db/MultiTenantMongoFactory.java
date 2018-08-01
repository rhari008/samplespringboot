package com.example.sample.db;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;

import org.springframework.web.context.annotation.ApplicationScope;

import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;


@ApplicationScope
public class MultiTenantMongoFactory implements MongoDbFactory{

	
	private final MongoConnector mongoConnector;
	private final MongoExceptionTranslator exceptionTranslator;

	MultiTenantMongoFactory(MongoConnector mongoConnector) {
		this.mongoConnector = mongoConnector;
		this.exceptionTranslator = new MongoExceptionTranslator();
	}
	
	@Override
	public MongoDatabase getDb() throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MongoDatabase getDb(String arg0) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceExceptionTranslator getExceptionTranslator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DB getLegacyDb() {
		// TODO Auto-generated method stub
		return null;
	}

}
	