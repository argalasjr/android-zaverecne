package sk.stuba.fei.mv.android.zaverecne.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.recycle_view_gallery.*
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.beans.Album
import sk.stuba.fei.mv.android.zaverecne.fetchfiles.FetchFiles
import sk.stuba.fei.mv.android.zaverecne.fetchfiles.StoragePath
import java.io.File
import java.io.Serializable
import java.util.*

class FolderRecycleView : AppCompatActivity(), FolderRecycleViewAdapter.RowItemsListener,
    Serializable {
    private val searchMenuItem: MenuItem? = null
    var fileType = ".mp4"
    var root = Environment.getExternalStorageDirectory()
    var searchView: SearchView? = null
    var adapter: FolderRecycleViewAdapter? = null
    var itemLayout = 0
    var isSDPresent = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycle_view_gallery)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.my_gallery)

        itemLayout = R.layout.folder_item

        if (checkPermission()) {
            fetchFiles()
        } else {
            requestPermission()
        }
    }

    override fun onStart() {
        super.onStart()
    }


    private fun fetchFiles() {
        val albums: MutableList<Album> = ArrayList<Album>()

        val storagePath: StoragePath
        storagePath = StoragePath(getExternalFilesDirs(null))
        val storages = storagePath.deviceStorages
        for (i in storages.indices) {
            storages[i]?.let { Log.d("storage", it) }
        }
        for (i in storages.indices) {
            albums.addAll(FetchFiles.getFiles(File(storages[i]), fileType))
        }
        //        }
        if (!albums.isEmpty()) {
            noItems!!.visibility = View.GONE
            noItemsText!!.visibility = View.GONE
            recycleViewGallery!!.visibility = View.VISIBLE
            numberOfFiles!!.visibility = View.VISIBLE
            val songsFound =
                resources.getQuantityString(R.plurals.numberOfFolders, albums.size, albums.size)
            numberOfFiles!!.text = songsFound
            adapter = FolderRecycleViewAdapter(this, itemLayout, albums, this)
            recycleViewGallery!!.adapter = adapter
            recycleViewGallery!!.layoutManager = GridLayoutManager(this, 1)
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
            recycleViewGallery!!.setHasFixedSize(true)
            // Removes blinks
            (recycleViewGallery!!.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
            val itemDecor = DividerItemDecoration(applicationContext, GridLayout.HORIZONTAL)
            recycleViewGallery!!.addItemDecoration(itemDecor)
        } else {
            supportActionBar!!.setSubtitle("")
            recycleViewGallery!!.visibility = View.GONE
            numberOfFiles!!.visibility = View.GONE
            noItems!!.visibility = View.VISIBLE
            noItemsText!!.text = "No video founded"
            noItemsText!!.visibility = View.VISIBLE
            val snackbar = Snackbar
                .make(
                        media_root_layout!!,
                        "You have to upload video (.mp4) to your phone's storage before the process.",
                        Snackbar.LENGTH_INDEFINITE
                )
                .setAction("OK") { finish() }
            snackbar.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.about) {
//            About.CreateDialog(this)
//        }
        if (item.itemId == R.id.home) {
            supportFinishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        menu.findItem(R.id.search).isVisible = false
        //        SearchManager searchManager = (SearchManager)
//                getSystemService(Context.SEARCH_SERVICE);
//        searchMenuItem = menu.findItem(R.id.search);
//        searchView = (SearchView) searchMenuItem.getActionView();
//
//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        //searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onRefreshData() {
        val songsFound = resources.getQuantityString(
                R.plurals.numberOfFolders,
                adapter!!.itemCount,
                adapter!!.itemCount
        )
        numberOfFiles!!.text = songsFound
    }

    //    @Override
    //    public boolean onQueryTextSubmit(String query) {
    //        adapter.getFilter().filter(query);
    //        return false;
    //    }
    //
    //    @Override
    //    public boolean onQueryTextChange(String newText) {
    //        adapter.getFilter().filter(newText);
    //
    //        return true;
    //    }
    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
                this@FolderRecycleView,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
                this@FolderRecycleView,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .")
                fetchFiles()
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .")
            }
        }
    }

    override fun onRowItemSelected(album: Album?, imageView: ImageView?) {

//        if (searchView.isShown()) {
//            searchMenuItem.collapseActionView();
//            searchView.setQuery("", false);
//        }
        val i = Intent(this@FolderRecycleView, GalleryRecycleView::class.java)
        val args = Bundle()
        if (album != null) {
            args.putSerializable("rowItems", album.rowItems)
        }
        i.putExtra("bundle", args)
        if (album != null) {
            i.putExtra("title", album.name)
        }
        //i.putExtra("rowItems", album.getRowItems());
        if (Build.VERSION.SDK_INT > 20) {
//            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
//            view.setTransitionName("videoTrim");
            val p1 = Pair.create<View, String>(
                    imageView, ViewCompat.getTransitionName(
                    imageView!!
            )
            )
            val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
            startActivity(i, activityOptionsCompat.toBundle())
        } else {
            startActivity(i)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}