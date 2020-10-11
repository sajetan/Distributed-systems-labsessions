package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RemoteSetup {

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			throw new IllegalArgumentException(
					"Provide the username and password that you were given for the remote setup.");
		}

		String username = args[0];
		String password = args[1];
		Boolean startRegistry = Boolean.parseBoolean(args[2]); // whether to start a RMI registry on the server
		int registryPort = Integer.parseInt(args[3]); // on which port to start a RMI registry
		String jar = (args.length > 4) ? args[4] : "rmi_server.jar"; // name of the JAR file that contains the server
																		// code

		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			connection = (HttpURLConnection) new URL("http://10.10.10.250/").openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String basicAuth = "Basic "
					+ new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
			connection.setRequestProperty("Authorization", basicAuth);

			connection.setRequestProperty("X-Registry-Start", startRegistry.toString());
			connection.setRequestProperty("X-Registry-Port", Integer.toString(registryPort));

			FileInputStream fin = new FileInputStream(jar);
			copy(fin, connection.getOutputStream());
			fin.close();

			inputStream = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = rd.readLine()) != null) {
				System.out.println("[REMOTE SERVER] " + line);
			}
			rd.close();
		} finally {
			inputStream.close();
			connection.disconnect();
		}
	}

	// https://stamm-wilbrandt.de/en/blog/Post.java
	// copy method from From E.R. Harold's book "Java I/O"
	public static void copy(InputStream in, OutputStream out) throws IOException {
		// do not allow other threads to read from the
		// input or write to the output while copying is
		// taking place
		synchronized (in) {
			synchronized (out) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	}
}
