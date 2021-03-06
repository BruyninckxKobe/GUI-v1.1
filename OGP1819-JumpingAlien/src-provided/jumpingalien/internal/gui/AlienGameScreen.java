package jumpingalien.internal.gui;

import jumpingalien.internal.gui.AlienScreenPanel;
import jumpingalien.internal.gui.JumpingAlienGUI;
import jumpingalien.internal.gui.JumpingAlienGUIOptions;
import jumpingalien.internal.gui.painters.*;

import java.awt.Color;
import java.util.Optional;

import jumpingalien.internal.game.IActionHandler;
import jumpingalien.internal.game.JumpingAlienGame;
import ogp.framework.gui.MessagePainter;
import ogp.framework.gui.Screen;
import ogp.framework.gui.SolidBackgroundPainter;
import ogp.framework.gui.camera.Camera;
import ogp.framework.gui.camera.CameraScreen;
import ogp.framework.gui.camera.Camera.Rectangle;

public class AlienGameScreen
		extends CameraScreen<JumpingAlienGame, JumpingAlienGUI> {


	private static final int DEBUG_PIXELS_ZOOM = 8;

	private Camera mainCamera;

	public AlienGameScreen(AlienScreenPanel panel, JumpingAlienGUI gui,
			Screen<JumpingAlienGame, JumpingAlienGUI> previous) {
		super(panel, gui, previous);
	}

	public JumpingAlienGUIOptions getOptions() {
		return getGUI().getGUIOptions();
	}

	public IActionHandler getActionHandler() {
		return getGame().getActionHandler();
	}

	@Override
	public void screenStarted() {
		super.screenStarted();
		getGame().start();
	}

	@Override
	public void screenStopped() {
		super.screenStopped();
		getGUI().exit();
	}
	

	@Override
	protected void setupCameras() {
		Optional<int[]> worldSize = getGame().getWorldInfoProvider()
				.getWorldSize();
		if (!worldSize.isPresent()) {
			throw new IllegalStateException("World size must be set!");
		}
		int worldWidth = worldSize.get()[0];
		int worldHeight = worldSize.get()[1];

		if (getOptions().getDebugShowPixels()) {
			mainCamera = new Camera(new Rectangle(0, 0, worldWidth
					/ DEBUG_PIXELS_ZOOM, worldHeight / DEBUG_PIXELS_ZOOM),
					new Rectangle(0, 0, getScreenWidth(), getScreenHeight()));
		} else {
			int widthOnScreen = getScreenWidth();
			int heightOnScreen = getScreenHeight();

			double scale = 1.0;

			if (widthOnScreen < worldWidth) {
				scale = (double) widthOnScreen / worldWidth;
			}

			if (heightOnScreen < worldHeight) {
				scale = Math.min(scale, (double) heightOnScreen / worldHeight);
			}

			heightOnScreen = (int) (scale * worldHeight);
			widthOnScreen = (int) (scale * worldWidth);
			int screenX = (getScreenWidth() - widthOnScreen) / 2;
			int screenY = (getScreenHeight() - heightOnScreen) / 2;

			mainCamera = new Camera(
					new Rectangle(0, 0, worldWidth, worldHeight),
					new Rectangle(screenX, screenY, widthOnScreen,
							heightOnScreen));
		}
		addCamera(mainCamera);
	}
	
	@Override
	protected void setupPainters() {
		addPainter(new SolidBackgroundPainter(Color.BLACK, this));

		if (getOptions().getDebugShowInfo()) {
			addPainter(new DebugInfoPainter(this));
		}

		if (getOptions().getDebugShowAxes()) {
			addPainter(new AxesPainter(this));
		}

		if (getOptions().getDebugShowPixels()) {
			addPainter(new PixelPainter(this));
		}

		if (getOptions().getDebugShowHistory()) {
			addPainter(new HistoryPainter(this));
		}

		addPainter(new PlayerPainter(this));

		addPainter(new WorldBorderPainter(this));

		addPainter(new MessagePainter<>(this,
				getGame()::getCurrentMessage));
	}

	@Override
	public void updateState(double dt) {
		if (getOptions().getDebugShowPixels()) {
			getGame()
					.getAlienInfoProvider()
					.getAlienXYPixel()
					.ifPresent(
							xy -> mainCamera.moveToWorldLocation(xy[0], xy[1]));
		}
	}

	@Override
	protected AlienInputMode createDefaultInputMode() {
		return new AlienInputMode(this, null);
	}

	@Override
	public Camera getMainCamera() {
		return mainCamera;
	}
}
