package com.cholomia.basicpaginationlib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T, out VH : BaseListAdapter.BaseListViewHolder<T>>(
    @LayoutRes private val loadingLayoutRes: Int,
    @LayoutRes private val endViewLayoutRes: Int?,
    diffCallback: BaseListViewDiffCallback<T>
) : ListAdapter<ListItemView<T>, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private const val VIEW_ITEM = 1
        private const val VIEW_LOADING = 2
        private const val VIEW_END = 3
    }

    abstract fun inflateItemView(parent: ViewGroup): RecyclerView.ViewHolder

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItemView.ItemView -> VIEW_ITEM
        ListItemView.LoadingView -> VIEW_LOADING
        ListItemView.EndView -> VIEW_END
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = when (viewType) {
        VIEW_ITEM -> inflateItemView(parent)
        VIEW_LOADING -> SimpleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                loadingLayoutRes,
                parent,
                false
            )
        )
        VIEW_END -> SimpleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                endViewLayoutRes ?: throw IllegalStateException("VIEW_END is used but null"),
                parent,
                false
            )
        )
        else -> throw IllegalStateException("unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (getItem(position) as? ListItemView.ItemView)?.let {
            (holder as VH).bind(it.item)
        }
    }

    abstract class BaseListViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(item: T)

    }

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)

}