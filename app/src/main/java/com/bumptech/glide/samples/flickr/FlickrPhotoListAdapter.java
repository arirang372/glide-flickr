package com.bumptech.glide.samples.flickr;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.samples.flickr.FlickrPhotoListAdapter.PhotoTitleViewHolder;
import com.bumptech.glide.samples.flickr.api.Api;
import com.bumptech.glide.samples.flickr.api.Photo;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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
	private List<Photo> photos = Collections.emptyList();

	public FlickrPhotoListAdapter() {
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
		Glide.with(context).asDrawable().centerCrop().placeholder(new ColorDrawable(Color.GRAY))
				.load(current)
				.thumbnail(Glide.with(context).asDrawable()
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.override(Api.SQUARE_THUMB_SIZE)
						.transition(withCrossFade())
						.load(current))
				.into(holder.imageView);
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
		new ViewPreloadSizeProvider<Photo>().setView(vh.imageView);
		return vh;
	}

	void setPhotos(List<Photo> photos) {
		this.photos = photos;
		notifyDataSetChanged();
	}
}
