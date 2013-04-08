package com.example.libsysmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SocketClient extends Thread {
	String scannedCode = "";
	MainActivity a;
	String serverIP;
	String serverPort;

	public SocketClient(MainActivity a) {
		this.a = a;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
		serverIP = sp.getString("IP", "192.168.1.1");
		serverPort = sp.getString("PORT", "6789");
		Log.d("Socket", "Adress: "+ serverIP+", Port: " + serverPort);
	}

	public void send(String code) {
		scannedCode = code;
		start();
	}

	public void run() {
		if (scannedCode == "") {
			return;
		}

		try {
			// PRIPOJENI
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setInfoString("Odesílám...");
					a.setButtonActive(false);
				}
			});
			
			
			Log.d("Socket", "Pripojuji");
			Socket clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(serverIP, Integer.parseInt(serverPort)),2000);
			clientSocket.setSoTimeout(2000);
			
			PrintWriter outToServer = new PrintWriter(
					clientSocket.getOutputStream(), true);
			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			Log.d("Socket", "pripojeno");
			// PRENOS
			String fromServer;

			// SEND CODE
			outToServer.println("CODE" + scannedCode);
			fromServer = inFromServer.readLine();
			Log.d("Socket", fromServer);

			// ANSWER
			if (!fromServer.equals("BOOK_FOUND")) {
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						a.setTitleS("Kniha nenalezena");
						a.setAuthor("");
						a.setInfoString("Tuto knihu systém neeviduje");
						a.setButtonActive(true);
					}
				});
				
				clientSocket.close();
				return;
			}

			// TITLE
			outToServer.println("TITLE");
			fromServer = inFromServer.readLine();
			final String fromServerFinal = fromServer;
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setTitleS(fromServerFinal);
				}
			});
			Log.d("Socket", fromServer);

			// AUTHOR
			outToServer.println("AUTHOR");
			fromServer = inFromServer.readLine();
			final String fromServerFinal2 = fromServer;
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setAuthor(fromServerFinal2);
				}
			});
			Log.d("Socket", fromServer);

			// UKONCENI SPOJENI
			outToServer.println("END");
			clientSocket.close();
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setInfoString("Úspìšnì odesláno");
					a.setButtonActive(true);
				}
			});
			// VYJIMKY
		} catch (SocketTimeoutException e) {
			Log.d("Socket", "Timeout");
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setInfoString("LibSys Server neodpovídá. Zkontrolujte nastavení");
					a.setButtonActive(true);
				}
			});

		} catch (IOException e) {
			Log.d("Socket", "Odpojeno");
			a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					a.setInfoString("LibSys Server Neodpovídá. Zkontrolujte nastavení");
					a.setButtonActive(true);
				}
			});
		}

	}
	
}
