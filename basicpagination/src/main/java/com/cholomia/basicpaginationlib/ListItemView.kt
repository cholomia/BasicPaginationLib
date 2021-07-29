package com.cholomia.basicpaginationlib

sealed class ListItemView<out T> {

    data class ItemView<T>(val item: T) : ListItemView<T>()

    object LoadingView : ListItemView<Nothing>()

    object EndView : ListItemView<Nothing>()

}
