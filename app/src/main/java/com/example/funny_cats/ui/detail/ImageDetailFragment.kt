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
import com.example.funny_cats.ui.BaseFragment
import com.example.funny_cats.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImageDetailFragment : BaseFragment() {

    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var currentImage: CatImage
    private var isFavorite: Boolean = false
    private var isFromBreedDetail: Boolean = false

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

        try {
            val imageUrl = arguments?.getString("image_url") ?: ""
            val imageId = arguments?.getString("image_id") ?: ""

            if (imageUrl.isEmpty() || imageId.isEmpty()) {
                showError("Неверные данные изображения")
                return
            }

            // Проверяем, открыто ли из деталей породы
            isFromBreedDetail = arguments?.getString("breed_id") != null

            currentImage = CatImage(
                id = imageId,
                url = imageUrl,
                width = 0,
                height = 0,
                isInFavorites = false
            )

            // ОБНОВЛЯЕМ ВРЕМЯ ПРОСМОТРА ПРИ ОТКРЫТИИ ДЕТАЛЕЙ
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.updateViewTime(currentImage.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            setupToolbar()
            loadImage()
            setupButtons()

            // Загружаем состояние избранного только если НЕ из деталей породы
            if (!isFromBreedDetail) {
                loadFavoriteState()
            } else {
                // Если из деталей породы - скрываем кнопку избранного
                binding.buttonFavorite.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Ошибка загрузки деталей изображения")
        }
    }

    private fun setupToolbar() {
        try {
            binding.toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadImage() {
        try {
            Glide.with(this)
                .load(currentImage.url)
                .placeholder(R.drawable.ic_cat_placeholder)
                .error(R.drawable.ic_cat_placeholder)
                .into(binding.imageViewDetail)
        } catch (e: Exception) {
            e.printStackTrace()
            binding.imageViewDetail.setImageResource(R.drawable.ic_cat_placeholder)
        }
    }

    private fun loadFavoriteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val imageFromDb = viewModel.getImageById(currentImage.id)
                isFavorite = imageFromDb?.isInFavorites ?: false
                updateFavoriteButton()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupButtons() {
        binding.buttonFavorite.setOnClickListener {
            try {
                isFavorite = !isFavorite
                viewModel.toggleImageFavorite(currentImage.id, isFavorite)
                updateFavoriteButton()

                val message = if (isFavorite) "Добавлено в избранное! ❤️" else "Удалено из избранного"
                showToast(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.buttonDownload.setOnClickListener {
            try {
                downloadImage(currentImage.url)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Ошибка скачивания")
            }
        }

        binding.buttonShare.setOnClickListener {
            try {
                shareImage(currentImage.url)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Ошибка при попытке поделиться")
            }
        }
    }

    private fun updateFavoriteButton() {
        try {
            // Если кнопка скрыта, ничего не делаем
            if (binding.buttonFavorite.visibility == View.GONE) return

            val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            binding.buttonFavorite.setImageResource(icon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        try {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showError(message: String) {
        try {
            binding.imageViewDetail.setImageResource(R.drawable.ic_cat_placeholder)
            showToast(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}