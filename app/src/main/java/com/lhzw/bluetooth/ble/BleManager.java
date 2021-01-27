package com.lhzw.bluetooth.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.lhzw.bluetooth.dfu.DfuBeanEvent;
import com.lhzw.bluetooth.dfu.uitls.CRC32;
import com.lhzw.bluetooth.dfu.uitls.FileHelper;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class BleManager extends no.nordicsemi.android.ble.BleManager<BleManagerCallbacks> {
    private static final String TAG = "BleManager";
    private final static UUID COMM_SERVICE_UUID = UUID.fromString("88660001-8866-8866-8866-78A901000000");
    private final static UUID COMM_SERVICE_TXRX_CHAR_UUID = UUID.fromString("88660002-8866-8866-8866-78A901000000");

    private DfuBeanEvent dfuFile;
    private BluetoothGattCharacteristic mCommTXRXCharacteristic = null;
    private BleManagerCallbacks bleManagerCallbacks;


    public BleManager(@NonNull final Context context) {
        super(context);
        try {
            bleManagerCallbacks = (BleManagerCallbacks) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BleManagerCallbacks");
        }
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }


    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            //这个方法被回调  说明已经发现蓝牙服务并且设备支持此服务,也就是连接成功可以通信了
            Logger.e("蓝牙设备连接成功...initialize");
            if (mCommTXRXCharacteristic != null) {
                enableNotifications(mCommTXRXCharacteristic).enqueue();
                Logger.e("mCommTXRXCharacteristic notification enabled");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Watch", "connection_update .... ");
                        connection_update(true);
                    }
                }, 500);
            } else {
                Logger.e("mCommTXRXCharacteristic==null,char not found....蓝牙设备未连接成功..");
            }

        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            Logger.i(TAG, "isRequiredServiceSupported");
            final BluetoothGattService comm_service = gatt.getService(COMM_SERVICE_UUID);
            if (comm_service != null) {
                Logger.e("service found");
                mCommTXRXCharacteristic = comm_service.getCharacteristic(COMM_SERVICE_TXRX_CHAR_UUID);
                if (mCommTXRXCharacteristic != null) {
                    Logger.e("char found");
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onDeviceDisconnected() {
            Logger.i(TAG, this.getClass().getName() + "onDeviceDisconnected");
            mCommTXRXCharacteristic = null;
        }

        @Override
        protected void onDeviceReady() {
            Logger.e("蓝牙设备连接成功...onDeviceReady");
            super.onDeviceReady();
        }
    };

    public int device_disconnect() {

        disconnect().enqueue();
        return 0;
    }


    public int notification_enable() {

        if (mCommTXRXCharacteristic != null) {
            enableNotifications(mCommTXRXCharacteristic).enqueue();
            Logger.i(TAG, this.getClass().getName() + "mCommTXRXCharacteristic notification enabled");
        }

        return 0;
    }

    public int notification_disable() {
        if (mCommTXRXCharacteristic != null) {
            disableNotifications(mCommTXRXCharacteristic).enqueue();
            Logger.i(TAG, this.getClass().getName() + "mCommTXRXCharacteristic notification disabled");
        }

        return 0;
    }


    public int mtu_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onMtuUpdateResponse(data.getValue());
        });
        byte[] data = new byte[]{0x03};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int _mtu_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks._onMtuUpdateResponse(data.getValue());
        });
        byte[] data = new byte[]{0x03};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    // 跟新连接参数
    public int connection_update(Boolean fast) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onConnectionUpdateResponse(data.getValue());
        });
        if (fast) {
            byte[] data = new byte[]{0x01, 0x0F, 0x00, 0x1E, 0x00, 0x00, 0x00, (byte) 0x90, 0x01};//高功耗，快速
            writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        } else {
            byte[] data = new byte[]{0x01, (byte) 0x90, 0x01, (byte) 0x8A, 0x02, 0x00, 0x00, (byte) 0x90, 0x01};//低功耗，慢速
            writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();

        }
        return 0;
    }

    // 跟新连接参数
    public int settinng_connect_parameter(Boolean fast) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onSettingConnectParameter(data.getValue());
        });
        if (fast) {
            byte[] data = new byte[]{0x01, 0x0F, 0x00, 0x1E, 0x00, 0x00, 0x00, (byte) 0x90, 0x01};//高功耗，快速
            writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        } else {
            byte[] data = new byte[]{0x01, (byte) 0x90, 0x01, (byte) 0x8A, 0x02, 0x00, 0x00, (byte) 0x90, 0x01};//低功耗，慢速
            writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();

        }
        return 0;
    }

    // 跟新连接参数
    public int connection_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks._onReconnectResponse(data.getValue());
        });
        byte[] data = new byte[]{0x01, 0x0F, 0x00, 0x1E, 0x00, 0x00, 0x00, (byte) 0x90, 0x01};//高功耗，快速
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int device_info() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onDeviceInfoResponse(data.getValue());
        });
        byte[] data = new byte[]{0x02};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int watch_time_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onWatchTimeUpdateResponse(data.getValue());
        });
        List<Byte> listByte = new ArrayList<Byte>();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR) % 100;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekDay == 0) weekDay = 7;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int zone = calendar.get(Calendar.ZONE_OFFSET);
        Logger.i(TAG, "year : " + String.valueOf(year));
        Logger.i(TAG, "month : " + String.valueOf(month));
        Logger.i(TAG, "day : " + String.valueOf(day));
        Logger.i(TAG, "weekDay : " + String.valueOf(weekDay));
        Logger.i(TAG, "hour : " + String.valueOf(hour));
        Logger.i(TAG, "minute : " + String.valueOf(minute));
        Logger.i(TAG, "second : " + String.valueOf(second));
        Logger.i(TAG, "zone : " + String.valueOf(zone));
        Log.e("BLETime", "weakday  " + String.valueOf(weekDay));
        listByte.add((byte) year);
        listByte.add((byte) month);
        listByte.add((byte) day);
        listByte.add((byte) weekDay);
        listByte.add((byte) hour);
        listByte.add((byte) minute);
        listByte.add((byte) second);
        listByte.add((byte) zone);
        byte[] bytes = new byte[listByte.size()];
        for (int i = 0; i < listByte.size(); i++) {
            bytes[i] = listByte.get(i);
        }
        byte[] data = new byte[listByte.size() + 1];
        data[0] = 0x07;
        System.arraycopy(bytes, 0, data, 1, listByte.size());
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int personal_info_read() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onPersonalInfoReadResponse(data.getValue());
        });
        byte[] data = new byte[]{0x08};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int readWatchData(byte[] bytes) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onWatchDataResponse(data.getValue());
        });
        writeCharacteristic(mCommTXRXCharacteristic, bytes).enqueue();
        return 0;
    }


    //更新用户个人信息---性别,年龄,身高,体重,步长,目标步数,目标卡路里,目标距离,心率区间极限值
    public int personal_info_update(byte[] bytes) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onPersonalInfoUpdateResponse(data.getValue());
        });
        writeCharacteristic(mCommTXRXCharacteristic, bytes).enqueue();
        return 0;
    }

    //SettingFragment保存用户个人信息---性别,年龄,身高,体重,步长,目标步数,目标卡路里,目标距离,心率区间极限值
    public int personal_info_save(byte[] bytes) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onPersonalInfoSaveResponse(data.getValue());
        });
        writeCharacteristic(mCommTXRXCharacteristic, bytes).enqueue();
        return 0;
    }


    // 读取活动地址
    public int sports_param_read(byte[] content, String ID) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onSportsParamReadResponse(data.getValue(), ID);
        });
        writeCharacteristic(mCommTXRXCharacteristic, content).enqueue();
        return 0;
    }


    public int sports_param_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onSportsParamUpdateResponse(data.getValue());
        });
        byte[] data = new byte[]{0x09};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int daily_data_request(byte[] content) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onDailyDataRequestResponse(data.getValue());
        });
