package com.nammapustaka.ui.qrscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nammapustaka.databinding.FragmentQrScannerBinding
import com.nammapustaka.utils.Resource
import com.nammapustaka.utils.showToast
import com.nammapustaka.viewmodel.QrScannerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class QrScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QrScannerViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var isFlashOn = false
    private var hasProcessedCode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupClickListeners()
        startCamera()
        observeViewModels()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.ivFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            camera?.cameraControl?.enableTorch(isFlashOn)
        }

        binding.btnBorrow.setOnClickListener {
            val book = (viewModel.scannedBook.value as? Resource.Success)?.data ?: return@setOnClickListener
            viewModel.confirmBorrow(book)
        }

        binding.btnReturn.setOnClickListener {
            showToast("Return scan: scan the book QR to return")
            viewModel.setScanMode(QrScannerViewModel.ScanMode.RETURN)
            viewModel.resetScan()
            hasProcessedCode = false
        }

        binding.btnScanAgain.setOnClickListener {
            viewModel.resetScan()
            hasProcessedCode = false
            binding.cardBookResult.isVisible = false
        }

        binding.chipBorrow.setOnClickListener { viewModel.setScanMode(QrScannerViewModel.ScanMode.BORROW) }
        binding.chipReturn.setOnClickListener { viewModel.setScanMode(QrScannerViewModel.ScanMode.RETURN) }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply { setAnalyzer(cameraExecutor, ::analyzeImage) }

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        if (hasProcessedCode) { imageProxy.close(); return }
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { qrValue ->
                            if (!hasProcessedCode) {
                                hasProcessedCode = true
                                requireActivity().runOnUiThread {
                                    viewModel.onQrCodeDetected(qrValue)
                                }
                            }
                        }
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scannedBook.collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                binding.progressBar.isVisible = true
                                binding.cardBookResult.isVisible = false
                            }
                            is Resource.Success -> {
                                binding.progressBar.isVisible = false
                                binding.cardBookResult.isVisible = true
                                val book = result.data
                                binding.tvBookTitle.text = book.title
                                binding.tvBookAuthor.text = book.author
                                binding.tvBookAvailability.text = if (book.isAvailable) "Available" else "Borrowed"
                                binding.btnBorrow.isVisible = book.isAvailable &&
                                    viewModel.scanMode.value == QrScannerViewModel.ScanMode.BORROW
                                binding.btnReturn.isVisible = !book.isAvailable &&
                                    viewModel.scanMode.value == QrScannerViewModel.ScanMode.RETURN
                            }
                            is Resource.Error -> {
                                binding.progressBar.isVisible = false
                                showToast(result.message)
                                hasProcessedCode = false
                            }
                            null -> {
                                binding.progressBar.isVisible = false
                                binding.cardBookResult.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    viewModel.transactionResult.collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                binding.lottieSuccess.isVisible = true
                                binding.lottieSuccess.playAnimation()
                                showToast("Success!")
                                binding.cardBookResult.isVisible = false
                                viewModel.resetScan()
                                hasProcessedCode = false
                            }
                            is Resource.Error -> showToast(result.message)
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
