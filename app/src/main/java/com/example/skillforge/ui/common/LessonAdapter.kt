package com.example.skillforge.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.skillforge.R
import com.example.skillforge.data.model.Lesson
import com.example.skillforge.databinding.ItemLessonRowBinding
import com.example.skillforge.util.formatDurationMinutes

class LessonAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LessonAdapter.ViewHolder>() {

    private var items: List<Lesson> = emptyList()
    private var activeLessonId: String? = null

    fun submit(lessons: List<Lesson>, activeLessonId: String? = null) {
        items = lessons
        this.activeLessonId = activeLessonId
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLessonRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = items[position]
        holder.bind(lesson, isActive = lesson.id == activeLessonId)
    }

    class ViewHolder(
        private val binding: ItemLessonRowBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson, isActive: Boolean) {
            val context = binding.root.context
            binding.lessonTitle.text = lesson.title
            binding.freeBadge.visibility = if (lesson.isFree && !isActive) View.VISIBLE else View.GONE

            val strokeWidthPx = (1 * context.resources.displayMetrics.density).toInt()
            when {
                isActive -> {
                    binding.lessonIconBackground.setImageResource(R.drawable.bg_lesson_icon_active)
                    binding.lessonIcon.setImageResource(R.drawable.ic_pause_on_teal)
                    binding.lessonIcon.contentDescription = context.getString(R.string.content_desc_pause)
                    binding.lessonDuration.text = context.getString(
                        R.string.now_playing_format,
                        formatDurationMinutes(lesson.durationMinutes)
                    )
                    binding.lessonTitle.setTextColor(ContextCompat.getColor(context, R.color.teal))
                    binding.lessonDuration.setTextColor(ContextCompat.getColor(context, R.color.teal))
                    binding.rowCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_surface))
                    binding.rowCard.strokeColor = ContextCompat.getColor(context, R.color.teal)
                    binding.rowCard.strokeWidth = strokeWidthPx
                }
                lesson.isFree -> {
                    binding.lessonIconBackground.setImageResource(R.drawable.bg_lesson_icon_free)
                    binding.lessonIcon.setImageResource(R.drawable.ic_play_arrow)
                    binding.lessonIcon.contentDescription = context.getString(R.string.content_desc_play)
                    binding.lessonDuration.text = formatDurationMinutes(lesson.durationMinutes)
                    binding.lessonTitle.setTextColor(ContextCompat.getColor(context, R.color.ink_primary))
                    binding.lessonDuration.setTextColor(ContextCompat.getColor(context, R.color.ink_secondary))
                    binding.rowCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cream_elevated))
                    binding.rowCard.strokeWidth = 0
                }
                else -> {
                    binding.lessonIconBackground.setImageResource(R.drawable.bg_lesson_icon_locked)
                    binding.lessonIcon.setImageResource(R.drawable.ic_lock)
                    binding.lessonIcon.contentDescription = context.getString(R.string.content_desc_locked)
                    binding.lessonDuration.text = formatDurationMinutes(lesson.durationMinutes)
                    binding.lessonTitle.setTextColor(ContextCompat.getColor(context, R.color.ink_secondary))
                    binding.lessonDuration.setTextColor(ContextCompat.getColor(context, R.color.ink_secondary))
                    binding.rowCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cream_elevated))
                    binding.rowCard.strokeWidth = 0
                }
            }

            binding.root.setOnClickListener { onClick(lesson.id) }
        }
    }
}
