package midien.kheldiente.equalizer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import midien.kheldiente.equalizer.R

class PresetAdapter(private val context: Context) : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_preset, parent, false)
        return PresetViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 0
    }

    inner class PresetViewHolder @JvmOverloads constructor(
            view: View
    ): RecyclerView.ViewHolder(view) {

        fun bindPreset() {

        }

    }
}