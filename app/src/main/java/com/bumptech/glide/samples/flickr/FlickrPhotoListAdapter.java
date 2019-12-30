package com.bumptech.glide.samples.flickr;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.samples.flickr.FlickrPhotoListAdapter.PhotoTitleViewHolder;
import com.bumptech.glide.samples.flickr.api.Photo;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FlickrPhotoListAdapter extends RecyclerView.Adapter<PhotoTitleViewHolder>
		implements ListPreloader.PreloadModelProvider<Photo> {

	static final class PhotoTitleViewHolder extends RecyclerView.ViewHolder {

		public final ImageView imageView;
		public final TextView titleView;

		PhotoTitleViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.photo_view);
			titleView = itemView.findViewById(R.id.title_view);
		}
	}

	private Context context;
	private GlideRequest<Drawable> fullRequest;
	private List<Photo> photos = Collections.emptyList();
	private ViewPreloadSizeProvider<Photo> preloadSizeProvider;
	private GlideRequest<Drawable> thumbRequest;

	public FlickrPhotoListAdapter(ViewPreloadSizeProvider<Photo> preloadSizeProvider, GlideRequest<Drawable> fullRequest, GlideRequest<Drawable> thumbRequest) {
		this.preloadSizeProvider = preloadSizeProvider;
		this.fullRequest = fullRequest;
		this.thumbRequest = thumbRequest;
	}

	@Override
	public int getItemCount() {
		return photos.size();
	}

	@NonNull
	@Override
	public List<Photo> getPreloadItems(int position) {
		return photos.subList(position, position + 1);
	}

	@Nullable
	@Override
	public RequestBuilder<?> getPreloadRequestBuilder(@NonNull Photo item) {
		return null;
	}

	@Override
	public void onBindViewHolder(@NonNull PhotoTitleViewHolder holder, int position) {
		final Photo current = photos.get(position);
		fullRequest.load(current).thumbnail(thumbRequest.load(current)).into(holder.imageView);
		holder.imageView.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = FullscreenActivity.getIntent(context, current);
						context.startActivity(intent);
					}
				});

		holder.titleView.setText(current.getTitle());
	}

	@NonNull
	@Override
	public PhotoTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		context = parent.getContext();
		View view = LayoutInflater.from(context).inflate(R.layout.flickr_photo_list_item, parent, false);
		PhotoTitleViewHolder vh = new PhotoTitleViewHolder(view);
		preloadSizeProvider.setView(vh.imageView);
		return vh;
	}

	void setPhotos(List<Photo> photos) {
		this.photos = photos;
		notifyDataSetChanged();
	}
}
