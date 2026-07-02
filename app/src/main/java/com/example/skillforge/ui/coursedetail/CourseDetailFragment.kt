package com.example.skillforge.ui.coursedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.skillforge.R
import com.example.skillforge.data.model.Course
import com.example.skillforge.databinding.FragmentCourseDetailBinding
import com.example.skillforge.ui.catalog.CatalogViewModel
import com.example.skillforge.ui.common.LessonAdapter
import com.example.skillforge.util.formatDurationHoursShort
import com.example.skillforge.util.formatDurationMinutes
import com.example.skillforge.util.levelAccentColor
import java.util.Locale

class CourseDetailFragment : Fragment() {

    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by activityViewModels { CatalogViewModel.Factory }
    private val courseId: String by lazy { requireArguments().getString("courseId").orEmpty() }
    private lateinit var lessonAdapter: LessonAdapter

    private var isFollowing = false
    private var isBookmarked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lessonAdapter = LessonAdapter { lessonId ->
            findNavController().navigate(
                R.id.action_courseDetail_to_lesson,
                bundleOf("courseId" to courseId, "lessonId" to lessonId)
            )
        }
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.lessonsRecyclerView.adapter = lessonAdapter
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.retryButton.setOnClickListener { findNavController().navigateUp() }
        binding.bookmarkButton.setOnClickListener {
            isBookmarked = !isBookmarked
            binding.bookmarkButton.alpha = if (isBookmarked) 1f else 0.6f
        }
        binding.followButton.setOnClickListener {
            isFollowing = !isFollowing
            binding.followButton.text = getString(if (isFollowing) R.string.following else R.string.follow)
        }
        binding.enrollButton.setOnClickListener { /* No enrollment backend in this demo. */ }

        val bottomBarBasePadding = binding.bottomBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { view, insets ->
            val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomBarBasePadding + navBarBottom)
            insets
        }

        binding.loadingIndicator.visibility = View.GONE

        val course = viewModel.findCourse(courseId)
        if (course == null) {
            binding.contentScroll.visibility = View.GONE
            binding.errorGroup.visibility = View.VISIBLE
            binding.errorMessage.text = getString(R.string.error_course_not_found)
        } else {
            binding.contentScroll.visibility = View.VISIBLE
            binding.errorGroup.visibility = View.GONE
            renderCourse(course)
        }
    }

    private fun renderCourse(course: Course) {
        binding.courseHero.load(course.thumbnailUrl)
        binding.heroTag.text = getString(
            R.string.hero_tag_format,
            (course.tags.firstOrNull() ?: course.level).lowercase()
        )
        binding.heroTitle.text = course.title
        bindHeroTags(course.tags)

        binding.courseTitle.text = course.title
        binding.courseSubtitle.text = course.subtitle
        binding.ratingText.text = course.rating.toString()
        binding.studentsText.text = String.format(Locale.US, "%,d", course.studentsEnrolled)
        binding.durationText.text = formatDurationHoursShort(course.durationHours)
        binding.levelText.text = course.level
        binding.levelText.setTextColor(levelAccentColor(course.level, requireContext()))

        binding.instructorAvatar.load(course.instructor.avatarUrl)
        binding.instructorName.text = course.instructor.name
        binding.instructorTitle.text = course.instructor.title

        binding.courseDescription.text = course.description

        val totalMinutes = course.lessons.sumOf { it.durationMinutes }
        binding.courseContentMeta.text = getString(
            R.string.course_content_meta_format,
            course.lessons.size,
            formatDurationMinutes(totalMinutes)
        )
        lessonAdapter.submit(course.lessons)
    }

    private fun bindHeroTags(tags: List<String>) {
        val container = binding.heroTagsContainer
        container.removeAllViews()
        val marginPx = (8 * resources.displayMetrics.density).toInt()
        tags.forEach { tag ->
            val chip = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_hero_tag_chip, container, false)
            chip.findViewById<TextView>(R.id.tagLabel).text = tag
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = marginPx
            container.addView(chip, params)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
