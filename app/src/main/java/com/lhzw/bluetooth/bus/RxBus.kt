package com.lhzw.bluetooth.bus

import com.hwangjr.rxbus.Bus

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 10:03
 *
 */
class RxBus {
    companion object {
        private var mRxBus: Bus? = null
        fun getInstance(): Bus {
            synchronized(RxBus::class.java){
                if (mRxBus == null) {
                    mRxBus = Bus()
                }
            }
            return mRxBus as Bus
        }
    }
}