//        byte[] data = new byte[]{0X0C};
        writeCharacteristic(mCommTXRXCharacteristic, content).enqueue();
        return 0;
    }

    public int sport_detail_info_request(byte[] content, byte request_code, int type, String ID) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onSportDetailInfoResponse(data.getValue(), request_code, type, ID);
        });
        writeCharacteristic(mCommTXRXCharacteristic, content).enqueue();
        return 0;
    }

    public int read_boundary_address() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onActivityAddressRequestResponse(data.getValue());
        });
        byte[] data = new byte[]{0X0E};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int norFlash_read(byte[] content, String ID) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onNorFlashReadResponse(data.getValue(), ID);
        });
        /*
        byte callback_index = 1;
        int flash_addr = 0x00003000 + 12 * 1024 * 4 + 4096;
        byte size = (byte) 233;
        byte[] data = new byte[7];
        data[0] = 0x04;
        data[1] = callback_index;
        data[2] = (byte) (flash_addr & 0xff);
        data[3] = (byte) (flash_addr >> 8);
        data[4] = (byte) (flash_addr >> 16);
        data[5] = (byte) (flash_addr >> 24);
        data[6] = size;
         */
        writeCharacteristic(mCommTXRXCharacteristic, content).enqueue();
        return 0;
    }

    public int norFlash_write() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onNorFlashWriteResponse(data.getValue());
        });
        byte[] data = new byte[]{0x09};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int norFlash_erase() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onNorFlashEraseResponse(data.getValue());
        });
        byte[] data = new byte[]{0x09};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    public int app_short_msg(byte[] msgData) {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onAppShortMsgResponse(data.getValue());
        });
        writeCharacteristic(mCommTXRXCharacteristic, msgData).enqueue();
        return 0;
    }


    public int current_data_update() {
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            bleManagerCallbacks.onCurrentDataUpdate(data.getValue());
        });
        byte[] data = new byte[]{0X10};
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    /********************************************   OTA升级    *************************************************/
    //蓝牙升级腕表固件
    private int dfuImageDataOffset = 0;
    private int dfuImageDataSize = 0;
    private int dfuImageDataCrc = 0;

    public int dfu_start(DfuBeanEvent dfuFile) {
        this.dfuFile = dfuFile;
        dfu_cmd_start();
        return 0;
    }


    //OTA 升级
    private int dfu_total_image_size = 0;
    private int dfu_total_image_send = 0; //用于做进度条

    private int dfu_cmd_start() {
        bleManagerCallbacks.onDfuStatus("开始升级");

        dfu_total_image_send = 0;

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            dfu_cmd_bootsetting_apollo();

        });
        dfu_total_image_size = dfuFile.getApolloBinSize() + dfuFile.getNrf52BinSize();
        byte[] data = new byte[5];
        data[0] = 0x30;
        data[1] = (byte) (dfu_total_image_size & 0xff);
        data[2] = (byte) (dfu_total_image_size >> 8);
        data[3] = (byte) (dfu_total_image_size >> 16);
        data[4] = (byte) (dfu_total_image_size >> 24);
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_bootsetting_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送APOLLO BOOTSETTING");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfu_cmd_prevalidate_apollo();
            }
        });
        byte[] data = new byte[dfuFile.getApolloDatSize() + 1];
        data[0] = 0x31;
        byte[] bootsetting = FileHelper.readFile(dfuFile.getApolloBootSettingPath(), 0, dfuFile.getApolloDatSize());
        System.arraycopy(bootsetting, 0, data, 1, bootsetting.length);
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_prevalidate_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送APOLLO PREVALIDATE");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            dfu_cmd_image_data_start_apollo();
        });
        byte[] data = new byte[1];
        data[0] = 0x32;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }


    private int dfu_cmd_image_data_start_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送APOLLO IMAGE DATA");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfuImageDataOffset += dfuImageDataSize;
                dfu_total_image_send += dfuImageDataSize;
                dfu_cmd_image_data_apollo();
            }


        });
        dfuImageDataOffset = 0;
        dfuImageDataSize = 231;
        dfuImageDataCrc = 0;
        byte[] data = new byte[1 + 4 + 4 + 4 + 231];
        data[0] = 0x33;
        data[1] = (byte) (dfuImageDataOffset & 0xff);
        data[2] = (byte) (dfuImageDataOffset >> 8);
        data[3] = (byte) (dfuImageDataOffset >> 16);
        data[4] = (byte) (dfuImageDataOffset >> 24);
        data[5] = (byte) (dfuImageDataSize & 0xff);
        data[6] = (byte) (dfuImageDataSize >> 8);
        data[7] = (byte) (dfuImageDataSize >> 16);
        data[8] = (byte) (dfuImageDataSize >> 24);
        byte[] bin = FileHelper.readFile(dfuFile.getApolloBinPath(), 0, dfuImageDataSize);
        dfuImageDataCrc = CRC32.fast(bin);
        data[9] = (byte) (dfuImageDataCrc & 0xff);
        data[10] = (byte) (dfuImageDataCrc >> 8);
        data[11] = (byte) (dfuImageDataCrc >> 16);
        data[12] = (byte) (dfuImageDataCrc >> 24);
        System.arraycopy(bin, 0, data, 13, bin.length);
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_image_data_send_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        if ((dfuFile.getApolloBinSize() - dfuImageDataOffset) > 231) {
            dfuImageDataSize = 231;
        } else {
            dfuImageDataSize = dfuFile.getApolloBinSize() - dfuImageDataOffset;
        }
        dfuImageDataCrc = 0;
        byte[] data = new byte[1 + 4 + 4 + 4 + 231];
        data[0] = 0x33;
        data[1] = (byte) (dfuImageDataOffset & 0xff);
        data[2] = (byte) (dfuImageDataOffset >> 8);
        data[3] = (byte) (dfuImageDataOffset >> 16);
        data[4] = (byte) (dfuImageDataOffset >> 24);
        data[5] = (byte) (dfuImageDataSize & 0xff);
        data[6] = (byte) (dfuImageDataSize >> 8);
        data[7] = (byte) (dfuImageDataSize >> 16);
        data[8] = (byte) (dfuImageDataSize >> 24);
        byte[] bin = FileHelper.readFile(dfuFile.getApolloBinPath(), dfuImageDataOffset, dfuImageDataSize);
        dfuImageDataCrc = CRC32.fast(bin);
        data[9] = (byte) (dfuImageDataCrc & 0xff);
        data[10] = (byte) (dfuImageDataCrc >> 8);
        data[11] = (byte) (dfuImageDataCrc >> 16);
        data[12] = (byte) (dfuImageDataCrc >> 24);
        System.arraycopy(bin, 0, data, 13, bin.length);
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_image_data_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送APOLLO IMAGE DATA");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfuImageDataOffset += dfuImageDataSize;
                dfu_total_image_send += dfuImageDataSize;
                if (dfuImageDataOffset % 4158 == 0 || dfuImageDataOffset >= dfuFile.getApolloBinSize()) {
                    dfu_cmd_image_data_write_apollo();
                    bleManagerCallbacks.onDfuProgress((dfu_total_image_send * 100) / dfu_total_image_size);
                } else {
                    dfu_image_data_send_apollo();
                }
            }


        });
        dfu_image_data_send_apollo();
        return 0;
    }

    private int dfu_cmd_image_data_write_apollo() {

//        bleManagerCallbacks.onDfuStatus("发送APOLLO IMAGE DATA WRITE");
        resetTimer(TIMER_STATE_REMOVE);
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));
            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                if (dfuImageDataOffset >= dfuFile.getApolloBinSize()) {
                    dfu_cmd_postvalidate_apollo();
                } else {
                    dfu_cmd_image_data_apollo();
                }
            }


        });
        byte[] data = new byte[1];
        data[0] = 0x34;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_postvalidate_apollo() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送APOLLO POSTVALIDATE");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfu_cmd_reset_n_activate_apollo();
            }

        });
        byte[] data = new byte[1];
        data[0] = 0x35;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_reset_n_activate_apollo() {
        bleManagerCallbacks.onDfuStatus("发送APOLLO RESET N ACTIVATE");
        resetTimer(TIMER_STATE_REMOVE);
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("错误");
            } else {
                dfu_cmd_bootsetting_nrf52();
            }

        });
        byte[] data = new byte[1];
        data[0] = 0x36;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_bootsetting_nrf52() {

        bleManagerCallbacks.onDfuStatus("发送NRF52 BOOTSETTING");
        resetTimer(TIMER_STATE_REMOVE);
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));
            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfu_cmd_prevalidate_nrf52();
            }

        });
        byte[] data = new byte[dfuFile.getNrf52DatSize() + 1];
        data[0] = 0x37;
        byte[] bootsetting = FileHelper.readFile(dfuFile.getNrf52BootSettingPath(), 0, dfuFile.getNrf52DatSize());
        System.arraycopy(bootsetting, 0, data, 1, bootsetting.length);
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();


        return 0;
    }

    private int dfu_cmd_prevalidate_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送NRF52 PREVALIDATE");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));
            dfu_cmd_image_data_start_nrf52();
        });
        byte[] data = new byte[1];
        data[0] = 0x38;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_image_data_start_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送NRF52 IMAGE DATA");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfuImageDataOffset += dfuImageDataSize;
                dfu_total_image_send += dfuImageDataSize;
                dfu_cmd_image_data_nrf52();
            }
        });
        dfuImageDataOffset = 0;
        dfuImageDataSize = 231;
        dfuImageDataCrc = 0;
        byte[] data = new byte[1 + 4 + 4 + 4 + 231];
        data[0] = 0x39;
        data[1] = (byte) (dfuImageDataOffset & 0xff);
        data[2] = (byte) (dfuImageDataOffset >> 8);
        data[3] = (byte) (dfuImageDataOffset >> 16);
        data[4] = (byte) (dfuImageDataOffset >> 24);
        data[5] = (byte) (dfuImageDataSize & 0xff);
        data[6] = (byte) (dfuImageDataSize >> 8);
        data[7] = (byte) (dfuImageDataSize >> 16);
        data[8] = (byte) (dfuImageDataSize >> 24);
        byte[] bin = FileHelper.readFile(dfuFile.getNrf52BinPath(), 0, dfuImageDataSize);
        dfuImageDataCrc = CRC32.fast(bin);
        data[9] = (byte) (dfuImageDataCrc & 0xff);
        data[10] = (byte) (dfuImageDataCrc >> 8);
        data[11] = (byte) (dfuImageDataCrc >> 16);
        data[12] = (byte) (dfuImageDataCrc >> 24);
        System.arraycopy(bin, 0, data, 13, bin.length);
