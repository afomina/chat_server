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

	public static InetAddress getAddress() throws UnknownHostException {
		return InetAddress.getByName(ADDRESS);
	}

	public static void main(String[] args) throws IOException {
		try {
			serverSocket = new ServerSocket(PORT);
			Socket socket = serverSocket.accept();
			System.out.println("Connected to client");
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());

			reader = new BufferedReader(new FileReader("users.txt"));
			getAuthData();
			while (true) {

				String msg = input.readUTF();
				output.writeUTF(msg);
				output.flush();
			}
		} catch (SocketException e) {
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void getAuthData() throws IOException {
		int auth = input.readByte();
		if (auth == 1) {
			String login = input.readUTF();
			String pass = input.readUTF();

			boolean userExists = false;
			String line;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split(" ");
				if (data[0].equals(login)) {
					userExists = true;
					String correctPass = data[1];
					if (correctPass.equals(pass)) {
						output.writeUTF(login);
						break;
					} else {
						output.writeUTF("Login failed");
						break;
					}
				}
			}
			reader.close();
			if (!userExists) {
				output.writeUTF("Login failed");
			}
		}
	}
}
