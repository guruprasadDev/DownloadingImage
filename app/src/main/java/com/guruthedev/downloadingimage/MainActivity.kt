package com.guruthedev.downloadingimage

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Declaring and initializing  the elements from the layout file
        val imageView =  findViewById<ImageView>(R.id.image_View)
        val downloadBtn = findViewById<Button>(R.id.downloadBtn)

        //Declaring a bitmap local

        var image : Bitmap?

        //Declaring  a web path as a String
        val imageUrlWebPath = ""

        //Declaring and initializing an Executor and a handler
        val newExecutor = Executors.newSingleThreadExecutor()
        val newHandler = Handler(Looper.getMainLooper())

        // When Button is clicked, executor will
        // fetch the image and handler will display it.
        // Once displayed, it is stored locally


        downloadBtn.setOnClickListener {
            newExecutor.execute{
                image = loadImage(imageUrlWebPath)
                newHandler.post{
                    imageView.setImageBitmap(image)
                    image?.let { saveMediaToStorage(it) }
                }
            }
        }



    }


    // Function to establish connection and load image

    private fun loadImage(string: String): Bitmap? {
        val url:URL = convertStringToURL(string) !!
        val connection : HttpURLConnection?

        try{
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream : InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)

        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext,"Error to Load image",Toast.LENGTH_LONG).show()
        }
        return null

    }

    // Function to convert string to URL

    private fun convertStringToURL(string: String): URL? {
        try{
            return URL(string)

        }catch (e:MalformedURLException){
            e.printStackTrace()
        }
        return null

    }
    // Function to save image on the device.

    private fun saveMediaToStorage(image: Bitmap?) {
        val fileName = "${System.currentTimeMillis()}.jpg"
        var filterOutputStream : OutputStream? = null

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            this.contentResolver?.also { contentResolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
                filterOutputStream =  imageUri?.let { contentResolver.openOutputStream(it) }
            }
        }else{
            val imageDri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val images = File(imageDri,fileName)
            filterOutputStream = FileOutputStream(images)
        }
        filterOutputStream?.use {
            image?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Saved to Gallery" , Toast.LENGTH_SHORT).show()

        }

    }
}