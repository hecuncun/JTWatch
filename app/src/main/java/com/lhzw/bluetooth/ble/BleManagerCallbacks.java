package com.lhzw.bluetooth.ble;


public interface BleManagerCallbacks extends no.nordicsemi.android.ble.BleManagerCallbacks {


    void onMtuUpdateResponse(byte[] response);

    void onConnectionUpdateResponse(byte[] response);

    void onSettingConnectParameter(byte[] response);

    void onDeviceInfoResponse(byte[] response);

    void onWatchTimeUpdateResponse(byte[] response);

    void onPersonalInfoReadResponse(byte[] response);

    void onPersonalInfoUpdateResponse(byte[] response);

    void onSportsParamReadResponse(byte[] response, String ID);

    void onSportsParamUpdateResponse(byte[] response);

    void onDailyDataRequestResponse(byte[] response);

    void onActivityAddressRequestResponse(byte[] response);

    void onSportDetailInfoResponse(byte[] response, byte request_code, int type, String ID);

    void onNorFlashReadResponse(byte[] response, String ID);

    void onNorFlashWriteResponse(byte[] response);

    void onNorFlashEraseResponse(byte[] response);

    void onAppShortMsgResponse(byte[] response);

    void onWatchDataResponse(byte[] response);

    void onCurrentDataUpdate(byte[] response);

    void onPersonalInfoSaveResponse(byte[] response);

    void onDfuStatus(String message);

    void onDfuProgress(int progress);

    void _onReconnectResponse(byte[] response);

    void _onMtuUpdateResponse(byte[] response);

    void _onUpdateError();
}
