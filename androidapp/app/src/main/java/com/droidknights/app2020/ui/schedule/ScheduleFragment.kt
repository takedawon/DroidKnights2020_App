package com.droidknights.app2020.ui.schedule

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidknights.app2020.BR
import com.droidknights.app2020.R
import com.droidknights.app2020.base.BaseFragment
import com.droidknights.app2020.common.DataBindingAdapter
import com.droidknights.app2020.databinding.ScheduleFragmentBinding
import com.droidknights.app2020.ui.schedule.filter.ScheduleFilterFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Created by jiyoung on 04/12/2019
 */
@AndroidEntryPoint
class ScheduleFragment : BaseFragment<ScheduleEmptyViewModel, ScheduleFragmentBinding>(
    R.layout.schedule_fragment,
    ScheduleEmptyViewModel::class.java
) {
    private val TAG = this@ScheduleFragment::class.java.simpleName

    private val scheduleAdapter = ScheduleAdapter()

    val scheduleViewModel by activityViewModels<ScheduleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO : 세션 태그 필터링 기능
        //TODO : 관심세션 북마크 기능

        with(binding) {
            setVariable(BR.scheduleVm, scheduleViewModel)
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        initObserve()
    }

    private fun initView() {
        scheduleAdapter.apply {
            itemClickListener = object : DataBindingAdapter.ItemClickListener {
                override fun onClickItem(sessionId: String) {
                    scheduleViewModel.onClickItem(sessionId)
                }
            }
        }

        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = scheduleAdapter
        }
    }

    private fun initObserve() {
        scheduleViewModel.sessionList.observe(viewLifecycleOwner) { sessions ->
            binding.floatingFilter.isVisible = true

            sessions.filter { session ->
                scheduleViewModel.selectedTags.intersect(session.tag.orEmpty()).isNotEmpty()
            }.let(scheduleAdapter::submitList)
            Timber.d(TAG, "getSessionListData : $sessions")
        }

        scheduleViewModel.itemEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { sessionId ->
                val action = ScheduleFragmentDirections.actionScheduleToSessionDetail(sessionId)
                binding.root.findNavController().navigate(action)
            }
        }

        scheduleViewModel.fabEvent.observe(viewLifecycleOwner) { event ->
            binding.floatingFilter.isVisible = false

            val fragment = ScheduleFilterFragment()
            parentFragmentManager.beginTransaction()
                .addToBackStack(fragment::class.java.simpleName)
                .add(R.id.frameLayout, fragment)
                .commit()
        }
    }
}