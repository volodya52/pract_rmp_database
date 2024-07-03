package com.bignerdranch.android.application_practica2.ui.home

import android.content.ContentValues.TAG
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.bignerdranch.android.application_practica2.databinding.FragmentHomeBinding
import com.bignerdranch.android.application_practica2.ui.database.MyData
import com.bignerdranch.android.application_practica2.ui.database.MyDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var myDataBase: MyDataBase
    private lateinit var imageUrl: Uri
    private lateinit var ivMyImage: ImageView
    private lateinit var homeViewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        imageUrl = createImageUri()
        ivMyImage = binding.ivMyImage
        myDataBase = MyDataBase.getInstance(requireContext())
        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val surname = binding.surname.text.toString()
            val group = binding.group.text.toString()
            val bytearray = convertImageToBytes(ivMyImage)
            if (name.isNotEmpty() || surname.isNotEmpty() || group.isNotEmpty())
            {
                val data = MyData(
                    id = 1,
                    image = bytearray!!,
                    name = name,
                    surname = surname,
                    group = group
                )
                saveDataToDatabase(data, myDataBase)
                Toast.makeText(requireContext(),"Данные были сохранены",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Вы не заполнили какое-то окно ввода", Toast.LENGTH_SHORT).show()
            }
        }
        val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
            ivMyImage.setImageURI(imageUrl)
        }
        ivMyImage.setOnClickListener {
            contract.launch(imageUrl)
        }
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root: View = binding.root
        homeViewModel.text.observe(viewLifecycleOwner) {}
        loadDataFromDatabase()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createImageUri(): Uri{
        val image = File(requireActivity().filesDir, "myPhoto.png")
        return FileProvider.getUriForFile(
            requireContext(),
            "com.bignerdranch.android.application_practica2.fileprovider",
            image)
    }
    private fun convertImageToBytes(imageView: ImageView): ByteArray? {

            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                return outputStream.toByteArray()
            }
        return null
    }
    private fun saveDataToDatabase(data: MyData, myDataBase: MyDataBase) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val existingData = myDataBase.getDbDao().getById(1)
                    if (existingData != null) {
                        myDataBase.getDbDao().update(data)
                    } else {
                        myDataBase.getDbDao().insert(data)
                    }
                    Log.d("HomeFragment", "Дата была сохранена: $data")
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error saving data", e)
                }
            }
        }
    }
    private fun updateUI(data: MyData) {
        binding.name.setText(data.name)
        binding.surname.setText(data.surname)
        binding.group.setText(data.group)
        val bitmap = data.image.let { BitmapFactory.decodeByteArray(data.image, 0, it.size) }
        ivMyImage.setImageBitmap(bitmap)
    }

    private fun loadDataFromDatabase() {
        myDataBase.getDbDao().query().asLiveData().observe(viewLifecycleOwner) { dataList ->
            if (dataList.isNotEmpty()) {
                val data = dataList[0]
                Log.d("HomeFragment", "Data loaded: $data")
                updateUI(data)
            } else {
                Log.d("HomeFragment", "Data list is empty")
            }
        }
    }
}