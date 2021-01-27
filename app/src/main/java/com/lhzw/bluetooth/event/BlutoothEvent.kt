package com.lhzw.bluetooth.event

import android.app.Activity
import android.bluetooth.BluetoothDevice

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 10:18
 *
 */
data class BlutoothEvent(val device: BluetoothDevice, val context: Activity?)