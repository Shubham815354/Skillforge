package com.example.skillforge.ui.categorylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.skillforge.R
import com.example.skillforge.databinding.FragmentCategoryListBinding
import com.example.skillforge.ui.catalog.CatalogViewModel
import com.example.skillforge.ui.common.CategoryTileAdapter

class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by activityViewModels { CatalogViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoryTileAdapter(
            onClick = { categoryId ->
                findNavController().navigate(
                    R.id.action_categoryList_to_courseList,
                    bundleOf("categoryId" to categoryId)
                )
            }
        )
        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.categoriesRecyclerView.adapter = adapter
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        adapter.submit(viewModel.categories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
