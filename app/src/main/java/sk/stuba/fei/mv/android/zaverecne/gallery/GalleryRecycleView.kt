package sk.stuba.fei.mv.android.zaverecne.gallery

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.recycle_view_gallery.*
import sk.stuba.fei.mv.android.zaverecne.MainActivity
import sk.stuba.fei.mv.android.zaverecne.R
import sk.stuba.fei.mv.android.zaverecne.beans.RowItem
import sk.stuba.fei.mv.android.zaverecne.video.VideoFragment
import java.io.IOException
import java.io.Serializable
import java.util.*


class GalleryRecycleView : AppCompatActivity(), SearchView.OnQueryTextListener,
    VideoRecycleViewAdapter.RowItemsListener, Serializable {
    var selectedItem: RowItem? = null
    var searchView: SearchView? = null
    var adapter: VideoRecycleViewAdapter? = null
    var itemLayout = 0
    private var searchMenuItem: MenuItem? = null

    var rowItems: List<RowItem>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setAnimation();
        setContentView(R.layout.recycle_view_gallery)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        intentData
//        supportActionBar!!.title = title

        itemLayout = R.layout.gallery_view_item

        if (checkPermission()) {
            fetchFiles()
        } else {
            requestPermission()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private val intentData: Unit
        private get() {
            val i = intent
            if (i != null) {
                val args = i.getBundleExtra("bundle")!!
                rowItems = args.getSerializable("rowItems") as ArrayList<RowItem>?
                title = i.getStringExtra("title")
            }
        }

    private fun fetchFiles() {
        if (rowItems!!.isNotEmpty()) {
            path.text = "~ " + rowItems!![0].parent!!.absolutePath
            adapter = VideoRecycleViewAdapter(this, itemLayout, rowItems!!, this, false)
            recycleViewGallery!!.adapter = adapter
            recycleViewGallery!!.layoutManager = GridLayoutManager(this, 2)
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
            recycleViewGallery!!.setHasFixedSize(true)
            // Removes blinks
            (recycleViewGallery!!.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
            //DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
            //recyclerView.addItemDecoration(itemDecor);
        } else {
        }
    }

    override fun onBackPressed() {
        // close search view on back button pressed
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
//                About.CreateDialog(this)
                supportFinishAfterTransition()
            }
            android.R.id.home -> supportFinishAfterTransition()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem?.actionView as SearchView
        searchView!!.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        adapter!!.filter.filter(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        adapter!!.filter.filter(newText)
        return true
    }

    override fun onRowItemSelected(rowItem: RowItem?, imageView: ImageView?) {
        selectedItem = rowItem
//        if (searchView!!.isShown) {
//            searchMenuItem!!.collapseActionView()
//            searchView!!.setQuery("", false)
//        }
        if (rowItem != null) {
            if (isVideoValid(rowItem.file.absoluteFile.toString())) {
                val fragmentManager: FragmentManager = supportFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                val myFragment = VideoFragment()
                val b = Bundle()
                b.putString("videoUri", rowItem.file.absolutePath)
                myFragment.arguments = b
                fragmentTransaction.replace(R.id.media_root_layout, myFragment).commit()

            }
        }
    }

    private fun isVideoValid(path: String): Boolean {
        val mediaPlayer = MediaPlayer()
        return try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            true
        } catch (e: IOException) {
            val snackbar = Snackbar
                .make(media_root_layout!!, "The video is not supported.", Snackbar.LENGTH_LONG)
            snackbar.show()
            //Toast.makeText(MainActivity.this, "Video can not be played", Toast.LENGTH_SHORT).show();
            false
        }
    }


    override fun onRefreshData(multiselect: Boolean) {
        val songsFound = resources.getQuantityString(
            R.plurals.numberOfFiles,
            adapter!!.itemCount,
            adapter!!.itemCount
        )
        numberOfFiles!!.text = songsFound
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@GalleryRecycleView,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@GalleryRecycleView,
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
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .")
                fetchFiles()
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .")
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}