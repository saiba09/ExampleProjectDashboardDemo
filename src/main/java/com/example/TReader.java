package com.example;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.ProjectionEntity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.Query.ResultType;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.ReadOption;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.joda.time.Instant;

import com.google.cloud.dataflow.sdk.io.UnboundedSource;
import com.google.cloud.dataflow.sdk.io.UnboundedSource.CheckpointMark;
import com.google.cloud.dataflow.sdk.io.UnboundedSource.UnboundedReader;

public class TReader extends UnboundedReader<String> {

	 private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);
	private Datastore datastore = HELPER.getOptions().toBuilder().setNamespace("ghijklmnop").build().getService();
	QueryResults<Entity> result;	

	private String currentData;
	private String currentKey;

	@Override
	public boolean advance() throws IOException {
		currentData = "";
		currentKey = "";
		if (result != null) {
			while(result.hasNext()) {
				Entity record = result.next();
				//System.out.println("record key = "+record.key() +" and value = "+record.value());
				currentData += " "+record.toString();
				currentKey += " "+String.valueOf(record.getKey());
			}
			return true;
		}else{
			System.out.println("NO MORE TO READ");
			return false;
		}
		
	}

	@Override
	public CheckpointMark getCheckpointMark() {
		return new KafkaCheckpointMark();
	}

	@Override
	public UnboundedSource<String, ?> getCurrentSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instant getWatermark() {
		return new Instant(new java.util.Date().getTime() + 1000L);
	}

	@Override
	public boolean start() throws IOException {

		Query<Entity> query = Query.newEntityQueryBuilder()
    		.setKind("Temperature")
    		.setFilter(PropertyFilter.le("Time", Timestamp.parseTimestamp("2017-05-10T17:20:20.00002+05:30")))
    		.build();
		result = datastore.run(query);
		return advance();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public String getCurrent() throws NoSuchElementException {
		return currentData;
	}

	@Override
	public byte[] getCurrentRecordId() throws NoSuchElementException {
		return currentKey.getBytes();
	}

	@Override
	public Instant getCurrentTimestamp() throws NoSuchElementException {
		return Instant.now();
	}
	
	class KafkaCheckpointMark implements CheckpointMark{

		public void finalizeCheckpoint() throws IOException {
			// TODO Auto-generated method stub
		}
		
	}

}

