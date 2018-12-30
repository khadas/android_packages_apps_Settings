/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings;

import android.app.Service;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.os.SystemProperties;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;

import java.util.List;
import java.util.ArrayList;


import android.util.Log;

public class AtxAgentService extends Service {

	private final static String TAG = "AtxAgentService";
	private final int PORT = 4001;
	private final static String MSG = "khadas";
	private final static String PROCESS_NAME = "atx_agent";
	private String pre_ip = "";
	private AtxThread mAtxThread;
	private BroadcastThread mBroadcastThread;
	private Context mContext;
    @Override
	public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		if (SystemProperties.getBoolean("ro.has.atxagent", false)) {
			Log.d(TAG, "AtxAgentService onCreate()");
			mContext = this;
			mAtxThread = new AtxThread();
			mBroadcastThread = new BroadcastThread();
			mBroadcastThread.start();
		}
	};

	public class AtxThread extends Thread {
		private String server_ip = "";

		public void SetServerIp(String ip) {
			this.server_ip = ip;
		}
		public void run() {
			StartAtxService(server_ip);
		}
	}

	public class BroadcastThread extends Thread {

		public void run() {
			ParseServiceIp();
		}
	}

	private void StartAtxService(String ip) {
		List<String> commandList = new ArrayList<String>();
		commandList.add("su");
		commandList.add("-c");
		commandList.add("/system/bin/atx_agent");
		commandList.add("-d");
		commandList.add("-t");
		commandList.add(ip+":8000");
		commandList.add("&");
		try {
				Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private void StopAtxService(int pid) {
		List<String> commandList = new ArrayList<String>();
		commandList.add("su");
		commandList.add("-c");
		commandList.add("kill");
		commandList.add(Integer.toString(pid));

		try {
				Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private int GetPIDwithName() {
		ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();
		int pid = -1;
		for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess){
			if(amProcess.processName.equals(PROCESS_NAME)){
				pid=amProcess.pid;
				break;
			}
		}
		Log.d(TAG, "Get atx-agent PID: "+pid);
		return pid;
	}

	private void ParseServiceIp()
	{

		try {
				byte data[] = new byte[6];
				String ip = "";
				DatagramSocket mDatagramSocket = new DatagramSocket(PORT);
				DatagramPacket packet = new DatagramPacket(data , data.length);
				Log.d(TAG,"receive start");
				while (true) {
					mDatagramSocket.receive(packet);
					Log.d(TAG,"receive loop");
					if (packet.getAddress() != null) {
						String msg = new String(packet.getData());
						ip = packet.getAddress().toString().substring(1);
						if (msg.equals(MSG)) {
							if (!ip.equals(pre_ip)) {
								int pid = GetPIDwithName();
								if (pid != -1) {
									StopAtxService(pid);
								}
								mAtxThread.SetServerIp(ip);
								mAtxThread.start();
							}
							pre_ip = ip;
						}
						Log.d(TAG,"ip:" + ip+ "  msg: "+ msg);
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
