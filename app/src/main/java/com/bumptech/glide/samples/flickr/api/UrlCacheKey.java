package com.bumptech.glide.samples.flickr.api;

public class UrlCacheKey {

	private final Photo photo;
	private final String sizeKey;

	public UrlCacheKey(Photo photo, String sizeKey) {
		this.photo = photo;
		this.sizeKey = sizeKey;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UrlCacheKey) {
			UrlCacheKey other = (UrlCacheKey) o;
			return photo.equals(other.photo) && sizeKey.equals(other.sizeKey);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = photo.hashCode();
		result = 31 * result + sizeKey.hashCode();
		return result;
	}
}
