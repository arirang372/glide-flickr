package com.bumptech.glide.samples.flickr.api;

import java.util.List;

public class QueryResult {

	private final Query query;
	private final List<Photo> results;

	public QueryResult(Query query, List<Photo> results) {
		this.query = query;
		this.results = results;
	}

	public Query getQuery() {
		return this.query;
	}

	public List<Photo> getResults() {
		return this.results;
	}
}
