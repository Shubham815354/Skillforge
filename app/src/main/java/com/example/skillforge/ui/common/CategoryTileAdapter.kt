package com.example.skillforge.ui.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.skillforge.R
import com.example.skillforge.data.model.Category
import com.example.skillforge.databinding.ItemCategoryTileBinding

/**
 * @param fixedWidthPx When set, forces each tile to this width (used for the horizontal
 * carousel on Home). Left null to let the tile fill its grid cell (used on the "See all
 * categories" screen).
 */
class CategoryTileAdapter(
    private val onClick: (String) -> Unit,
    private val fixedWidthPx: Int? = null
) : RecyclerView.Adapter<CategoryTileAdapter.ViewHolder>() {

    private var items: List<Category> = emptyList()

    fun submit(categories: List<Category>) {
        items = categories
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        fixedWidthPx?.let { width ->
            binding.root.layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(
        private val binding: ItemCategoryTileBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.categoryName.text = category.name
            binding.courseCountText.text = binding.root.context.resources.getQuantityString(
                R.plurals.course_count,
                category.courseCount,
                category.courseCount
            )

            val accent = runCatching { Color.parseColor(category.iconColor) }
                .getOrDefault(ColorUtils.blendARGB(Color.GRAY, Color.WHITE, 0.5f))
            binding.tileIconBackground.drawable.mutate().setTint(ColorUtils.setAlphaComponent(accent, 40))
            binding.tileIconAccent.drawable.mutate().setTint(accent)

            binding.root.setOnClickListener { onClick(category.id) }
        }
    }
}
