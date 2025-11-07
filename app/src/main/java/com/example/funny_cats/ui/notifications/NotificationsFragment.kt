package com.example.funny_cats.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.funny_cats.databinding.FragmentNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = NotificationsAdapter(
            onSettingChanged = { setting, isEnabled ->
                viewModel.updateSetting(setting.id, isEnabled)
            },
            onTestClick = { setting ->
                viewModel.sendTestNotification(setting.id)
                Toast.makeText(
                    requireContext(),
                    "Тест уведомления: ${setting.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNotifications.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationSettings.collect { settings ->
                adapter.submitList(settings)
                binding.textEmpty.visibility = if (settings.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonTestAll.setOnClickListener {
            // Тестируем все включенные уведомления
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.notificationSettings.collect { settings ->
                    settings.filter { it.isEnabled }.forEach { setting ->
                        viewModel.sendTestNotification(setting.id)
                    }
                    Toast.makeText(
                        requireContext(),
                        "Тестовые уведомления отправлены для всех включенных типов",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}