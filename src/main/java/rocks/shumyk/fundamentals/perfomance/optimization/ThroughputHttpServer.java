package rocks.shumyk.fundamentals.perfomance.optimization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class ThroughputHttpServer {

	public static final String INPUT_FILE = "resources/throughput/war_and_peace.txt";
	public static final int THREADS_NUMBER = 4;

	public static void main(String[] args) throws IOException {
		final String text = new String(Files.readAllBytes(Paths.get(INPUT_FILE)));
		startServer(text);
	}

	private static void startServer(String text) throws IOException {
		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		final Executor executor = Executors.newFixedThreadPool(THREADS_NUMBER);

		server.createContext("/search", new WordCountHandler(text));
		server.setExecutor(executor);

		server.start();
	}

	@Data
	private static class WordCountHandler implements HttpHandler {
		private final String text;

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String query = exchange.getRequestURI().getQuery();
			final String[] keyValue = query.split("=");
			String action = keyValue[0];
			String word = keyValue[1];

			if (!action.equalsIgnoreCase("word")) {
				exchange.sendResponseHeaders(400, 0);
				return;
			}

			long count = countWord(word);
			log.info("Word [{}] appeared {} times in text", word, count);


			final byte[] response = Long.toString(count).getBytes();
			exchange.sendResponseHeaders(200, response.length);
			final OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(response);
			outputStream.close();
		}

		private long countWord(String word) {
			long count = 0;
			int index = 0;

			while (index >= 0) {
				index = text.indexOf(word, index);
				if (index >= 0) {
					count++;
					index++;
				}
			}
			return count;
		}
	}
}
