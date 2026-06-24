package com.makeus.mody.core.commonui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, E : UiIntent>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    private val _intents = Channel<E>(Channel.BUFFERED)
    private val _reducer = Channel<S.() -> S>(Channel.BUFFERED)

    init {
        _intents.receiveAsFlow()
            .onEach(::processIntent)
            .launchIn(viewModelScope)

        _reducer.receiveAsFlow()
            .onEach { reduce -> _state.value = currentState.reduce() }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: E) = viewModelScope.launch { _intents.send(intent) }

    protected abstract suspend fun processIntent(intent: E)

    protected fun setState(reduce: S.() -> S) =
        viewModelScope.launch { _reducer.send(reduce) }
}
