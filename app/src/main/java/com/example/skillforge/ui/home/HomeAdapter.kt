package com.example.skillforge.ui.home

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillforge.data.model.Category
import com.example.skillforge.data.model.Course
import com.example.skillforge.databinding.ItemHomeCategoriesSectionBinding
import com.example.skillforge.databinding.ItemHomeCourseListSectionBinding
import com.example.skillforge.ui.common.CategoryTileAdapter
import com.example.skillforge.ui.common.CourseRowAdapter
import com.example.skillforge.ui.common.HorizontalSpaceDecoration

private sealed class HomeListItem {
    data class Categories(val categories: List<Category>) : HomeListItem()
    data class CourseList(
        val title: String,
        val courses: List<Course>,
        val showSeeAll: Boolean
    ) : HomeListItem()
}

class HomeAdapter(
    private val onCategoryClick: (String) -> Unit,
    private val onSeeAllCategories: () -> Unit,
    private val onCourseClick: (String) -> Unit,
    private val onSeeAllCourses: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HomeListItem> = emptyList()

    fun showBrowse(categories: List<Category>, popularTitle: String, popularCourses: List<Course>) {
        items = listOf(
            HomeListItem.Categories(categories),
            HomeListItem.CourseList(popularTitle, popularCourses, showSeeAll = true)
        )
        notifyDataSetChanged()
    }

    fun showSearchResults(resultsTitle: String, results: List<Course>) {
        items = listOf(HomeListItem.CourseList(resultsTitle, results, showSeeAll = false))
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeListItem.Categories -> TYPE_CATEGORIES
        is HomeListItem.CourseList -> TYPE_COURSE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CATEGORIES -> CategoriesViewHolder(
                ItemHomeCategoriesSectionBinding.inflate(inflater, parent, false),
                onCategoryClick,
                onSeeAllCategories
            )
            else -> CourseListViewHolder(
                ItemHomeCourseListSectionBinding.inflate(inflater, parent, false),
                onCourseClick,
                onSeeAllCourses
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeListItem.Categories -> (holder as CategoriesViewHolder).bind(item.categories)
            is HomeListItem.CourseList -> (holder as CourseListViewHolder).bind(item)
        }
    }

    private class CategoriesViewHolder(
        private val binding: ItemHomeCategoriesSectionBinding,
        onCategoryClick: (String) -> Unit,
        private val onSeeAllCategories: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val adapter = CategoryTileAdapter(
            onClick = onCategoryClick,
            fixedWidthPx = (140 * binding.root.resources.displayMetrics.density).toInt()
        )

        init {
            binding.categoriesRecyclerView.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.categoriesRecyclerView.adapter = adapter
            val spacePx = (12 * binding.root.resources.displayMetrics.density).toInt()
            binding.categoriesRecyclerView.addItemDecoration(HorizontalSpaceDecoration(spacePx))

            binding.seeAllCategories.setOnClickListener { onSeeAllCategories() }
            binding.scrollLeftButton.setOnClickListener {
                binding.categoriesRecyclerView.smoothScrollBy(-tileScrollDistance(binding), 0)
            }
            binding.scrollRightButton.setOnClickListener {
                binding.categoriesRecyclerView.smoothScrollBy(tileScrollDistance(binding), 0)
            }
            binding.categoriesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    updateScrollThumb()
                }
            })

            var dragStartRawX = 0f
            var dragStartTranslation = 0f
            binding.scrollThumb.setOnTouchListener { thumbView, event ->
                val track = thumbView.parent as? View ?: return@setOnTouchListener false
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        dragStartRawX = event.rawX
                        dragStartTranslation = thumbView.translationX
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val maxTranslation = (track.width - thumbView.width).toFloat()
                        if (maxTranslation <= 0f) return@setOnTouchListener true
                        val proposedTranslation = (dragStartTranslation + (event.rawX - dragStartRawX))
                            .coerceIn(0f, maxTranslation)

                        val recyclerView = binding.categoriesRecyclerView
                        val range = recyclerView.computeHorizontalScrollRange()
                        val extent = recyclerView.computeHorizontalScrollExtent()
                        val scrollableDistance = (range - extent).toFloat()
                        if (scrollableDistance > 0) {
                            val targetOffset = (proposedTranslation / maxTranslation * scrollableDistance).toInt()
                            val currentOffset = recyclerView.computeHorizontalScrollOffset()
                            recyclerView.scrollBy(targetOffset - currentOffset, 0)
                        }
                        true
                    }
                    else -> true
                }
            }
        }

        private fun tileScrollDistance(binding: ItemHomeCategoriesSectionBinding): Int =
            (160 * binding.root.resources.displayMetrics.density).toInt()

        private fun updateScrollThumb() {
            val track = binding.scrollThumb.parent as? View ?: return
            val trackWidth = track.width
            if (trackWidth <= 0) return

            val recyclerView = binding.categoriesRecyclerView
            val range = recyclerView.computeHorizontalScrollRange()
            val extent = recyclerView.computeHorizontalScrollExtent()
            val offset = recyclerView.computeHorizontalScrollOffset()

            val thumbWidth = if (range <= extent) {
                trackWidth
            } else {
                (extent.toFloat() / range.toFloat() * trackWidth)
                    .toInt()
                    .coerceAtLeast((trackWidth * 0.15f).toInt())
            }
            val maxTranslation = (trackWidth - thumbWidth).toFloat()
            val scrollableDistance = (range - extent).toFloat()
            val fraction = if (scrollableDistance > 0) offset / scrollableDistance else 0f

            binding.scrollThumb.layoutParams.width = thumbWidth
            binding.scrollThumb.requestLayout()
            binding.scrollThumb.translationX = fraction.coerceIn(0f, 1f) * maxTranslation
        }

        fun bind(categories: List<Category>) {
            adapter.submit(categories)
            binding.categoriesRecyclerView.doOnLayout { updateScrollThumb() }
        }
    }

    private class CourseListViewHolder(
        private val binding: ItemHomeCourseListSectionBinding,
        onCourseClick: (String) -> Unit,
        private val onSeeAllCourses: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val adapter = CourseRowAdapter(onCourseClick)

        init {
            binding.coursesRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.coursesRecyclerView.adapter = adapter
        }

        fun bind(item: HomeListItem.CourseList) {
            binding.sectionTitle.text = item.title
            binding.seeAllCourses.visibility = if (item.showSeeAll) View.VISIBLE else View.GONE
            binding.seeAllCourses.setOnClickListener { onSeeAllCourses() }
            binding.emptyText.visibility = if (item.courses.isEmpty()) View.VISIBLE else View.GONE
            adapter.submit(item.courses)
        }
    }

    companion object {
        private const val TYPE_CATEGORIES = 0
        private const val TYPE_COURSE_LIST = 1
    }
}
