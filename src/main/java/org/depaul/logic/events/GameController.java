package org.depaul.logic.events;

import org.depaul.gui.GuiController;
import org.depaul.logic.board.Board;
import org.depaul.logic.board.SimpleBoard;
import org.depaul.logic.data.ViewData;

import java.util.concurrent.ThreadLocalRandom;

public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10); // WHY IS THE HEIGHT OF THE BOARD CALLED "width" ???

    private final GuiController viewGuiController;
    private final ScoreManager scoreManager;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        scoreManager = new ScoreManager(c, board.getScore());
    }

    @Override
    public ViewData onMoveEvent(MoveEvent event) {
        boolean movable = board.moveBrick(event);
        if (!movable) {
            
            board.mergeBrickToBackground();
            scoreManager.addBrickScore(4);

            int num_cleared_lines = board.clearLines();
            scoreManager.addLinesCleared(num_cleared_lines);
            
            scoreManager.scorePoints();

            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
           // viewGuiController.refreshGameBackground(board.getBoardMatrix(), board.getViewData()); samira
        } else {
            if (event.getEventSource() == EventSource.USER) {
                // board.getScore().add(ThreadLocalRandom.current().nextInt(100)); TODO: keep this commented out
            }
        }

        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        boolean movable = board.rotateBrick();
        if (!movable) {
//            viewGuiController.updateLevel(board.getScore().getLevel()); TODO: update level and scores
            viewGuiController.refreshGameBackground(board.getBoardMatrix());
        }
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.getScore().reset();
        board.newGame();
        scoreManager.reset();
       // viewGuiController.refreshGameBackground(board.getBoardMatrix(), board.getViewData()); Samira
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}
