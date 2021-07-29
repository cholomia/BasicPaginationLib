package com.cholomia.basicpaginationlib

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * supports vertical list only for the meantime
 */
abstract class BaseListFragment<Item, Param>(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {


    data class Views(
        val refreshLayout: SwipeRefreshLayout,
        val recyclerView: RecyclerView,
        val orientation: Orientation,
        val reverseLayout: Boolean,
        val loadingView: View,
        val emptyState: View,
        val containedEndOfList: View?,
        val paramLoadedOnInit: Boolean
    ) {

        enum class Orientation {
            VERTICAL,
            HORIZONTAL
        }

    }

    abstract fun getViews(): Views

    abstract fun getVM(): BaseListViewModel<Item, Param>

    abstract fun getParam(): Param

    abstract fun errorHandler(error: Throwable)

    abstract fun initialize(param: Param, items: LiveData<List<ListItemView<Item>>>)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val views = getViews()
        val viewModel = getVM()

        views.refreshLayout.setOnRefreshListener {
            viewModel.fetchItems(true)
        }

        val managerOrientation = when (views.orientation) {
            Views.Orientation.VERTICAL -> LinearLayoutManager.VERTICAL
            Views.Orientation.HORIZONTAL -> LinearLayoutManager.HORIZONTAL
        }
        views.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            managerOrientation,
            views.reverseLayout
        )

        views.recyclerView.itemAnimator = null
        views.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val canScroll = when (views.orientation) {
                    Views.Orientation.VERTICAL -> recyclerView.canScrollVertically(1)
                    Views.Orientation.HORIZONTAL -> recyclerView.canScrollHorizontally(1)
                }
                if (!canScroll) {
                    viewModel.fetchItems(false)
                }
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            views.refreshLayout.isRefreshing = isLoading
        }
        viewModel.showLoadingShimmer.observe(viewLifecycleOwner) { isLoading ->
            views.loadingView.isVisible = isLoading
        }
        viewModel.showEmptyState.observe(viewLifecycleOwner) { isEmpty ->
            views.emptyState.isVisible = isEmpty
        }
        viewModel.error.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { error ->
                errorHandler(error)
            }
        }

        if (views.paramLoadedOnInit) {
            val param = getParam()
            viewModel.getItems(param)
            if (!viewModel.refreshItemsOnGet) {
                // manually call if not automatic
                viewModel.fetchItems(true)
            }
            initialize(param, viewModel.items)
        }
    }

}