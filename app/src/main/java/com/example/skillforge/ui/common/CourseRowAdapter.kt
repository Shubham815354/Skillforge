package com.example.skillforge.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.skillforge.data.model.Course
import com.example.skillforge.databinding.ItemCourseRowBinding
import com.example.skillforge.util.formatDurationHoursShort
import com.example.skillforge.util.levelColors

class CourseRowAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CourseRowAdapter.ViewHolder>() {

    private var items: List<Course> = emptyList()

    fun submit(courses: List<Course>) {
        items = courses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCourseRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(
        private val binding: ItemCourseRowBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            val context = binding.root.context
            binding.courseThumbnail.load(course.thumbnailUrl)
            binding.levelChip.text = course.level.uppercase()
            binding.courseTitle.text = course.title
            binding.instructorName.text = course.instructor.name
            binding.ratingText.text = course.rating.toString()
            binding.durationText.text = formatDurationHoursShort(course.durationHours)

            val (bg, fg) = levelColors(course.level, context)
            binding.levelChip.background.mutate().setTint(bg)
            binding.levelChip.setTextColor(fg)

            binding.root.setOnClickListener { onClick(course.id) }
        }
    }
}
