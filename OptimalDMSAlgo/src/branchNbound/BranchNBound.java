package branchNbound;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BranchNBound {

    private static TeamNode bestSolution = null; // Best solution node
    private static List<TeamNode> conflictNodes = new ArrayList<>(); // Knoten mit Konflikten für intelligentes
                                                                     // Backtracking
    private static int conflictIndex = 0; // Index des aktuellen Konflikt-Knotens

    /**
     * Löst das Schwimmer-Zuordnungsproblem mit Branch-and-Bound
     * mit intelligentem Backtracking zu Konflikt-Knoten.
     * Springt direkt zum ersten Knoten mit hasConflicts=true beim Backtracking.
     * 
     * @param root              Der initiale TeamNode (leeres LineUp)
     * @param initialLowerBound Initiale untere Schranke
     * @return Der beste gefundene TeamNode (vollständige Lösung)
     */
    public static TeamNode knapSackSolver(TeamNode root, int initialLowerBound) {

        TeamNode.lowerBound = initialLowerBound;
        bestSolution = null;
        conflictNodes.clear();
        conflictIndex = 0;

        // Stack für DFS (speichereffizient - nur einen Pfad im Speicher)
        Stack<TeamNode> stack = new Stack<>();
        stack.push(root);

        int nodesExplored = 0;
        int nodesPruned = 0;

        while (!stack.isEmpty()) {

            TeamNode current = stack.pop();
            // System.out.println(current.getEmptySpotsLeft());

            // System.out.println(current.getUpperBound());
            // Pruning: Wenn Upper Bound < Lower Bound, überspringen
            if (current.getUpperBound() < TeamNode.lowerBound) {
                nodesPruned++;
                // Beim Backtracking: Springe zum nächsten Konflikt-Knoten
                if (hasUnexploredConflicts()) {
                    TeamNode nextConflict = getNextConflictNode();
                    // System.out.println("hey");
                    if (nextConflict != null) {
                        nextConflict.fixConflicts();
                        stack.push(nextConflict);
                    }
                }
                continue;
            }

            // Prüfe ob Lösung vollständig ist
            if (current.isComplete()) {
                // Neue beste Lösung gefunden
                System.out.println(current.toStringLineUp());
                if (current.getTotalPoints() > TeamNode.lowerBound) {
                    TeamNode.lowerBound = current.getTotalPoints();
                    bestSolution = current;
                    System.out.println(current.toStringLineUp());
                    System.out.println("Bessere Lösung gefunden: " + TeamNode.lowerBound);
                }
                // Nach kompletter Lösung zum nächsten Konflikt-Knoten springen, falls vorhanden
                if (hasUnexploredConflicts()) {
                    TeamNode nextConflict = getNextConflictNode();
                    if (nextConflict != null) {
                        nextConflict.fixConflicts();
                        stack.push(nextConflict);
                    }
                }
            } else {
                // Generiere nächsten Team-Knoten
                TeamNode nextNode = current.nextTeamNode();

                nodesExplored++;

                if (nextNode != null) {
                    // Speichere Knoten mit Konflikten für intelligentes Backtracking
                    if (current.hasConflicts()) {
                        if (!conflictNodes.contains(current)) {
                            conflictNodes.add(current);
                        }
                    }

                    // Prüfe ob neuer Knoten gepruned wird
                    if (nextNode.getUpperBound() >= TeamNode.lowerBound) {
                        stack.push(nextNode);
                    } else {
                        nodesPruned++;
                        // Beim Pruning: Springe zum nächsten Konflikt-Knoten
                        if (hasUnexploredConflicts()) {
                            TeamNode nextConflict = getNextConflictNode();
                            if (nextConflict != null) {
                                nextConflict.fixConflicts();
                                stack.push(nextConflict);
                            }
                        }
                    }
                } else {
                    // nextTeamNode() hat null zurückgegeben - Backtracking nötig
                    // Springe zum nächsten Konflikt-Knoten
                    if (hasUnexploredConflicts()) {
                        TeamNode nextConflict = getNextConflictNode();
                        if (nextConflict != null) {
                            nextConflict.fixConflicts();
                            stack.push(nextConflict);
                        }
                    }
                }
            }
        }

        System.out.println("Knoten exploriert: " + nodesExplored + ", Knoten gepruned: " + nodesPruned);
        System.out.println("Konflikt-Knoten analyzed: " + conflictNodes.size());

        return bestSolution;
    }

    /**
     * Prüft ob es noch unerforschte Konflikt-Knoten gibt
     */
    private static boolean hasUnexploredConflicts() {
        return conflictIndex < conflictNodes.size();
    }

    /**
     * Gibt den nächsten Konflikt-Knoten zurück und inkrementiert den Index
     */
    private static TeamNode getNextConflictNode() {
        if (hasUnexploredConflicts()) {
            TeamNode nextConflict = conflictNodes.get(conflictIndex);
            conflictIndex++;
            return nextConflict;
        }
        return null;
    }

}
