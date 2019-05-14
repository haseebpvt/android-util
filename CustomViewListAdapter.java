
/*
 * Copyright (c) 2019, Sivasankaran KB 
 */


package com.sivasankarankb.android_util;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Enhanced List Adapter for Custom Views
 */
public class CustomViewListAdapter<T extends View> implements ListAdapter {

    HashSet<DataSetObserver> dsoHashSet;
    List<T> viewList;

    public CustomViewListAdapter() {
        dsoHashSet=new HashSet<DataSetObserver>();
        viewList=new Vector<T>(1,1);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return ((i<viewList.size())&&(i>=0));
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        dsoHashSet.add(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        dsoHashSet.remove(dataSetObserver);
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public Object getItem(int i) {
        return viewList.get(i).toString();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return viewList.get(i);
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return viewList.isEmpty();
    }

    /**
     * Call(ed) when the list has changed.
     */
    public void listChanged(){
        for (DataSetObserver x: dsoHashSet) {
            x.onChanged();
        }
    }

    /**
     * Call(ed) when the list data has become invalid.
     */
    public void listInvalid(){
        for (DataSetObserver x: dsoHashSet) {
            x.onInvalidated();
        }
    }

    /**
     * Add a view of type T to the list.
     * @param v The view to add
     */
    public void addItem(T v){

        if(v==null) return;

        viewList.add(v);
        listChanged();
    }

    /**
     * Remove a view from the list.
     * @param v The view to remove
     */
    public void removeItem(T v){

        if(v==null) return;

        viewList.remove(v);
        listChanged();
    }

    /**
     * Clear the list of all views.
     */
    public void clearList(){
        viewList.clear();
        listChanged();
    }
}
