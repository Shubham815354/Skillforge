package com.example.skillforge.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.tabs.TabLayout
import com.example.skillforge.R
import com.example.skillforge.data.model.Course
import com.example.skillforge.data.model.Lesson
import com.example.skillforge.databinding.FragmentLessonBinding
import com.example.skillforge.ui.catalog.CatalogViewModel
import com.example.skillforge.ui.common.LessonAdapter
import com.example.skillforge.util.formatClock

class LessonFragment : Fragment() {

    private var _binding: FragmentLessonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by activityViewModels { CatalogViewModel.Factory }
    private val courseId: String by lazy { requireArguments().getString("courseId").orEmpty() }
    private val lessonId: String by lazy { requireArguments().getString("lessonId").orEmpty() }
    private lateinit var lessonsAdapter: LessonAdapter

    private var isPlaying = false
    private var totalSeconds = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lessonsAdapter = LessonAdapter { newLessonId ->
            findNavController().navigate(
                R.id.action_lesson_to_lesson,
                bundleOf("courseId" to courseId, "lessonId" to newLessonId)
            )
        }
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.lessonsRecyclerView.adapter = lessonsAdapter
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.retryButton.setOnClickListener { findNavController().navigateUp() }
        binding.fullscreenButton.setOnClickListener { /* No real video surface in this demo. */ }

        binding.lessonTabs.addTab(binding.lessonTabs.newTab().setText(R.string.tab_lessons))
        binding.lessonTabs.addTab(binding.lessonTabs.newTab().setText(R.string.tab_notes))
        binding.lessonTabs.addTab(binding.lessonTabs.newTab().setText(R.string.tab_resources))
        binding.lessonTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = showTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        val result = viewModel.findLesson(courseId, lessonId)
        if (result == null) {
            binding.contentScroll.visibility = View.GONE
            binding.errorGroup.visibility = View.VISIBLE
            binding.errorMessage.text = getString(R.string.error_lesson_not_found)
        } else {
            binding.contentScroll.visibility = View.VISIBLE
            binding.errorGroup.visibility = View.GONE
            renderLesson(result.first, result.second)
        }
    }

    private fun renderLesson(course: Course, lesson: Lesson) {
        binding.lessonHero.load(course.thumbnailUrl)
        binding.heroTag.text = getString(
            R.string.hero_tag_format,
            (course.tags.firstOrNull() ?: course.level).lowercase()
        )
        binding.heroGhostTitle.text = course.title

        val lessonIndex = course.lessons.indexOfFirst { it.id == lesson.id }.coerceAtLeast(0)
        binding.lessonNumberLabel.text = getString(
            R.string.lesson_number_format,
            lessonIndex + 1,
            course.title
        )
        binding.lessonTitle.text = lesson.title
        binding.lessonContent.text = lesson.content

        totalSeconds = lesson.durationMinutes * 60
        binding.totalTimeText.text = formatClock(totalSeconds)
        binding.elapsedTimeText.text = formatClock(0)
        binding.playerSeekBar.progress = 0

        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.elapsedTimeText.text = formatClock(totalSeconds * progress / 100)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        isPlaying = false
        updatePlayPauseIcon()
        binding.playPauseButton.setOnClickListener {
            isPlaying = !isPlaying
            updatePlayPauseIcon()
            val progress = if (isPlaying) 35 else 0
            binding.playerSeekBar.progress = progress
            binding.elapsedTimeText.text = formatClock(totalSeconds * progress / 100)
        }

        lessonsAdapter.submit(course.lessons, activeLessonId = lesson.id)
        showTab(binding.lessonTabs.selectedTabPosition)
    }

    private fun updatePlayPauseIcon() {
        binding.playPauseIcon.setImageResource(
            if (isPlaying) R.drawable.ic_pause_dark else R.drawable.ic_play_dark
        )
        binding.playPauseButton.contentDescription = getString(
            if (isPlaying) R.string.content_desc_pause else R.string.content_desc_play
        )
    }

    private fun showTab(position: Int) {
        binding.lessonsRecyclerView.visibility = if (position == 0) View.VISIBLE else View.GONE
        binding.notesEmptyText.visibility = if (position == 1) View.VISIBLE else View.GONE
        binding.resourcesEmptyText.visibility = if (position == 2) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