//        Logger.i(Logger.BLE_TAG, byte2HexStr(data));
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_image_data_send_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        if ((dfuFile.getNrf52BinSize() - dfuImageDataOffset) > 231) {
            dfuImageDataSize = 231;
        } else {
            dfuImageDataSize = dfuFile.getNrf52BinSize() - dfuImageDataOffset;
        }
        dfuImageDataCrc = 0;
        byte[] data = new byte[1 + 4 + 4 + 4 + 231];
        data[0] = 0x39;
        data[1] = (byte) (dfuImageDataOffset & 0xff);
        data[2] = (byte) (dfuImageDataOffset >> 8);
        data[3] = (byte) (dfuImageDataOffset >> 16);
        data[4] = (byte) (dfuImageDataOffset >> 24);
        data[5] = (byte) (dfuImageDataSize & 0xff);
        data[6] = (byte) (dfuImageDataSize >> 8);
        data[7] = (byte) (dfuImageDataSize >> 16);
        data[8] = (byte) (dfuImageDataSize >> 24);
        byte[] bin = FileHelper.readFile(dfuFile.getNrf52BinPath(), dfuImageDataOffset, dfuImageDataSize);
        dfuImageDataCrc = CRC32.fast(bin);
        data[9] = (byte) (dfuImageDataCrc & 0xff);
        data[10] = (byte) (dfuImageDataCrc >> 8);
        data[11] = (byte) (dfuImageDataCrc >> 16);
        data[12] = (byte) (dfuImageDataCrc >> 24);
        System.arraycopy(bin, 0, data, 13, bin.length);
