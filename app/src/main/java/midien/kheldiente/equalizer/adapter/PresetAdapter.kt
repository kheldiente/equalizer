package midien.kheldiente.equalizer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import kotlinx.android.synthetic.main.item_preset.view.*
import midien.kheldiente.equalizer.R
import midien.kheldiente.equalizer.data.Preset

class PresetAdapter(private val context: Context,
                    var presetList: ArrayList<Preset> = ArrayList(0),
                    var enabled: Boolean = false,
                    var currentPreset: String = "Normal",
                    var currentPosition: Int = 0,
                    private val listener: (Int, Preset) -> Unit)
    : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    val TAG = PresetAdapter::class.java.simpleName
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

    fun check(position: Int) {
        itemViewList.forEachIndexed { index, view ->
            val checkBox = view.findViewById<CheckBox>(R.id.cb_preset_selected)
            checkBox.isChecked = index === position
        }
    }

    inner class PresetViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int, preset: Preset, listener: (Int, Preset) -> Unit) = with(itemView) {
            // Save instance for reference
            itemViewList.add(itemView)
            txt_preset.text = preset.name
            itemView.isEnabled = enabled
            txt_preset.isEnabled = enabled
            cb_preset_selected.isEnabled = enabled
            cb_preset_selected.tag = position

            if(preset.name.equals(currentPreset)) {
                currentPosition = position
                cb_preset_selected.isChecked = true
                Log.d(TAG, "currentPosition: $currentPosition")
            }

            // Set listeners
            setOnClickListener {
                applyChanges(position, listener, cb_preset_selected, preset)
            }
            cb_preset_selected.setOnCheckedChangeListener { _, _ ->
                // Override and do nothing
            }
        }

        fun applyChanges(position: Int, listener: (Int, Preset) -> Unit,  cb: CompoundButton, preset: Preset) {
            if(currentPosition != position) {
                Log.d(TAG, "applyChanges: $position")
                currentPosition = position
                cb.isChecked = !cb.isChecked
                listener(position, preset)
                forceUncheck(position)
            }
            Log.d(TAG, "currentPosition: $currentPosition")
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