package com.jackq.funfurniture;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jackq.funfurniture.FurnitureListItemFragment.OnListFragmentInteractionListener;
import com.jackq.funfurniture.model.Furniture;
import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Furniture} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFurnitureListItemRecyclerViewAdapter extends RecyclerView.Adapter<MyFurnitureListItemRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "LIST_ITEM_ADAPTER";
    private final List<Furniture> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;

    public MyFurnitureListItemRecyclerViewAdapter(Context context, List<Furniture> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_furniturelistitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Furniture furniture = mValues.get(position);
        holder.mItem = furniture;
        holder.mItemName.setText(furniture.getName());
        holder.mItemPrice.setText(String.format(Locale.ENGLISH, "%.2f", furniture.getPrice()));
        holder.mItemDescription.setText(furniture.getDescription());
        //holder.mItemImage.setImageURI();
        if(furniture.getPictures() != null && furniture.getPictures().size() > 0){
            Log.d(TAG, "Load image" + furniture.getPictures().get(0));
            Ion.with(mContext).load(furniture.getPictures().get(0)).withBitmap().placeholder(R.drawable.logo_main).intoImageView(holder.mItemImage);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public Furniture mItem;
        public final ImageView mItemImage;
        public final TextView mItemName;
        public final AppCompatRatingBar mItemRate;
        public final TextView mItemPrice;
        public final TextView mItemDescription;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mItemImage = (ImageView) view.findViewById(R.id.list_item_image);
            mItemName = (TextView) view.findViewById(R.id.list_item_name);
            mItemRate = (AppCompatRatingBar) view.findViewById(R.id.list_item_rate);
            mItemPrice = (TextView) view.findViewById(R.id.list_item_price);
            mItemDescription = (TextView) view.findViewById(R.id.list_item_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItemName.getText() + "'";
        }
    }
}
