package midien.kheldiente.equalizer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.item_preset.view.*
import midien.kheldiente.equalizer.R
import midien.kheldiente.equalizer.data.Preset

class PresetAdapter(private val context: Context,
                    var presetList: ArrayList<Preset> = ArrayList(0),
                    var enabled: Boolean = false,
                    var currentPreset: String = "Normal",
                    private val listener: (Preset) -> Unit)
    : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    val itemViewList = ArrayList<View>(0)

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int)
            = holder.bind(position = position, preset = presetList[position], listener = listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = PresetViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preset, parent, false))

    override fun getItemCount() = presetList.size

    fun checkAll(check: Boolean) {
        itemViewList.forEach {
            val checkBox = it.findViewById<CheckBox>(R.id.cb_preset_selected)
            checkBox.isChecked = check
        }
    }

    fun enableAll(enable: Boolean) {
        itemViewList.forEach {
            val txtName = it.findViewById<TextView>(R.id.txt_preset)
            val checkBox = it.findViewById<CheckBox>(R.id.cb_preset_selected)

            txtName.isEnabled = enable
            checkBox.isEnabled = enable
            it.isEnabled = enable
        }
    }

    inner class PresetViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, preset: Preset, listener: (Preset) -> Unit) = with(itemView) {
            // Save instance for reference
            itemViewList.add(itemView)
            txt_preset.text = preset.name
            txt_preset.isEnabled = enabled
            cb_preset_selected.isEnabled = enabled

            if(preset.name.equals(currentPreset))
                cb_preset_selected.isChecked = true

            setOnClickListener {
                cb_preset_selected.isChecked = !cb_preset_selected.isChecked
                listener(preset)
                forceUncheck(position)
            }
        }

        fun forceUncheck(selected: Int) {
            itemViewList
                    .filterIndexed { index, view -> index != selected }
                    .forEach {
                        val checkBox = it.findViewById<CheckBox>(R.id.cb_preset_selected)
                        checkBox.isChecked = false
                    }
        }

    }
}