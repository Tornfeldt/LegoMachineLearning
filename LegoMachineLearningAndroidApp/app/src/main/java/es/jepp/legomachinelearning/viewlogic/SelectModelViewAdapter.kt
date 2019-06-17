package es.jepp.legomachinelearning.viewlogic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.jepp.legomachinelearning.R


class SelectModelViewAdapter : RecyclerView.Adapter<SelectModelViewAdapter.ViewHolder> {
    private val modelNames: List<String>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    // data is passed into the constructor
    constructor(context: Context, modelNames: List<String>) {
        this.mInflater = LayoutInflater.from(context)
        this.modelNames = modelNames
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.select_data_for_training_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = modelNames[position]
        holder.modelNameTextView.setText(animal)
    }

    // total number of rows
    override fun getItemCount(): Int {
        return modelNames.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var modelNameTextView: TextView

        init {
            modelNameTextView = itemView.findViewById(R.id.modelNameTextView)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener?.onItemClick(view, adapterPosition, modelNames[adapterPosition])
        }
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, modelName: String)
    }
}