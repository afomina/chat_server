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
import java.net.SocketException;

public class Server {
	public static final int PORT = 6680;
	public static final String ADDRESS = "127.0.0.1";
	public static final int AUTH = 1;
	static ServerSocket serverSocket;
	Socket socket;
	DataInputStream input;
	DataOutputStream output;
	static String logFile = "log.txt";
	static String usersFile = "users.txt";
	ServerFrame serverOut;

	public Server() {
		serverOut = new ServerFrame();
		serverOut.println("Waiting for connection...");

		try {
			serverSocket = new ServerSocket(PORT);
			socket = serverSocket.accept();
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));
			serverOut.println("Cannot listen on port: " + PORT);
			serverOut.println("Details: " + trace);
		}
	}

	public void listen() {
		try {
			boolean loginSuccess = getAuthData();
			while (true) {
				if (loginSuccess) {
					String msg = input.readUTF();
					serverOut.println(msg);
					output.writeUTF(msg);
					output.flush();
				} else {
					loginSuccess = getAuthData();
				}
			}
		} catch (SocketException e) {
			serverOut.println(e.getMessage());
			waitConnection();
		} catch (IOException e) {
			serverOut.println(e.getMessage());
		}
	}

	void waitConnection() {
		try {
			serverOut.println("Waiting for connection...");
			socket = serverSocket.accept();
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			listen();
		} catch (IOException e) {
			serverOut.println("Accept failed");
		}
	}

	boolean getAuthData() throws IOException {
		boolean loginSuccess = false;

		String login;
		String pass;
		try {
			login = input.readUTF();
			pass = input.readUTF();
		} catch (NullPointerException e) {
			return false;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(usersFile));
		} catch (FileNotFoundException e) {
			serverOut.println("File not found: " + usersFile);
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
			if (!loginSuccess) {
				String msg = login + " login failed\n";
				output.writeUTF(msg);
				logWriter.append(msg);
				serverOut.println(msg);
			} else {
				output.writeUTF(login);
				logWriter.append(login + " login success\n");
				serverOut.println(login + " log in");
			}
		} finally {
			output.flush();
			reader.close();
			logWriter.close();
		}
		return loginSuccess;
	}
}
