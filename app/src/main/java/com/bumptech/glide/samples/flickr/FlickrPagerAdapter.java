package com.bumptech.glide.samples.flickr;

import android.content.Context;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import static com.bumptech.glide.samples.flickr.FlickrSearchActivity.PAGE_TO_TITLE;

public class FlickrPagerAdapter extends FragmentPagerAdapter {

	private Fragment mLastFragment;
	private int mLastPosition = -1;
	private Context context;

	public FlickrPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context;
	}

	@Override
	public int getCount() {
		return Page.values().length;
	}

	@Override
	public Fragment getItem(int position) {
		return pageToFragment(position);
	}

	private int getPageSize(int id) {
		return context.getResources().getDimensionPixelSize(id);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Page page = Page.values()[position];
		int titleId = PAGE_TO_TITLE.get(page);
		return context.getString(titleId);
	}

	private Fragment pageToFragment(int position) {
		Page page = Page.values()[position];
		if (page == Page.SMALL) {
			int pageSize = getPageSize(R.dimen.small_photo_side);
			return FlickrPhotoGridFragment.newInstance(pageSize, 15, false /*thumbnail*/);
		} else if (page == Page.MEDIUM) {
			int pageSize = getPageSize(R.dimen.medium_photo_side);
			return FlickrPhotoGridFragment.newInstance(pageSize, 10, true /*thumbnail*/);
		} else if (page == Page.LIST) {
			return FlickrPhotoListFragment.newInstance();
		} else {
			throw new IllegalArgumentException("No fragment class for page=" + page);
		}
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		if (position != mLastPosition) {
			if (mLastPosition >= 0) {
				Glide.with(mLastFragment).pauseRequests();
			}
			Fragment current = (Fragment) object;
			mLastPosition = position;
			mLastFragment = current;
			if (current.isAdded()) {
				Glide.with(current).resumeRequests();
			}
		}
	}
}
