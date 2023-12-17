package org.depaul.logic.board;

import org.depaul.logic.bricks.Brick;
import org.depaul.logic.bricks.BrickGenerator;
import org.depaul.logic.bricks.RandomBrickGenerator;
import org.depaul.logic.data.Score;
import org.depaul.logic.data.ViewData;
import org.depaul.logic.events.EventSource;
import org.depaul.logic.events.EventType;
import org.depaul.logic.events.MoveEvent;
import org.depaul.logic.rotator.BrickRotator;
import org.depaul.logic.util.Operations;

import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(3, 0);
        return Operations.intersectMatrix(currentGameMatrix, brickRotator.getCurrentShapeMatrix(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public boolean rotateBrick() {
        int[][] currentMatrix = Operations.copyMatrix(currentGameMatrix);
        Point p = new Point(currentOffset);
        boolean conflict = Operations.intersectMatrix(currentMatrix, brickRotator.getNextShapeMatrix(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            // conflict on rotate == ignore move
            return true;
        } else {
            brickRotator.setCurrentShapeIndex(brickRotator.getNextShapeIndex());
            return true;
        }
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShapeMatrix(), (int) currentOffset.getX(), (int) currentOffset.getY(), brickGenerator.getNextBrick().getBrickMatrixList().get(0));
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = Operations.mergeMatrix(currentGameMatrix, brickRotator.getCurrentShapeMatrix(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    private void shiftRowsDown(int from_row)
    {
        assert(from_row >= 0 && from_row < width);
        for(int i = from_row; i > 0; i--)
        {
            // Java 2D arrays should be an array of references, so this should work
            // Kinda evil if you ask me
            // - Klaudius
            int[] arr = currentGameMatrix[i];
            currentGameMatrix[i] = currentGameMatrix[i - 1];
            currentGameMatrix[i - 1] = arr;
        }
    }
    private void zeroOutRow(int row)
    {
        assert(row >= 0 && row < width);
        for(int col = 0; col < height; col++)
        {
            currentGameMatrix[row][col] = 0;
        }
    }
    private boolean isRowFull(int row)
    {
        assert(row >= 0 && row < width);
        for(int col = 0; col < height; col++)
        {
            if(currentGameMatrix[row][col] == 0)
                return false;
        }
        return true;
    }
    @Override
    public int clearLines() {
        int num_cleared_lines = 0;
        for(int row = 0; row < width; row++)
        {
            if(isRowFull(row))
            {
                num_cleared_lines++;
                zeroOutRow(row);
                shiftRowsDown(row);
            }
        }
        return num_cleared_lines;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }

    @Override
    public boolean moveBrick(MoveEvent event) {
        int[][] currentMatrix = Operations.copyMatrix(currentGameMatrix);
        int[][] currentBrickMatrix = brickRotator.getCurrentShapeMatrix();
        int[][] nextRotateBrickMatrix = brickRotator.getNextShapeMatrix(); //rotated 1 time
        Point p = new Point(currentOffset);
        // We must see if there is a conflict with the move 
        //if shape is still moveable after the move then return true
        boolean conflict = isConflict(currentMatrix, currentBrickMatrix, p);

        if(event.eventType() == EventType.DOWN && event.eventSource() == EventSource.THREAD) {
            p.translate(0,1); // Move the brick 
            conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            if(conflict) {
                return false;
            } else {
                currentOffset = p;
                return true;
            }
        } else if (event.eventType() == EventType.LEFT && event.eventSource() == EventSource.THREAD) {
            p.translate(-1,0); 
            conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            if(!conflict) {
                currentOffset = p;
                return true;
            } 
        } else if (event.eventType() == EventType.RIGHT && event.eventSource() == EventSource.THREAD) {
            p.translate(1,0); 
            conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            if(!conflict) {
                currentOffset = p;
                return true;
            } 
        } else if (event.eventType() == EventType.SPACE && event.eventSource() == EventSource.THREAD) {
            conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            while (!conflict) {
                p.translate(0, 1);
                conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            }
            p.translate(0, -1);
            currentOffset = p;
            return true;

        } else if (event.eventType() == EventType.UP && event.eventSource() == EventSource.THREAD) {
            conflict = isConflict(currentMatrix, currentBrickMatrix, p);
            if(!conflict) {
                currentOffset = p;
                return true;
            } 

        } else if (event.eventType() == EventType.Z && event.eventSource() == EventSource.THREAD) {
            return rotateBrick();
        }

        return conflict;
    }

    private boolean isConflict(int[][] currentMatrix, int[][] currentBrickMatrix, Point p) {
        boolean conflict = Operations.intersectMatrix(currentMatrix, currentBrickMatrix, (int) p.getX(), (int) p.getY());
        return conflict;
    }
}
