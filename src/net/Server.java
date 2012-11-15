package net;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {
	public static final int SERVER_PORT = 6680;
	public static final int CONNECT_PORT = 6681;
	public static final String ADDRESS = "127.0.0.1";
	public static final int AUTH = 1;
	ServerSocket serverSocket;
	ServerSocket connectServerSocket;
	DataInputStream connectIn;
	DataOutputStream connectOut;
	static String logFile = "log.txt";
	static String usersFile = "users.txt";
	ServerFrame serverOut;

	public Server() {
		serverOut = new ServerFrame();
		serverOut.println("Waiting for connection...");

		try {
			connectServerSocket = new ServerSocket(CONNECT_PORT);
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			serverOut.println("Cannot listen on port: " + SERVER_PORT);
			serverOut.println("Details: " + trace);
		}
	}

	public void listen() throws IOException {
		while (true) {
			Socket msgSocket;
			msgSocket = serverSocket.accept();

			Thread clientThread = new Thread(new ServerListener(msgSocket));
			clientThread.start();
		}
	}

	public void connect() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Socket connectionSocket = connectServerSocket.accept();
					connectIn = new DataInputStream(
							connectionSocket.getInputStream());
					connectOut = new DataOutputStream(
							connectionSocket.getOutputStream());
					while (true) {
						int data = connectIn.readInt();
						if (data == AUTH) {
							listen();
						}
					}
				} catch (IOException e) {
					serverOut.println(e.getMessage());
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
	}

	synchronized boolean getAuthData(DataInputStream in, DataOutputStream out)
			throws IOException {
		boolean loginSuccess = false;

		String login;
		String pass;
		try {
			login = in.readUTF();
			pass = in.readUTF();
		} catch (NullPointerException e) {
			return false;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(usersFile));
		} catch (FileNotFoundException e) {
			serverOut.println("File not found: " + usersFile);
			return false;
		}
		String line;
		try {
			while ((line = reader.readLine()) != null) {

				String[] data = line.split(" ");

				if (data[0].equals(login)) {
					String correctPass = data[1];

					if (correctPass.equals(pass)) {
						loginSuccess = true;
					}
					break;
				}
			}
		} catch (IOException e) {
			serverOut.println("Can't read file: " + usersFile);
		}

		PrintWriter logWriter = null;
		try {
			logWriter = new PrintWriter(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			serverOut.println("File not found: " + logFile);
		}
		try {
			logWriter.append(new Date().toString() + " ");
			if (!loginSuccess) {
				String msg = login + " login failed\n";
				out.writeUTF(msg);
				logWriter.append(msg);
				serverOut.println(msg);
			} else {
				out.writeUTF(login);
				logWriter.append(login + " login success\n");
				serverOut.println(login + " log in");
			}
		} finally {
			out.flush();
			reader.close();
			logWriter.close();
		}
		return loginSuccess;
	}

	class ServerListener implements Runnable {
		Socket msgSocket;

		public ServerListener(Socket msgSocket) {
			this.msgSocket = msgSocket;
		}

		@Override
		public void run() {
			try {
				DataInputStream input = new DataInputStream(
						msgSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(
						msgSocket.getOutputStream());
				boolean loginSuccess = getAuthData(input, output);
				while (true) {
					if (loginSuccess) {
						String msg = input.readUTF();
						serverOut.println(msg);
						output.writeUTF(msg);
						output.flush();
					} else {
						loginSuccess = getAuthData(input, output);
					}
				}
			} catch (IOException e) {
				serverOut.println(e.getMessage());
			}
		}
	}
}
