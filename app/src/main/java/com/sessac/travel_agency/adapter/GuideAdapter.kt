package com.sessac.travel_agency.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sessac.travel_agency.data.GuideItem
import com.sessac.travel_agency.databinding.ItemGuideBinding


//tutor pyo Diff-Util ListAdapter 로 바꾸기
//RxBinding, FlowBinding
//tutor pyo ListAdapter

class GuideAdapter(val itemOnClick: (GuideItem) -> (Unit)) :
    ListAdapter<GuideItem, GuideAdapter.GuideViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<GuideItem>() {
            override fun areItemsTheSame(oldItem: GuideItem, newItem: GuideItem): Boolean {
                return oldItem.guideId == newItem.guideId
            }

            override fun areContentsTheSame(oldItem: GuideItem, newItem: GuideItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class GuideViewHolder(val binding: ItemGuideBinding) : RecyclerView.ViewHolder(binding.root){
        fun widgetBinding(guideItem: GuideItem){
            guideItem.let{guide ->
                with(binding){
                    Glide.with(guideImage.context)
                        .load(guide.gImage)
                        .into(guideImage)
                    guideName.text = guide.gName

                    // 카드 클릭시
                    root.setOnClickListener {
                        itemOnClick(guide)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        return GuideViewHolder(
            ItemGuideBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.widgetBinding(getItem(position))
    }

    fun setGuideList(guide: List<GuideItem>) {
        submitList(guide)
    }
}