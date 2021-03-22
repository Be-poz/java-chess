package chess.domain.board;


import chess.domain.piece.*;

import java.util.*;

public class Board {
    private static final double TOTAL_SCORE = 38;
    private static final double PAWN_SAME_HORIZONTAL_SCORE = 0.5;
    public static final int MIN_BORDER = 1;
    public static final int MAX_BORDER = 8;
    private final Map<Position, Piece> board;
    private final Map<Team, Double> lostScoreByTeam;

    public Board(Map<Position, Piece> board) {
        this.board = new LinkedHashMap<>(board);
        this.lostScoreByTeam = initializeDeadPieceByTeamMap();
    }

    private Map<Team, Double> initializeDeadPieceByTeamMap() {
        Map<Team, Double> result = new HashMap<>();
        result.put(Team.BLACK, 0d);
        result.put(Team.WHITE, 0d);
        return result;
    }

    public Piece findPieceFromPosition(Position position) {
        return board.get(position);
    }

    public void movePiece(Position target, Position destination) {
        Piece targetPiece = findPieceFromPosition(target);
        List<Position> targetMovablePositions = targetPiece.searchMovablePositions(target);
        checkMovable(targetMovablePositions, destination);
        if (targetPiece.canMove(target, destination, this)) {
            Piece destinationPiece = findPieceFromPosition(destination);
            exitWhenPieceIsKing(destinationPiece);
            loseScoreWhenDestinationIsPiece(destinationPiece);
            movePieceToPosition(targetPiece, destination);
            clearPosition(target);
            return;
        }
        throw new IllegalArgumentException("기물을 움직일 수 없습니다.");
    }

    private void loseScoreWhenDestinationIsPiece(Piece destinationPiece) {
        if (Objects.nonNull(destinationPiece)) {
            lostScoreByTeam.put(destinationPiece.getTeam()
                    , lostScoreByTeam.get(destinationPiece.getTeam()) + destinationPiece.getScore());
        }
    }

    private void exitWhenPieceIsKing(Piece destinationPiece) {
        if(destinationPiece instanceof King){
            System.exit(0);
        }
    }

    private void movePieceToPosition(Piece targetPiece, Position destination) {
        if (targetPiece instanceof Pawn && destination.isDeadLine()) {
            targetPiece = new Queen(targetPiece.getTeam());
        }
        board.put(destination, targetPiece);
    }

    private void clearPosition(Position target) {
        board.put(target, null);
    }

    private void checkMovable(List<Position> targetMovablePositions, Position destination) {
        if (targetMovablePositions.contains(destination)) {
            return;
        }
        throw new UnsupportedOperationException("이동 불가능한 좌표입니다.");
    }

    public double calculateScore(Team team) {
        double defaultScore = TOTAL_SCORE - lostScoreByTeam.get(team);
        return defaultScore - countOfSameLinePawn(team) * PAWN_SAME_HORIZONTAL_SCORE;
    }

    private int countOfSameLinePawn(Team team) {
        int result = 0;
        for (Horizontal horizontal : Horizontal.values()) {
            int cnt = calculateSameLinePawnCount(team, horizontal);
            result = plusCountWhenSameLinePawnIsMoreThanTwo(result, cnt);
        }
        return result;
    }

    private int plusCountWhenSameLinePawnIsMoreThanTwo(int result, int cnt) {
        if (cnt >= 2) {
            result += cnt;
        }
        return result;
    }

    private int calculateSameLinePawnCount(Team team, Horizontal horizontal) {
        int cnt = 0;
        for (Map.Entry<Position, Piece> entry : board.entrySet()) {
            Position position = entry.getKey();
            Piece piece = entry.getValue();
            cnt = calculateEachHorizontalTotalPawnCount(team, horizontal, cnt, position, piece);
        }
        return cnt;
    }

    private int calculateEachHorizontalTotalPawnCount(Team team, Horizontal horizontal, int cnt, Position position, Piece piece) {
        if (horizontal.isSameHorizontal(position) && piece instanceof Pawn && piece.isSameTeam(team)) {
            cnt++;
        }
        return cnt;
    }

    public Map<Position, Piece> getBoard() {
        return Collections.unmodifiableMap(board);
    }
}
