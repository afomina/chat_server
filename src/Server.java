import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Server {
	public static final int PORT = 6680;
	public static final String ADDRESS = "127.0.0.1";
	public static final int AUTH = 1;
	static ServerSocket serverSocket;
	static DataInputStream input;
	static DataOutputStream output;
	static BufferedReader reader;
	static String logFile = "log.txt";
	static PrintWriter writer;

	public static InetAddress getAddress() throws UnknownHostException {
		return InetAddress.getByName(ADDRESS);
	}

	public Server() throws IOException {
		writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
		serverSocket = new ServerSocket(PORT);
		Socket socket = serverSocket.accept();
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
	}

	public static void main(String[] args) {
		try {
			new Server();

			boolean loginSuccess = getAuthData();
			while (true) {
				if (loginSuccess) {
					writer.close();
					String msg = input.readUTF();
					output.writeUTF(msg);
					output.flush();
				} else {
					loginSuccess = getAuthData();
				}
			}

		} catch (SocketException e) {
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static boolean getAuthData() throws IOException {
		boolean loginSuccess = false;
		String login = "";

		login = input.readUTF();
		String pass = input.readUTF();

		reader = new BufferedReader(new FileReader("users.txt"));
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

		if (!loginSuccess) {
			output.writeUTF("Login failed");
			writer.append(login + " login failed\n");
		} else {
			output.writeUTF(login);
			writer.append(login + " login success\n");
		}
		output.flush();
		reader.close();

		return loginSuccess;
	}
}
