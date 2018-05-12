package midien.kheldiente.equalizer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import kotlinx.android.synthetic.main.item_preset.view.*
import midien.kheldiente.equalizer.R
import midien.kheldiente.equalizer.data.Preset

class PresetAdapter(private val context: Context,
                    var presetList: ArrayList<Preset> = ArrayList(0),
                    private val listener: (Preset) -> Unit)
    : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    val checkBoxList = ArrayList<CheckBox>(0)

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int)
            = holder.bind(position = position, preset = presetList[position], listener = listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = PresetViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preset, parent, false))

    override fun getItemCount() = presetList.size

    inner class PresetViewHolder @JvmOverloads constructor(
            itemView: View
    ): RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, preset: Preset, listener: (Preset) -> Unit) = with(itemView) {
            // Save instance for reference
            checkBoxList.add(cb_preset_selected)
            txt_preset.text = preset.name
            setOnClickListener {
                cb_preset_selected.isChecked = !cb_preset_selected.isChecked
                listener(preset)
                forceUncheck(position)
            }
        }

        fun forceUncheck(selected: Int) {
            checkBoxList
                    .filterIndexed { index, cb -> index != selected }
                    .forEach { it.isChecked = false }
        }

    }
}