//        Logger.i(Logger.BLE_TAG, byte2HexStr(data));
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_image_data_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送NRF52 IMAGE DATA");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfuImageDataOffset += dfuImageDataSize;
                dfu_total_image_send += dfuImageDataSize;
                if (dfuImageDataOffset % 4158 == 0 || dfuImageDataOffset >= dfuFile.getNrf52BinSize()) {
                    dfu_cmd_image_data_write_nrf52();
                    bleManagerCallbacks.onDfuProgress((dfu_total_image_send * 100) / dfu_total_image_size);
                } else {
                    dfu_image_data_send_nrf52();
                }
            }
        });
        dfu_image_data_send_nrf52();
        return 0;
    }

    private int dfu_cmd_image_data_write_nrf52() {

//        bleManagerCallbacks.onDfuStatus("发送NRF52 IMAGE DATA WRITE");
        resetTimer(TIMER_STATE_REMOVE);
        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                if (dfuImageDataOffset >= dfuFile.getNrf52BinSize()) {
                    dfu_cmd_postvalidate_nrf52();
                } else {
                    dfu_cmd_image_data_nrf52();
                }
            }
        });
        byte[] data = new byte[1];
        data[0] = 0x3A;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_postvalidate_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送NRF52 POSTVALIDATE");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("CRC 错误");
            } else {
                dfu_cmd_reset_n_activate_nrf52();
            }

        });
        byte[] data = new byte[1];
        data[0] = 0x3B;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();
        return 0;
    }

    private int dfu_cmd_reset_n_activate_nrf52() {
        resetTimer(TIMER_STATE_REMOVE);
        bleManagerCallbacks.onDfuStatus("发送NRF52 RESET N ACTIVATE");

        setNotificationCallback(mCommTXRXCharacteristic).with((device, data) -> {
//            Logger.i(Logger.BLE_TAG, byte2HexStr(data.getValue()));

            if (data.getValue()[1] != 0) {
                bleManagerCallbacks.onDfuStatus("错误");
            } else {
                bleManagerCallbacks.onDfuStatus("升级结束");
            }

        });
        byte[] data = new byte[1];
        data[0] = 0x3C;
        resetTimer(TIMER_STATE_SEND);
        writeCharacteristic(mCommTXRXCharacteristic, data).enqueue();

        return 0;
    }
    //蓝牙升级腕表固件

    /**
     * 失败处理 机制
     */
    protected final int DELAY_WATCH_ERROR = 0x15;
    private final int TIMER_STATE_REMOVE = 0x01;
    private final int TIMER_STATE_SEND = 0x02;
    private final int TIMER_STATE_ALL = 0x03;

    private void resetTimer(int state) {
        switch (state) {
            case TIMER_STATE_REMOVE:
                mHadler.removeMessages(DELAY_WATCH_ERROR);
                break;
            case TIMER_STATE_SEND:
                mHadler.sendEmptyMessageDelayed(DELAY_WATCH_ERROR, 3000);
                break;
            case TIMER_STATE_ALL:
                mHadler.removeMessages(DELAY_WATCH_ERROR);
                mHadler.sendEmptyMessageDelayed(DELAY_WATCH_ERROR, 3000);
                break;
        }
    }

    private Handler mHadler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELAY_WATCH_ERROR:
                    removeMessages(DELAY_WATCH_ERROR);
                    bleManagerCallbacks._onUpdateError();
                    break;
            }
        }
    };
}
