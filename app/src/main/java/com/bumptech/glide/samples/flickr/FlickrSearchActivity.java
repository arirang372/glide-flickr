package com.bumptech.glide.samples.flickr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.prefill.PreFillType;
import com.bumptech.glide.samples.flickr.api.Api;
import com.bumptech.glide.samples.flickr.api.Photo;
import com.bumptech.glide.samples.flickr.api.Query;
import com.bumptech.glide.samples.flickr.api.SearchQuery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

/**
 * An activity that allows users to search for images on Flickr and that contains a series of
 * fragments that display retrieved image thumbnails.
 */
public class FlickrSearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

	private class QueryListener implements Api.QueryListener {

		private boolean isCurrentQuery(Query query) {
			return currentQuery != null && currentQuery.equals(query);
		}

		@Override
		public void onSearchCompleted(Query query, List<Photo> photos) {
			if (!isCurrentQuery(query)) {
				return;
			}

			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "Search completed, got " + photos.size() + " results");
			}
			searching.setVisibility(View.INVISIBLE);

			for (PhotoViewer viewer : photoViewers) {
				viewer.onPhotosUpdated(photos);
			}

			if (backgroundThumbnailFetcher != null) {
				backgroundThumbnailFetcher.cancel();
			}

			backgroundThumbnailFetcher = new BackgroundThumbnailFetcher(FlickrSearchActivity.this, photos);
			backgroundHandler.post(backgroundThumbnailFetcher);
			currentPhotos = photos;
		}

		@Override
		public void onSearchFailed(Query query, Exception e) {
			if (!isCurrentQuery(query)) {
				return;
			}

			if (Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Search failed", e);
			}
			searching.setVisibility(View.VISIBLE);
			searchLoading.setVisibility(View.INVISIBLE);
			searchTerm.setText(getString(R.string.search_failed, currentQuery.getDescription()));
		}
	}

	private static final Query DEFAULT_QUERY = new SearchQuery("kitten");
	public static final Map<Page, Integer> PAGE_TO_TITLE;
	private static final String STATE_QUERY = "state_search_string";
	public static final String TAG = "FlickrSearchActivity";

	static {
		Map<Page, Integer> temp = new HashMap<>();
		temp.put(Page.SMALL, R.string.small);
		temp.put(Page.MEDIUM, R.string.medium);
		temp.put(Page.LIST, R.string.list);
		PAGE_TO_TITLE = Collections.unmodifiableMap(temp);
	}

	private Handler backgroundHandler;
	private HandlerThread backgroundThread;
	private BackgroundThumbnailFetcher backgroundThumbnailFetcher;
	private List<Photo> currentPhotos = new ArrayList<>();
	private Query currentQuery;
	private final Set<PhotoViewer> photoViewers = new HashSet<>();
	private final QueryListener queryListener = new QueryListener();
	private View searchLoading;
	private TextView searchTerm;
	private SearchView searchView;
	private View searching;

	private void executeQuery(Query query) {
		currentQuery = query;
		if (query == null) {
			queryListener.onSearchCompleted(null, Collections.<Photo>emptyList());
			return;
		}

		searching.setVisibility(View.VISIBLE);
		searchLoading.setVisibility(View.VISIBLE);
		searchTerm.setText(getString(R.string.searching_for, currentQuery.getDescription()));

		Api.get(this).query(currentQuery);
	}

	private void executeSearch(String searchString) {
		Query query = TextUtils.isEmpty(searchString) ? null : new SearchQuery(searchString);
		executeQuery(query);
	}

	private int getScreenWidth() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (fragment instanceof PhotoViewer) {
			PhotoViewer photoViewer = (PhotoViewer) fragment;
			photoViewer.onPhotosUpdated(currentPhotos);
			if (!photoViewers.contains(photoViewer)) {
				photoViewers.add(photoViewer);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(
				new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

		backgroundThread = new HandlerThread("BackgroundThumbnailHandlerThread");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());

		setContentView(R.layout.flickr_search_activity);
		searching = findViewById(R.id.searching);
		searchLoading = findViewById(R.id.search_loading);
		searchTerm = (TextView) findViewById(R.id.search_term);

		Resources res = getResources();
		ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
		pager.setPageMargin(res.getDimensionPixelOffset(R.dimen.page_margin));
		pager.setAdapter(new FlickrPagerAdapter(this, getSupportFragmentManager()));

		Api.get(this).registerSearchListener(queryListener);
		if (savedInstanceState != null) {
			Query savedQuery = savedInstanceState.getParcelable(STATE_QUERY);
			if (savedQuery != null) {
				executeQuery(savedQuery);
			}
		} else {
			executeQuery(DEFAULT_QUERY);
		}

		int smallGridSize = res.getDimensionPixelSize(R.dimen.small_photo_side);
		int mediumGridSize = res.getDimensionPixelSize(R.dimen.medium_photo_side);
		int listHeightSize = res.getDimensionPixelSize(R.dimen.flickr_list_item_height);
		int screenWidth = getScreenWidth();

		if (savedInstanceState == null) {
			// Weight values determined experimentally by measuring the number of incurred GCs while
			// scrolling through the various photo grids/lists.
			Glide.get(this)
					.preFillBitmapPool(
							new PreFillType.Builder(smallGridSize).setWeight(1),
							new PreFillType.Builder(mediumGridSize).setWeight(1),
							new PreFillType.Builder(screenWidth / 2, listHeightSize).setWeight(6));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.search_activity, menu);

		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSubmitButtonEnabled(true);
		searchView.setIconified(false);
		searchView.setOnQueryTextListener(this);

		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Api.get(this).unregisterSearchListener(queryListener);
		if (backgroundThumbnailFetcher != null) {
			backgroundThumbnailFetcher.cancel();
			backgroundThumbnailFetcher = null;
			backgroundThread.quit();
			backgroundThread = null;
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		executeSearch(query);
		searchView.setQuery("", false /*submit*/);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (currentQuery != null) {
			outState.putParcelable(STATE_QUERY, currentQuery);
		}
	}
}
