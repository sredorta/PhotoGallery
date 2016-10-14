package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sredorta on 10/13/2016.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "SERGI::PhotoGalleryFr";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int pageNumber = 1;
    private int pageNumberOld = 0;
    private FetchItemsTask task;
    private List<GalleryItem> mGalleryItems;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        task = (FetchItemsTask) new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //Toast.makeText(getActivity(),"dx : " + dx + "\ndn :" + dy, Toast.LENGTH_LONG).show();
                GridLayoutManager gm = (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();
                int visibleItemCount = mPhotoRecyclerView.getChildCount();
                int totalItemCount = gm.getItemCount();
                int firstVisibleItem = gm.findFirstVisibleItemPosition();
                int lastVisibleItem = gm.findLastVisibleItemPosition();
                int lastItemThreshold = (Integer.valueOf(FlickrFetchr.PHOTOS_PER_PAGE)-1) * pageNumber;

                //If we are in last position then we need to fetch next page
                if (lastVisibleItem >= lastItemThreshold) {
                    Toast.makeText(getActivity(),"Reached end of page !" + pageNumber, Toast.LENGTH_LONG).show();
                    pageNumber = pageNumber + 1;
                    task.cancel(true);
                    task = (FetchItemsTask) new FetchItemsTask().execute();
                }
            }
        });
        setupAdapter();
        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.list_item_photo, container, false));
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_photo_title);
            //itemView.setOnClickListener(this)
        }
        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
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
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }



    //Get data from website
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems(pageNumber);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems =galleryItems;
            setupAdapter();
        }
    }




}
