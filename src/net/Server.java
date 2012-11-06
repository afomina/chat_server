package net;

import java.io.*;
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

	public Server() throws IOException {
		serverSocket = new ServerSocket(PORT);
		socket = serverSocket.accept();
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
	}

	public void listen() throws IOException {
		try {
			boolean loginSuccess = getAuthData();
			while (true) {
				if (loginSuccess) {
					String msg = input.readUTF();
					System.out.println(msg);
					output.writeUTF(msg);
					output.flush();
				} else {
					loginSuccess = getAuthData();
				}
			}
		} catch (SocketException e) {
			System.out.println("user log out");
			
			socket = serverSocket.accept();
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			listen();
		}
	}

	boolean getAuthData() throws IOException {
		boolean loginSuccess = false;

		String login = input.readUTF();
		String pass = input.readUTF();

		BufferedReader reader = new BufferedReader(new FileReader("users.txt"));
		String line;
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

		PrintWriter writer = new PrintWriter(
				new FileOutputStream(logFile, true));
		try {
			if (!loginSuccess) {
				output.writeUTF("Login failed");
				writer.append(login + " login failed\n");
			} else {
				output.writeUTF(login);
				writer.append(login + " login success\n");
			}
		} finally {
			output.flush();
			reader.close();
			writer.close();
		}
		return loginSuccess;
	}
}
