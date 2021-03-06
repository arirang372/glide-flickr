package com.bumptech.glide.samples.flickr;

import java.util.List;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.samples.flickr.PhotoAdapter.PhotoViewHolder;
import com.bumptech.glide.samples.flickr.api.Photo;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.bumptech.glide.util.Preconditions;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A fragment that shows square image thumbnails whose size is determined by the fragment's
 * arguments in a grid pattern.
 */
public class FlickrPhotoGridFragment extends Fragment implements PhotoViewer {

	private static final String IMAGE_SIZE_KEY = "image_size";
	private static final String PRELOAD_KEY = "preload";
	private static final String STATE_POSITION_INDEX = "state_position_index";
	private static final String THUMBNAIL_KEY = "thumbnail";

	public static FlickrPhotoGridFragment newInstance(int size, int preloadCount, boolean thumbnail) {
		FlickrPhotoGridFragment photoGrid = new FlickrPhotoGridFragment();
		Bundle args = new Bundle();
		args.putInt(IMAGE_SIZE_KEY, size);
		args.putInt(PRELOAD_KEY, preloadCount);
		args.putBoolean(THUMBNAIL_KEY, thumbnail);
		photoGrid.setArguments(args);
		return photoGrid;
	}

	private PhotoAdapter adapter;
	private List<Photo> currentPhotos;
	private RecyclerView grid;
	private GridLayoutManager layoutManager;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle args = Preconditions.checkNotNull(getArguments());
		int photoSize = args.getInt(IMAGE_SIZE_KEY);
		boolean thumbnail = args.getBoolean(THUMBNAIL_KEY);

		final View result = inflater.inflate(R.layout.flickr_photo_grid, container, false);

		final int gridMargin = getResources().getDimensionPixelOffset(R.dimen.grid_margin);
		int spanCount = getResources().getDisplayMetrics().widthPixels / (photoSize + (2 * gridMargin));
		grid = result.findViewById(R.id.flickr_photo_grid);
		layoutManager = new GridLayoutManager(getActivity(), spanCount);
		grid.setLayoutManager(layoutManager);

		grid.addItemDecoration(
				new RecyclerView.ItemDecoration() {
					@Override
					public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
						outRect.set(gridMargin, gridMargin, gridMargin, gridMargin);
					}
				});
		grid.setRecyclerListener(
				new RecyclerView.RecyclerListener() {
					@Override
					public void onViewRecycled(RecyclerView.ViewHolder holder) {
						PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
						GlideApp.with(FlickrPhotoGridFragment.this).clear(photoViewHolder.imageView);
					}
				});

		int heightCount = getResources().getDisplayMetrics().heightPixels / photoSize;
		grid.getRecycledViewPool().setMaxRecycledViews(0, spanCount * heightCount * 2);
		grid.setItemViewCacheSize(0);
		adapter = new PhotoAdapter(photoSize, thumbnail);
		grid.setAdapter(adapter);

		FixedPreloadSizeProvider<Photo> preloadSizeProvider = new FixedPreloadSizeProvider<>(photoSize, photoSize);
		RecyclerViewPreloader<Photo> preloader =
				new RecyclerViewPreloader<>( Glide.with(this), adapter, preloadSizeProvider, args.getInt(PRELOAD_KEY));
		grid.addOnScrollListener(preloader);

		if (currentPhotos != null) {
			adapter.setPhotos(currentPhotos);
		}

		if (savedInstanceState != null) {
			int index = savedInstanceState.getInt(STATE_POSITION_INDEX);
			grid.scrollToPosition(index);
		}

		return result;
	}

	@Override
	public void onPhotosUpdated(List<Photo> photos) {
		currentPhotos = photos;
		if (adapter != null) {
			adapter.setPhotos(currentPhotos);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (grid != null) {
			int index = layoutManager.findFirstVisibleItemPosition();
			outState.putInt(STATE_POSITION_INDEX, index);
		}
	}
}
