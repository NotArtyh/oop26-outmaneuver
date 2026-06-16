package outmaneuver;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.MissileControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.model.missile.data.JsonMissileRepository;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.model.missile.data.MissileRepository;
import outmaneuver.model.session.GameState;
import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;
import outmaneuver.view.swing.GameKeyListener;
import outmaneuver.view.swing.SwingGameView;
import outmaneuver.view.swing.UIManager;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.menu.MainMenuView;

public final class AppBootstrapper {

    private static final int SCREEN_W = 800;
    private static final int SCREEN_H = 600;

    private AppBootstrapper() { }

    public static void launch() {
        final JFrame frame = new JFrame("OutManeuver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        final Plane plane = new PlaneImpl(new StandardStats());
        final InputControllerImpl inputCtrl = new InputControllerImpl();
        final HudControllerImpl hudCtrl = new HudControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl(hudCtrl);

        // TEMPORANEO — CollisionEngine verrà integrato da Spina
        final CollisionEngine collisionEngine = new CollisionEngine(master);

        // Carica i dati dei missili da JSON
        final MissileRepository missileRepo = new JsonMissileRepository(
                JsonResourceLoader.forList("missiles.json", MissileData.class, GsonProvider.create()));

        final MissileControllerImpl missileCtrl = new MissileControllerImpl(
                SCREEN_W, SCREEN_H, collisionEngine, missileRepo);
        master.setMissileController(missileCtrl);

        final EntityControllerImpl entity = new EntityControllerImpl(plane, inputCtrl, master, missileCtrl);
        master.setEntityController(entity);

        final SwingGameView gameView = new SwingGameView(new GameKeyListener(inputCtrl, master));
        gameView.init();
        master.attachView(gameView);

        final UIManager[] uiManagerRef = { null };

        final GameOverView gameOverView = new GameOverView();
        final MainMenuView mainMenuView = new MainMenuView(
            () -> onStart(uiManagerRef[0], master, gameView),
            () -> System.exit(0)
        );

        final Map<GameState, JPanel> screens = new EnumMap<>(GameState.class);
        screens.put(GameState.MENU, mainMenuView);
        screens.put(GameState.PLAYING, gameView.getPanel());
        screens.put(GameState.PAUSED, gameView.getPanel());
        screens.put(GameState.GAME_OVER, gameOverView);

        final UIManager uiManager = new UIManager(screens);
        uiManager.showScreen(GameState.MENU);
        uiManagerRef[0] = uiManager;

        frame.add(uiManager);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void onStart(final UIManager uiManager,
                                 final MasterController master,
                                 final SwingGameView gameView) {
        uiManager.showScreen(GameState.PLAYING);
        gameView.getPanel().requestFocusInWindow();
        master.start();
    }
}