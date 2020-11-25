package sk.stuba.fei.mv.android.zaverecne.gallery

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.beans.Album

class FolderRecycleViewAdapter(
    private val context: Context, itemLayoutId: Int,
    private val albums: List<Album>, listener: RowItemsListener
) : RecyclerView.Adapter<FolderRecycleViewAdapter.ViewHolder>() {
    private val itemsFiltered: List<Album> = albums
    private val listener: RowItemsListener = listener
    private val itemLayoutId: Int = itemLayoutId
    override fun getItemCount(): Int {
        return itemsFiltered.size
    }

    /*private view holder class*/
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.folderIcon)
        var txtTitle: TextView = itemView.findViewById(R.id.folderTitle)
        var mediaCount: TextView = itemView.findViewById(R.id.folderCount)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.parentFolder)

        // Method in ViewHolder class
        fun bind(album: Album) {
            // Get the state
            txtTitle.text = album.name
            val songsFound = context.resources.getQuantityString(
                R.plurals.numberOfFiles,
                album.rowItems.size,
                album.rowItems.size
            )
            mediaCount.text = songsFound
            Glide
                .with(context)
                .load(album.rowItems[0].file)
                .error(R.drawable.ic_broken_image)
                .override(220, 150)
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .into(imageView)
        }

        fun update(album: Album?) {
            linearLayout.setOnClickListener {
                listener.onRowItemSelected(album, imageView)
                Log.d("click", albums[adapterPosition].toString() + "click2")
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(itemLayoutId, null)
        // Return a new holder instance
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val album = albums[position]
        holder.bind(album)
        holder.update(album)
    }

    interface RowItemsListener {
        fun onRowItemSelected(album: Album?, imageView: ImageView?)
        fun onRefreshData()
    }

}