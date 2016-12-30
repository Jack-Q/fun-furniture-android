package com.jackq.funfurniture.API;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.jackq.funfurniture.R;
import com.jackq.funfurniture.model.Furniture;
import com.jackq.funfurniture.model.FurnitureDetail;
import com.jackq.funfurniture.model.FurnitureModel;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class APIServer {
    private static String mAPIHostName = null;
    private static final String API_PATH = "api/";
    private static final String API_LIST = "list";
    private static final String API_ITEM = "item";
    private static final String API_MODEL = "model";

    public interface APIServerCallback<T> {
        void onResource(T resource);

        void onError(Exception e);
    }

    public static String getFullListUrl(Context ctx) {
        return getAPIHostName(ctx) + API_PATH + API_LIST;
    }

    public static void getFullList(Context ctx, final APIServerCallback<List<Furniture>> callback) {
        Ion.with(ctx).load(getFullListUrl(ctx)).as(new TypeToken<List<Furniture>>() {
        }).setCallback(getCallback(callback, new ArrayList<Furniture>()));
    }

    public static String getItemListUrl(Context ctx, int categoryCode) {
        return getAPIHostName(ctx) + API_PATH + API_LIST + "?cat=" + categoryCode;
    }

    public static void getItemList(Context ctx, int categoryCode, final APIServerCallback<List<Furniture>> callback) {
        Ion.with(ctx).load(getItemListUrl(ctx, categoryCode)).as(new TypeToken<List<Furniture>>() {
        }).setCallback(getCallback(callback, new ArrayList<Furniture>()));
    }

    public static String getItemDetailUrl(Context ctx, int itemId) {
        return getAPIHostName(ctx) + API_PATH + API_ITEM + "?id=" + itemId;
    }

    public static void getItemDetail(Context ctx, int itemId, final APIServerCallback<FurnitureDetail> callback) {
        Ion.with(ctx).load(getItemDetailUrl(ctx, itemId)).as(new TypeToken<FurnitureDetail>() {
        }).setCallback(getCallback(callback));
    }

    public static String getItemModelUrl(Context ctx, int modelId) {
        return getAPIHostName(ctx) + API_PATH + API_MODEL + "?id=" + modelId;
    }

    public static void getItemModel(Context ctx, int modelId, final APIServerCallback<FurnitureModel> callback) {
        Ion.with(ctx).load(getItemModelUrl(ctx, modelId)).as(new TypeToken<FurnitureModel>() {
        }).setCallback(getCallback(callback));
    }


    private static String getAPIHostName(Context ctx) {
        if (mAPIHostName == null)
            mAPIHostName = ctx.getString(R.string.APIHostName);
        return mAPIHostName;
    }


    private static <T> FutureCallback<T> getCallback(final APIServerCallback<T> callback) {
        return getCallback(callback, null);
    }

    private static <T> FutureCallback<T> getCallback(final APIServerCallback<T> callback, final T emptyAlternation) {
        return new FutureCallback<T>() {
            @Override
            public void onCompleted(Exception e, T result) {
                if (e != null) {
                    callback.onError(e);
                    return;
                }
                if (result == null) {
                    callback.onResource(emptyAlternation);
                    return;
                }
                callback.onResource(result);
            }
        };
    }
}
