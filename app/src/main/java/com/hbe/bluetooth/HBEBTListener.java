package com.hbe.bluetooth;

public interface HBEBTListener {
	void onConnected();
	void onDisconnected();
	void onConnecting();
	void onConnectionFailed();
	void onConnectionLost();
	void onReceive(byte[] buff);
}
