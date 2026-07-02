package com.example.skillforge.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillforge.R
import com.example.skillforge.data.model.Category
import com.example.skillforge.data.model.Course
import com.example.skillforge.databinding.FragmentHomeBinding
import com.example.skillforge.ui.catalog.CatalogUiState
import com.example.skillforge.ui.catalog.CatalogViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by activityViewModels { CatalogViewModel.Factory }
    private lateinit var homeAdapter: HomeAdapter

    private var lastCategories: List<Category> = emptyList()
    private var lastCourses: List<Course> = emptyList()
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeAdapter = HomeAdapter(
            onCategoryClick = { categoryId ->
                findNavController().navigate(
                    R.id.action_home_to_courseList,
                    bundleOf("categoryId" to categoryId)
                )
            },
            onSeeAllCategories = {
                findNavController().navigate(R.id.action_home_to_categoryList)
            },
            onCourseClick = { courseId ->
                findNavController().navigate(
                    R.id.action_home_to_courseDetail,
                    bundleOf("courseId" to courseId)
                )
            },
            onSeeAllCourses = {
                findNavController().navigate(R.id.action_home_to_courseList)
            }
        )
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRecyclerView.adapter = homeAdapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadCatalog() }
        binding.retryButton.setOnClickListener { viewModel.loadCatalog() }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty()
                renderList()
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: CatalogUiState) {
        binding.swipeRefresh.isRefreshing = false
        when (state) {
            is CatalogUiState.Loading -> {
                val hasData = lastCategories.isNotEmpty()
                binding.loadingIndicator.visibility = if (hasData) View.GONE else View.VISIBLE
                binding.errorGroup.visibility = View.GONE
                binding.swipeRefresh.visibility = if (hasData) View.VISIBLE else View.GONE
            }
            is CatalogUiState.Success -> {
                lastCategories = state.categories
                lastCourses = state.categories.flatMap { it.courses }
                binding.loadingIndicator.visibility = View.GONE
                binding.errorGroup.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
                renderList()
            }
            is CatalogUiState.Error -> {
                binding.loadingIndicator.visibility = View.GONE
                binding.swipeRefresh.visibility = View.GONE
                binding.errorGroup.visibility = View.VISIBLE
                binding.errorMessage.text = state.message
            }
        }
    }

    private fun renderList() {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            val popular = lastCourses.sortedByDescending { it.rating }.take(4)
            homeAdapter.showBrowse(
                categories = lastCategories,
                popularTitle = getString(R.string.popular_courses_header),
                popularCourses = popular
            )
        } else {
            val results = lastCourses.filter { course ->
                course.title.contains(query, ignoreCase = true) ||
                    course.instructor.name.contains(query, ignoreCase = true) ||
                    course.tags.any { it.contains(query, ignoreCase = true) }
            }
            homeAdapter.showSearchResults(
                resultsTitle = getString(R.string.search_results_header, query),
                results = results
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
