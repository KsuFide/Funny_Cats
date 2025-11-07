package com.example.funny_cats.ui.detail

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.funny_cats.R
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.databinding.FragmentImageDetailBinding
import com.example.funny_cats.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImageDetailFragment : Fragment() {

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var currentImage: CatImage
    private var isFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString("image_url") ?: ""
        val imageId = arguments?.getString("image_id") ?: ""

        currentImage = CatImage(
            id = imageId,
            url = imageUrl,
            width = 0,
            height = 0,
            isInFavorites = false
        )

        // Сохраняем время просмотра
        saveViewTime()

        setupToolbar()
        loadImage()
        loadFavoriteState()
        setupButtons()
    }

    private fun saveViewTime() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateViewTime(currentImage.id)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadImage() {
        Glide.with(this)
            .load(currentImage.url)
            .into(binding.imageViewDetail)
    }

    private fun loadFavoriteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            val imageFromDb = viewModel.getImageById(currentImage.id)
            isFavorite = imageFromDb?.isInFavorites ?: false
            updateFavoriteButton()
        }
    }

    private fun setupButtons() {
        binding.buttonFavorite.setOnClickListener {
            isFavorite = !isFavorite
            viewModel.toggleImageFavorite(currentImage.id, isFavorite)
            updateFavoriteButton()

            val message = if (isFavorite) "Добавлено в избранное! ❤️" else "Удалено из избранного"
            showToast(message)
        }

        binding.buttonDownload.setOnClickListener {
            downloadImage(currentImage.url)
        }

        binding.buttonShare.setOnClickListener {
            shareImage(currentImage.url)
        }
    }

    private fun updateFavoriteButton() {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        binding.buttonFavorite.setImageResource(icon)
    }

    private fun downloadImage(imageUrl: String) {
        try {
            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(imageUrl)

            val request = DownloadManager.Request(uri)
                .setTitle("Котик")
                .setDescription("Скачивание фото котика")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "cat_${System.currentTimeMillis()}.jpg"
                )

            downloadManager.enqueue(request)
            showToast("Скачивание началось! 📥")

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Ошибка скачивания 😿")
        }
    }

    private fun shareImage(imageUrl: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Посмотри на этого котика! 🐱\n$imageUrl")
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, "Поделиться котиком"))
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Ошибка при попытке поделиться 😿")
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}