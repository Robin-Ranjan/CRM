package com.scout.crm.viewModals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModal : ViewModel() {
    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> get() = _currentTime

    private val _punchInTimeStamp = MutableLiveData<Long>()
    val punchInTimeStamp: LiveData<Long> get() = _punchInTimeStamp

    private val _punchOutTimeStamp = MutableLiveData<Long>()
    val punchOutTimeStamp: LiveData<Long> get() = _punchInTimeStamp
    private val updateInterval: Long = 1000

    init {
        startUpdatingTime()
    }

    private fun startUpdatingTime() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                while (isActive) {
                    updateTime()
                    withContext(Dispatchers.IO) {
                        delay(updateInterval)
                    }
                }
            }
        }
    }

    private fun updateTime() {
        val currentTimeStamp = System.currentTimeMillis()
        val formater = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val formaterTime = formater.format(Date(currentTimeStamp.toLong()))
        _currentTime.postValue(formaterTime)
    }

    fun setPunchInDetails(timeStamp: Long) {
        _punchInTimeStamp.value = timeStamp
    }

    fun setPunchOutDetails(timeStamp: Long) {
        _punchOutTimeStamp.value = timeStamp
    }
}