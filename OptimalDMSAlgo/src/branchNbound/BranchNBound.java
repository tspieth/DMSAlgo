package branchNbound;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BranchNBound {

    private static TeamNode bestSolution = null; // Best solution node

    /**
     * Löst das Schwimmer-Zuordnungsproblem mit Branch-and-Bound
     * Nutzt DFS mit Stack (speichereffizient) + greedy Kind-Sortierung (gutes
     * Pruning)
     * 
     * @param root              Der initiale TeamNode (leeres LineUp)
     * @param initialLowerBound Initiale untere Schranke
     * @return Der beste gefundene TeamNode (vollständige Lösung)
     */
    public static TeamNode knapSackSolver(TeamNode root, int initialLowerBound) {

        TeamNode.lowerBound = initialLowerBound;
        bestSolution = null;

        // Stack für DFS (speichereffizient - nur einen Pfad im Speicher)
        Stack<TeamNode> stack = new Stack<>();
        stack.push(root);

        int nodesExplored = 0;
        int nodesPruned = 0;

        while (!stack.isEmpty()) {

            TeamNode current = stack.pop();

            // Pruning: Wenn Upper Bound < Lower Bound, überspringen
            if (current.getUpperBound() < TeamNode.lowerBound) {
                nodesPruned++;
                continue;
            }

            // Prüfe ob Lösung vollständig ist
            if (isComplete(current)) {
                // Neue beste Lösung gefunden
                if (current.getTotalPoints() > TeamNode.lowerBound) {
                    TeamNode.lowerBound = current.getTotalPoints();
                    bestSolution = current;
                    System.out.println(current.toStringLineUp());
                    System.out.println("Bessere Lösung gefunden: " + TeamNode.lowerBound);
                }
            } else {
                // Sammle ALLE möglichen Kindknoten
                List<TeamNode> children = new ArrayList<>();
                TeamNode child = current.nextChildNode();
                while (child != null) {
                    nodesExplored++;
                    children.add(child);
                    child = current.nextChildNode();
                }

                // Sortiere nach Upper Bound (höchste zuerst)
                // Dadurch werden gute Pfade zuerst erforscht → früher bessere Lösungen →
                // besseres Pruning
                children.sort((a, b) -> Integer.compare(b.getUpperBound(), a.getUpperBound()));

                // Pushe in umgekehrter Reihenfolge (damit beste zuerst gepoppt wird)
                for (int i = children.size() - 1; i >= 0; i--) {
                    if (children.get(i).getUpperBound() < TeamNode.lowerBound) {
                        nodesPruned++;
                    } else {
                        stack.push(children.get(i));
                    }
                }
            }
        }

        System.out.println("Knoten exploriert: " + nodesExplored + ", Knoten gepruned: " + nodesPruned);

        return bestSolution;
    }

    /**
     * Prüft ob ein TeamNode eine vollständige Lösung ist
     * (alle LineUp-Spots gefüllt)
     */
    private static boolean isComplete(TeamNode node) {
        // Vereinfachte Prüfung: getNextLineUpSpot() gibt -1 zurück wenn fertig
        return node.getNextLineUpSpot() < 0;
    }

}
