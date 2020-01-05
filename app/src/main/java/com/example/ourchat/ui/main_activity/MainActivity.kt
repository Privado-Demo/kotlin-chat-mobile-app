package com.example.ourchat.ui.main


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.example.ourchat.R
import com.example.ourchat.Utils.ErrorMessage
import com.example.ourchat.Utils.LoadState
import com.example.ourchat.databinding.ActivityMainBinding
import com.example.ourchat.ui.signup.SignupFragment
import com.facebook.CallbackManager
import kotlinx.android.synthetic.main.issue_layout.view.*


class MainActivity : AppCompatActivity(), SignupFragment.ReturnCallBackManager {


    val REQUEST_IMAGE_CAPTURE = 1
    private val PICK_IMAGE_REQUEST = 2
lateinit var mCallbackManager:CallbackManager
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        //change title text color of toolbar to white
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.white))


        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        println("MainActivity.onCreate:${navController.currentDestination?.label}")

        //change overflow icon to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.toolbar.overflowIcon?.colorFilter =
                BlendModeColorFilter(Color.WHITE, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            binding.toolbar.overflowIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }


        //handle any change in loading state in whole app
        binding.issueLayout.cancelImage.setOnClickListener {
            binding.issueLayout.visibility = View.GONE
        }

        sharedViewModel.loadState.observe(this, Observer {

            when (it) {
                LoadState.LOADING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.SUCCESS -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.FAILURE -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.VISIBLE
                    binding.issueLayout.textViewIssue.text = ErrorMessage.errorMessage
                }

            }
        })



    }


    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    fun selectFromGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            //update live data so profile fragment will show result image
            sharedViewModel.imageBitmap.postValue(imageBitmap)
        }
        if (data != null && resultCode == RESULT_OK) {
            //update live data so profile fragment will show result image
            sharedViewModel.galleryImageUri.postValue(data.data)
            //upload image to firebase storage
            sharedViewModel.uploadImageByUri(data.data)
        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun bringBackCallbackManager(callbackManager: CallbackManager) {
        mCallbackManager=callbackManager
    }




}




//todo fix this method
fun hideKeyboard(activity: Activity) {
    /*   val imm: InputMethodManager =
           activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
       //Find the currently focused view, so we can grab the correct window token from it.
       var view: View? = activity.currentFocus
       //If no view currently has focus, create a new one, just so we can grab a window token from it
       if (view == null) {
           view = View(activity)
       }
       imm.hideSoftInputFromWindow(view.getWindowToken(), 0)*/
    //close soft keyboard
    var view: View? = activity.currentFocus
    val imm =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(
        view?.windowToken,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}