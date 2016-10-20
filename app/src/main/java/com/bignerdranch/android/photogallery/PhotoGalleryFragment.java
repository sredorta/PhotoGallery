package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sredorta on 10/13/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "SERGI::PhotoGalleryFr";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private FetchItemsTask task;
    private List<GalleryItem> mGalleryItems;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;


    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        task = updateItems();
        //Start background service through an alarm
        //Intent i = PollService.newIntent(getActivity());
        //getActivity().startService(i);
        //PollService.setServiceAlarm(getActivity(),true);  -> Use instead the menu
        //Create the thumbnail downloader
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDonwloadListener(new  ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                photoHolder.bindDrawable(drawable);
            }
        });
        QueryPreferences.setPageNumber(getActivity(),1);
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        //Set pageNumber default
        int pageNumber = 1;
        QueryPreferences.setPageNumber(getActivity(),pageNumber);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pageNumber = QueryPreferences.getPageNumber(getActivity());
                //Toast.makeText(getActivity(),"dx : " + dx + "\ndn :" + dy, Toast.LENGTH_LONG).show();
                GridLayoutManager gm = (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();
                int visibleItemCount = mPhotoRecyclerView.getChildCount();
                int totalItemCount = gm.getItemCount();
                int firstVisibleItem = gm.findFirstVisibleItemPosition();
                int lastVisibleItem = gm.findLastVisibleItemPosition();
                int lastItemThreshold = (Integer.valueOf(FlickrFetchr.PHOTOS_PER_PAGE)-1) * pageNumber ;

                //If we are in last position then we need to fetch next page
                if (lastVisibleItem >= lastItemThreshold) {
                    Toast.makeText(getActivity(),"Reached end of page !" + pageNumber, Toast.LENGTH_LONG).show();

                    //Store pageNumber in preferences
                    pageNumber = QueryPreferences.getPageNumber(getActivity()) + 1;
                    QueryPreferences.setPageNumber(getActivity(), pageNumber);

                    task.cancel(true);
                    task = updateItems();
                }
            }
        });
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Exit the downloader looper
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background task destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchview = (SearchView) searchItem.getActionView();

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Store on preferences the query
                QueryPreferences.setStoredQuery(getActivity(),s);
                //Set that we need first page as we submit new query
                QueryPreferences.setPageNumber(getActivity(), 1);
                //Update recycleView
                task = updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchview.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchview.setQuery(query,false);
            }
        });
        //Handle menu item of alarm service
        MenuItem toggleItem = (MenuItem) menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                //Set that we need first page as we submit new query
                QueryPreferences.setPageNumber(getActivity(), 1);
                task = updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private FetchItemsTask updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        return (FetchItemsTask) new FetchItemsTask(query).execute();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;


        public PhotoHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.list_item_photo, container, false));
            mItemImageView = (ImageView) itemView.findViewById(R.id.list_photo_gallery_image_view);
            itemView.setOnClickListener(this);
            //itemView.setOnClickListener(this)
        }
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            //When an item is click we open the browser
            //Intent i = new Intent(Intent.ACTION_VIEW,mGalleryItem.getPhotoPageUri());
            //startActivity(i);
            //Instead start PhotoPageActivity
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PhotoHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
            //photoHolder.bindGalleryItem(galleryItem);
            Drawable placeHolder = getResources().getDrawable(R.drawable.myphoto);
            photoHolder.bindDrawable(placeHolder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }



    //Get data from website
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            int pageNumber = QueryPreferences.getPageNumber(getActivity());
            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(pageNumber);
            } else {
                return new FlickrFetchr().searchPhotos(mQuery,pageNumber);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems =galleryItems;
            setupAdapter();
        }
    }




}
