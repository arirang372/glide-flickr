package com.bumptech.glide.samples.flickr;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.samples.flickr.api.Api;
import com.bumptech.glide.samples.flickr.api.Photo;

public class BackgroundThumbnailFetcher implements Runnable  {
	private final Context context;
	private final List<Photo> photos;

	private boolean isCancelled;

	public BackgroundThumbnailFetcher(Context context, List<Photo> photos) {
		this.context = context;
		this.photos = photos;
	}

	void cancel() {
		isCancelled = true;
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
		for (Photo photo : photos) {
			if (isCancelled) {
				return;
			}

			FutureTarget<File> futureTarget =
					Glide.with(context)
							.downloadOnly()
							.load(photo)
							.submit(Api.SQUARE_THUMB_SIZE, Api.SQUARE_THUMB_SIZE);

			try {
				futureTarget.get();
			} catch (InterruptedException e) {
				if (Log.isLoggable(FlickrSearchActivity.TAG, Log.DEBUG)) {
					Log.d(FlickrSearchActivity.TAG, "Interrupted waiting for background downloadOnly", e);
				}
			} catch (ExecutionException e) {
				if (Log.isLoggable(FlickrSearchActivity.TAG, Log.DEBUG)) {
					Log.d(FlickrSearchActivity.TAG, "Got ExecutionException waiting for background downloadOnly", e);
				}
			}
			Glide.with(context).clear(futureTarget);
		}
	}
}
