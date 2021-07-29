package com.cholomia.basicpaginationlib

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

abstract class BaseListViewModel<Item, Param>(
    hasEndOfPostView: Boolean,
    val refreshItemsOnGet: Boolean = true
) : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    private val _error = MutableLiveData<SingleEvent<Throwable>>()
    val error: LiveData<SingleEvent<Throwable>> = _error

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    protected fun onError(throwable: Throwable) {
        Timber.e(throwable)
        _error.value = SingleEvent(throwable)
    }


    private val paramLive = MutableLiveData<Param>()
    private val _items = MutableLiveData<List<Item>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _enableLoadMore = MutableLiveData<Boolean>()

    val items: LiveData<List<ListItemView<Item>>> = LiveDataExt.generateLiveListItems(
        _enableLoadMore = _enableLoadMore,
        _items = _items,
        hasEndOfPost = hasEndOfPostView
    )
    val isLoading: LiveData<Boolean> = _isLoading
    val showEmptyState: LiveData<Boolean> = LiveDataExt.generateShowEmptyState(_isLoading, _items)
    val showLoadingShimmer: LiveData<Boolean> = LiveDataExt.generateShowLoading(_isLoading, _items)

    val currentParam: Param?
        get() = paramLive.value

    private val getAction = PublishSubject.create<Param>()

    /**
     * @return flowable list of items
     */
    protected abstract fun queryLocal(param: Param): Flowable<List<Item>>

    /**
     * @return single true if enable load more, false otherwise.
     */
    protected abstract fun queryRemote(param: Param, isPullToRefresh: Boolean): Single<Boolean>

    init {
        getAction
            .distinctUntilChanged()
            .toFlowable(BackpressureStrategy.LATEST)
            .doOnNext { if (refreshItemsOnGet) fetchItems(true) }
            .switchMap { queryLocal(it) }
            .subscribe({
                _items.value = it
            }, Timber::e)
            .addTo(disposables)
    }

    fun getItems(param: Param) {
        paramLive.value = param
        getAction.onNext(param)
    }

    fun fetchItems(isPullToRefresh: Boolean) {
        if (!isPullToRefresh && _enableLoadMore.value == false) return // ignore
        val param = paramLive.value
            ?: throw IllegalStateException("BaseListViewModel.getItems(param) not yet called")
        queryRemote(param, isPullToRefresh)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { if (isPullToRefresh) _isLoading.value = true }
            .doFinally { _isLoading.value = false }
            .subscribe({
                _enableLoadMore.value = it
            }, {
                _enableLoadMore.value = false
                onError(it)
            })
            .addTo(disposables)
    }

}