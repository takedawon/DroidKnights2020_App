package com.droidknights.app2020.ui.schedule

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.droidknights.app2020.base.BaseViewModel
import com.droidknights.app2020.base.DispatcherProvider
import com.droidknights.app2020.common.Event
import com.droidknights.app2020.data.Tag
import com.droidknights.app2020.db.SessionRepository
import com.droidknights.app2020.ui.model.UiSessionModel
import com.droidknights.app2020.ui.model.asUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by jiyoung on 04/12/2019
 */
class ScheduleViewModel @ViewModelInject constructor(
    private val dispatchers: DispatcherProvider,
    private val repo: SessionRepository
) : BaseViewModel() {

    private val _loadingEvent = MutableLiveData(LoadingType.NORMAL)
    val sessionList: LiveData<List<UiSessionModel>> = _loadingEvent.switchMap { loadingType ->
        liveData {
            val sessions = repo.get(isCacheFirstLoad = loadingType == LoadingType.NORMAL)
                .map {
                    if (allTags.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            allTags =
                                sequence { it.map { sessions -> yieldAll(sessions.asUiModel().tag.orEmpty()) } }
                                    .distinctBy { s -> s }
                                    .toList()
                        }
                    }

                    it.map { session -> session.asUiModel() }
                }

            emitSource(
                sessions
                    .flowOn(dispatchers.default())
                    .asLiveData()
            )
        }
    }

    val isRefreshing: LiveData<Boolean> = sessionList.map { false }

    val selectedTags: List<Tag> get() = allTags.filter { it.isSelected }

    var allTags: List<Tag> = emptyList()

    private val _itemEvent = MutableLiveData<Event<String>>()
    val itemEvent: LiveData<Event<String>> get() = _itemEvent

    private val _fabEvent = MutableLiveData<Event<String>>()
    val fabEvent: LiveData<Event<String>> get() = _fabEvent

    private val _submitEvent = MutableLiveData<Event<String>>()
    val submitEvent: LiveData<Event<String>> get() = _submitEvent

    init {
        loading(LoadingType.NORMAL)
    }

    fun refresh() {
        loading(LoadingType.FORCE)
    }

    fun onClickItem(sessionId: String) {
        _itemEvent.value = Event(sessionId)
    }

    fun onClickFilter() {
        _fabEvent.value = Event("")
    }

    fun onClickSubmit() {
        _submitEvent.value = Event("")
    }

    private fun loading(loadingType: LoadingType) {
        _loadingEvent.value = loadingType
    }
}

private enum class LoadingType {
    NORMAL, FORCE
}