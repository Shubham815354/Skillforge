package com.example.skillforge.ui.courselist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillforge.R
import com.example.skillforge.data.model.Course
import com.example.skillforge.databinding.FragmentCourseListBinding
import com.example.skillforge.ui.catalog.CatalogViewModel
import com.example.skillforge.ui.common.CourseRowAdapter

class CourseListFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by activityViewModels { CatalogViewModel.Factory }
    private val categoryId: String? by lazy { requireArguments().getString("categoryId") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CourseRowAdapter { courseId ->
            findNavController().navigate(
                R.id.action_courseList_to_courseDetail,
                bundleOf("courseId" to courseId)
            )
        }
        binding.coursesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.coursesRecyclerView.adapter = adapter
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        val id = categoryId
        val courses: List<Course>
        if (id != null) {
            val category = viewModel.categories().firstOrNull { it.id == id }
            binding.screenTitle.text = category?.name ?: getString(R.string.error_category_not_found)
            courses = viewModel.coursesForCategory(id)
        } else {
            binding.screenTitle.text = getString(R.string.popular_courses_header)
            courses = viewModel.allCourses().sortedByDescending { it.rating }
        }

        adapter.submit(courses)
        binding.emptyText.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
        binding.coursesRecyclerView.visibility = if (courses.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
