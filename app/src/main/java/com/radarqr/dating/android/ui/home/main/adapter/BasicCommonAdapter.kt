package com.radarqr.dating.android.ui.home.main.adapter

import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.LayoutBasicInfoItemBinding
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible


class BasicCommonAdapter(
    private val list: ArrayList<BasicInfoData>,
    val requireActivity: Activity,
    val fromWork: Boolean = false
) :
    RecyclerView.Adapter<BasicCommonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutBasicInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val binding: LayoutBasicInfoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (fromWork) {
                binding.clMain.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                )
            }
            val item = list[absoluteAdapterPosition]
            binding.clMain.visible(item.value.isNotEmpty())

            binding.tvInfo.text =
                if (item.value.split("#")[0] == "gender") Utility.getTextWithFirstCapital(
                    item.value.split(
                        "#"
                    )[1]
                ) else item.value.split(
                    "#"
                )[1]

            binding.ivIcon.visible(isVisible = item.icon != 0)
            binding.ivIcon.loadImage(item.icon)


            /*when (item.split(",")[0].lowercase()) {
                "gender" -> {
                    when (item.split(",")[1].lowercase()) {
                        "men", "man" -> {
                            binding.ivIcon.loadImage(R.drawable.ic_male_sign)
                        }

                        "women", "woman" -> {
                            binding.ivIcon.loadImage(R.drawable.ic_female_sign)
                        }
                        else -> {
                            binding.ivIcon.loadImage(R.drawable.ic_non_binary)
                        }
                    }
                }

                "height" -> {
                    binding.ivIcon.loadImage(R.drawable.ic_scale)
                }

                "drinking" -> {
                    Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_drinking)
                        .into(holder.itemView.iv_sign)
                    binding.ivIcon.loadImage(R.drawable.ic_scale)
                }

                "smoking" -> {
                    Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_smoking)
                        .into(holder.itemView.iv_sign)
                    binding.ivIcon.loadImage(R.drawable.ic_scale)
                }

                "location" -> {
                    Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_map_pin)
                        .into(holder.itemView.iv_sign)
                    binding.ivIcon.loadImage(R.drawable.ic_scale)
                }
                "children" -> {
                    Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_person)
                        .into(holder.itemView.iv_sign)
                    binding.ivIcon.loadImage(R.drawable.ic_scale)
                }
                "zodiac" -> {
                    when (item.split(",")[1]) {
                        "Aries", "aries" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_aries_zodiac)
                                .into(holder.itemView.iv_sign)
                            binding.ivIcon.loadImage(R.drawable.ic_scale)
                        }
                        "Taurus", "taurus" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_taurus_zodiac)
                                .into(holder.itemView.iv_sign)
                            binding.ivIcon.loadImage(R.drawable.ic_scale)
                        }
                        "Gemini", "gemini" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_gemini_zodiac)
                                .into(holder.itemView.iv_sign)
                            binding.ivIcon.loadImage(R.drawable.ic_scale)
                        }
                        "Cancer", "cancer" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_cancer_zodiac)
                                .into(holder.itemView.iv_sign)
                        }
                        "Leo", "leo" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_leo_zodiac)
                                .into(holder.itemView.iv_sign)
                        }
                        "Virgo", "virgo" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_virgo_zodiac)
                                .into(holder.itemView.iv_sign)
                        }
                        "Libra", "libra" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_libra)
                                .into(holder.itemView.iv_sign)
                        }
                        "Scorpio", "scorpio" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_scorpio_zodiac)
                                .into(holder.itemView.iv_sign)
                        }
                        "Sagittarius", "sagittarius" -> {
                            Glide.with(holder.itemView.iv_sign)
                                .load(R.drawable.ic_sagittarius_zodiac)
                                .into(holder.itemView.iv_sign)
                        }
                        "Capricorn", "capricorn" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_capricorn)
                                .into(holder.itemView.iv_sign)
                        }
                        "Aquarius", "aquarius" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_acquarius)
                                .into(holder.itemView.iv_sign)
                        }
                        "Pisces", "pisces" -> {
                            Glide.with(holder.itemView.iv_sign).load(R.drawable.ic_pisces)
                                .into(holder.itemView.iv_sign)
                        }

                    }
                }

                else -> holder.itemView.iv_sign.visibility = View.GONE
            }*/
        }
    }

}