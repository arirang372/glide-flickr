package com.bumptech.glide.samples.flickr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.samples.flickr.FlickrPhotoListAdapter.PhotoTitleViewHolder;
import com.bumptech.glide.samples.flickr.api.Photo;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.List;

/**
 * A fragment that shows cropped image thumbnails half the width of the screen in a scrolling list.
 */
public class FlickrPhotoListFragment extends Fragment implements PhotoViewer {

    private static final int PRELOAD_AHEAD_ITEMS = 5;
    private static final String STATE_POSITION_INDEX = "state_position_index";
    private static final String STATE_POSITION_OFFSET = "state_position_offset";

    public static FlickrPhotoListFragment newInstance() {
        return new FlickrPhotoListFragment();
    }

    private FlickrPhotoListAdapter adapter;
    private List<Photo> currentPhotos;
    private LinearLayoutManager layoutManager;
    private RecyclerView list;
    private ViewPreloadSizeProvider<Photo> preloadSizeProvider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.flickr_photo_list, container, false);
        preloadSizeProvider = new ViewPreloadSizeProvider<>();

        final GlideRequests glideRequests = GlideApp.with(this);
        list = result.findViewById(R.id.flickr_photo_list);
        layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);
        adapter = new FlickrPhotoListAdapter();
        list.setAdapter(adapter);

        RecyclerViewPreloader<Photo> preloader =
                new RecyclerViewPreloader<>(
                        GlideApp.with(this), adapter, preloadSizeProvider, PRELOAD_AHEAD_ITEMS);
        list.addOnScrollListener(preloader);
        list.setItemViewCacheSize(0);

        if (currentPhotos != null) {
            adapter.setPhotos(currentPhotos);
        }

        list.setRecyclerListener(
                new RecyclerView.RecyclerListener() {
                    @Override
                    public void onViewRecycled(RecyclerView.ViewHolder holder) {
                        PhotoTitleViewHolder vh = (PhotoTitleViewHolder) holder;
                        glideRequests.clear(vh.imageView);
                    }
                });

        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt(STATE_POSITION_INDEX);
            int offset = savedInstanceState.getInt(STATE_POSITION_OFFSET);
            layoutManager.scrollToPositionWithOffset(index, offset);
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
        if (list != null) {
            int index = layoutManager.findFirstVisibleItemPosition();
            View topView = list.getChildAt(0);
            int offset = topView != null ? topView.getTop() : 0;
            outState.putInt(STATE_POSITION_INDEX, index);
            outState.putInt(STATE_POSITION_OFFSET, offset);
        }
    }
}
