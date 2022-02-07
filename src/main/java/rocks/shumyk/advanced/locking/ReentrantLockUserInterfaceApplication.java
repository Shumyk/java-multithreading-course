package rocks.shumyk.advanced.locking;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FillTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ReentrantLockUserInterfaceApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Cryptocurrency Prices");

		final var grid = createGrid();
		final var cryptoLabels = createCryptoPriceLabels();

		addLabelsToGrid(cryptoLabels, grid);

		double width = 300D;
		double height = 250D;

		final var root = new StackPane();

		final var background = createBackGroundRectangleWithAnimation(width, height);

		root.getChildren().add(background);
		root.getChildren().add(grid);

		final var scene = new Scene(root, width, height);
		scene.getRoot().setStyle("-fx-font-family: 'serif'");
		primaryStage.setScene(scene);

		final var pricesContainer = new PricesContainer();
		final var pricesUpdater = new PriceUpdater(pricesContainer);
		final AnimationTimer animationTimer = new AnimationTimer() {
			@Override
			public void handle(long l) {
				if (pricesContainer.getLock().tryLock()) {
					try {
						final var bitcoinLabel = cryptoLabels.get("BTC");
						bitcoinLabel.setText(String.valueOf(pricesContainer.getBitcoinPrice()));

						final var etherLabel = cryptoLabels.get("ETH");
						etherLabel.setText(String.valueOf(pricesContainer.getEtherPrice()));

						final var liteCoinLabel = cryptoLabels.get("LTC");
						liteCoinLabel.setText(String.valueOf(pricesContainer.getLitecoinPrice()));

						final var bitcoinCashLabel = cryptoLabels.get("BCH");
						bitcoinCashLabel.setText(String.valueOf(pricesContainer.getBitcoinCashPrice()));

						final var rippleLabel = cryptoLabels.get("XRP");
						rippleLabel.setText(String.valueOf(pricesContainer.getRipplePrice()));

					} finally {
						pricesContainer.getLock().unlock();
					}
				}
			}
		};

		animationTimer.start();
		pricesUpdater.start();
		primaryStage.show();
	}

	private Rectangle createBackGroundRectangleWithAnimation(double width, double height) {
		final var background = new Rectangle(width, height);
		final var fillTransition = new FillTransition(Duration.millis(1000), background, Color.LIGHTGREEN, Color.LIGHTBLUE);
		fillTransition.setCycleCount(Animation.INDEFINITE);
		fillTransition.setAutoReverse(true);
		fillTransition.play();
		return background;
	}

	private void addLabelsToGrid(Map<String, Label> labels, GridPane grid) {
		int row = 0;
		for (Map.Entry<String, Label> entry : labels.entrySet()) {
			final var nameLabel = new Label(entry.getKey());
			nameLabel.setTextFill(Color.BLUE);
			nameLabel.setOnMousePressed(e -> nameLabel.setTextFill(Color.RED));
			nameLabel.setOnMouseReleased(e -> nameLabel.setTextFill(Color.BLUE));

			grid.add(nameLabel, 0, row);
			grid.add(entry.getValue(), 1, row);

			row++;
		}
	}

	private Map<String, Label> createCryptoPriceLabels() {
		final var bitcoinPrice = new Label("0");
		bitcoinPrice.setId("BTC");
		
		final var etherPrice = new Label("0");
		etherPrice.setId("ETH");
		
		final var liteCoinPrice = new Label("0");
		liteCoinPrice.setId("LTC");
		
		final var bitcoinCashPrice = new Label("0");
		bitcoinCashPrice.setId("BCH");
		
		final var ripplePrice = new Label("0");
		ripplePrice.setId("XRP");

		final Map<String, Label> cryptoLabelsMap = new HashMap<>(5);
		cryptoLabelsMap.put("BTC", bitcoinPrice);
		cryptoLabelsMap.put("ETH", etherPrice);
		cryptoLabelsMap.put("LTC", liteCoinPrice);
		cryptoLabelsMap.put("BCH", bitcoinCashPrice);
		cryptoLabelsMap.put("XRP", ripplePrice);
		
		return cryptoLabelsMap;
	}

	private GridPane createGrid() {
		final var grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER);
		return grid;
	}


	@RequiredArgsConstructor
	public static class PriceUpdater extends Thread {
		private final PricesContainer pricesContainer;
		private final Random random = new Random();

		@Override
		public void run() {
			while (true) {
				pricesContainer.getLock().lock();
				try {
					imitateNetworkConnection(1000);

					pricesContainer.setBitcoinPrice(random.nextInt(20_000));
					pricesContainer.setEtherPrice(random.nextInt(2000));
					pricesContainer.setLitecoinPrice(random.nextInt(500));
					pricesContainer.setBitcoinCashPrice(random.nextInt(5000));
					pricesContainer.setRipplePrice(random.nextDouble());
				} finally {
					pricesContainer.getLock().unlock();
				}
				imitateNetworkConnection(2000);
			}
		}

		private void imitateNetworkConnection(int millis) {
			try {
				Thread.sleep(millis); // imitate network call
			} catch (InterruptedException e) {}
		}
	}

	public static class PricesContainer {
		@Getter private final Lock lock = new ReentrantLock();

		@Getter @Setter private double bitcoinPrice;
		@Getter @Setter private double etherPrice;
		@Getter @Setter private double litecoinPrice;
		@Getter @Setter private double bitcoinCashPrice;
		@Getter @Setter private double ripplePrice;
	}
}
