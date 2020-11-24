package sk.stuba.fei.mv.android.zaverecne.gallery

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.beans.RowItem
import java.util.*

class VideoRecycleViewAdapter(
    var context: Context, itemLayoutId: Int,
    private val items: List<RowItem>, listener: RowItemsListener, isActionMode: Boolean
) : RecyclerView.Adapter<VideoRecycleViewAdapter.ViewHolder>(), Filterable {
    var itemsFiltered: List<RowItem>
        private set
    private val listener: RowItemsListener
    private val isActionMode: Boolean
    var itemLayoutId: Int
    var actionMode: ActionMode? = null
    private var multiSelect = false
    private val selectedItems = ArrayList<RowItem>()
    val selectedItemsSize: Int
        get() = selectedItems.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                itemsFiltered = if (charString.isEmpty()) {
                    items
                } else {
                    val filteredList: MutableList<RowItem> = ArrayList()
                    for (row in items) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.title.toLowerCase()
                                .contains(charString.toLowerCase()) || row.title.contains(
                                charSequence
                            )
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = itemsFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                itemsFiltered = filterResults.values as ArrayList<RowItem>
                notifyDataSetChanged()
                listener.onRefreshData(multiSelect)
            }
        }
    }

    /*private view holder class*/
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var imageView: ImageView = itemView.findViewById(R.id.mediaIcon)
        private var txtTitle: TextView = itemView.findViewById(R.id.mediaTitle)
        private var linearLayout: LinearLayout = itemView.findViewById(R.id.row_item_root)

        // Method in ViewHolder class
        fun bind(rowItem: RowItem) {
            // Get the state
            // Set the visibility based on state

            txtTitle.text = rowItem.title
            Glide
                .with(context)
                .load(rowItem.file)
                .error(R.drawable.ic_broken_image)
                .override(140, 100)
                .transition(DrawableTransitionOptions.withCrossFade(750))
                .into(imageView)
        }

        fun selectItem(rowItem: RowItem) {
            if (multiSelect) {
                val res = context.resources
                if (selectedItems.contains(rowItem)) {
                    selectedItems.remove(rowItem)
                    linearLayout.setBackgroundColor(Color.WHITE)
                } else {
                    selectedItems.add(rowItem)
                    linearLayout.setBackgroundColor(Color.LTGRAY)
                }
//                val songsFound = res.getQuantityString(
//                    R.plurals.numberOfSelectedFile,
//                    selectedItemsSize,
//                    selectedItemsSize
//                )
//                actionMode!!.title = songsFound
            }
            listener.onRefreshData(multiSelect)
        }

        fun update(rowItem: RowItem) {
            //txtTitle.setText(rowItem.getTitle() + "");
            if (selectedItems.contains(rowItem)) {
                linearLayout.setBackgroundColor(Color.LTGRAY)
            } else {
                linearLayout.setBackgroundColor(Color.WHITE)
            }
            Log.d("adapter", "update called")
            itemView.setOnLongClickListener { view ->
                if (isActionMode) {
                    actionMode = (view.context as AppCompatActivity).startSupportActionMode(
                        actionModeCallbacks
                    )
                }
                selectItem(rowItem)
                true
            }
            itemView.setOnClickListener {
                selectItem(rowItem)
                if (!multiSelect) {
                    listener.onRowItemSelected(rowItem, imageView)
                }
            }
            listener.onRefreshData(multiSelect)
        }

    }

    private val actionModeCallbacks: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            multiSelect = true
//            menu.add("DELETE")
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (selectedItems.size != 0) {
//            ActionDelete.CreateDialog(context, selectedItems, itemsFiltered, mode);
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            multiSelect = false
            selectedItems.clear()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(itemLayoutId, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val rowItem = itemsFiltered[position]
        holder.bind(rowItem)
        holder.update(rowItem)
    }

    override fun getItemCount(): Int {
        return itemsFiltered.size
    }

    interface RowItemsListener {
        fun onRowItemSelected(rowItem: RowItem?, imageView: ImageView?)
        fun onRefreshData(multiselect: Boolean)
    }

    init {
        itemsFiltered = items
        this.listener = listener
        this.itemLayoutId = itemLayoutId
        this.isActionMode = isActionMode
    }
}