package com.cholomia.basicpaginationlib

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

object LiveDataExt {

    fun <T> generateShowLoading(
        _isLoading: LiveData<Boolean>,
        items: LiveData<List<T>>
    ) = MediatorLiveData<Boolean>().apply {
        var isLoading: Boolean? = null
        var isEmpty: Boolean? = null

        fun update() {
            if (isLoading != null && isEmpty != null) {
                this.value = isLoading == true && isEmpty == true
            }
        }

        addSource(_isLoading) {
            isLoading = it
            update()
        }
        addSource(items) {
            isEmpty = it.isEmpty()
            update()
        }
    }

    fun <T> generateShowEmptyState(
        _isLoading: LiveData<Boolean>,
        items: LiveData<List<T>>
    ) = MediatorLiveData<Boolean>().apply {
        var isLoading: Boolean? = null
        var isEmpty: Boolean? = null

        fun update() {
            if (isLoading != null && isEmpty != null) {
                this.value = isLoading == false && isEmpty == true
            }
        }

        addSource(_isLoading) {
            isLoading = it
            update()
        }
        addSource(items) {
            isEmpty = it.isEmpty()
            update()
        }
    }

    fun <T> generateLiveListItems(
        _enableLoadMore: LiveData<Boolean>,
        _items: LiveData<List<T>>,
        hasEndOfPost: Boolean,
    ) = MediatorLiveData<List<ListItemView<T>>>().apply {

        var enableLoadMore: Boolean? = null
        var items: List<T>? = null

        fun update() {
            items?.map {
                ListItemView.ItemView(it)
            }?.let {
                val listItemViews = mutableListOf<ListItemView<T>>()
                listItemViews.addAll(it)
                if (enableLoadMore == true) {
                    listItemViews.add(ListItemView.LoadingView)
                } else if (enableLoadMore == false && hasEndOfPost) {
                    listItemViews.add(ListItemView.EndView)
                }
                this.value = listItemViews
            }
        }

        addSource(_enableLoadMore) {
            enableLoadMore = it
            update()
        }
        addSource(_items) {
            items = it
            update()
        }
    }

}

