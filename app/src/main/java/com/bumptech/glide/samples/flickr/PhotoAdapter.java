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

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.samples.flickr.PhotoAdapter.PhotoViewHolder;
import com.bumptech.glide.samples.flickr.api.Photo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder>
		implements ListPreloader.PreloadModelProvider<Photo> {

	static class PhotoViewHolder extends RecyclerView.ViewHolder {

		public final ImageView imageView;

		PhotoViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView;
		}
	}

	private Context context;
	private int photoSize;
	private List<Photo> photos = Collections.emptyList();
	private boolean thumbnail;

	public PhotoAdapter(int photoSize, boolean thumbnail) {
		this.photoSize = photoSize;
		this.thumbnail = thumbnail;
	}

	@Override
	public int getItemCount() {
		return photos.size();
	}

	@Override
	public long getItemId(int i) {
		return RecyclerView.NO_ID;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@NonNull
	@Override
	public List<Photo> getPreloadItems(int position) {
		return photos.subList(position, position + 1);
	}

	@Nullable
	@Override
	public RequestBuilder<?> getPreloadRequestBuilder(@NonNull Photo item) {
		return Glide.with(context).asDrawable().centerCrop().load(item);
	}

	@Override
	public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
		final Photo current = photos.get(position);
		Glide.with(context).asDrawable().centerCrop()
				.load(current)
				.thumbnail(thumbnail ? Glide.with(context).load(current) : null)
				.into(holder.imageView);

		holder.imageView.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = FullscreenActivity.getIntent(context, current);
						context.startActivity(intent);
					}
				});
	}

	@NonNull
	@Override
	public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		context = parent.getContext();
		View view = LayoutInflater.from(context).inflate(R.layout.flickr_photo_grid_item, parent, false);
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = photoSize;
		params.height = photoSize;
		return new PhotoViewHolder(view);
	}

	void setPhotos(List<Photo> photos) {
		this.photos = photos;
		notifyDataSetChanged();
	}
}
