package fr.themode.minestom.entity.pathfinding;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

import java.util.ArrayList;

public class JPS {

    private Instance instance;
    private Position startPosition;
    private Position endPosition;

    private Node startNode;
    private Node endNode;

    private boolean pathFound = false;
    private ArrayList<Node> checkedNodes = new ArrayList<>();
    private ArrayList<Node> uncheckedNodes = new ArrayList<>();

    private int maxNodeTests;
    private boolean canClimbLadders;
    private double maxFallDistance;

    // ---
    // CONSTRUCTORS
    // ---

    public JPS(Instance instance, Position start, Position end, int maxNodeTests, boolean canClimbLadders, double maxFallDistance) {
        this.instance = instance;
        this.startPosition = start;
        this.endPosition = end;

        startNode = new Node(startPosition, 0, null);
        endNode = new Node(endPosition, 0, null);

        this.maxNodeTests = maxNodeTests;
        this.canClimbLadders = canClimbLadders;
        this.maxFallDistance = maxFallDistance;
    }

    public JPS(Instance instance, Position start, Position end) {
        this(instance, start, end, 1000, false, 1);
    }

    // ---
    // PATHFINDING
    // ---

    public Position[] getPath() {
        // check if player could stand at start and endpoint, if not return empty path
        if (!(canStandAt(startPosition) && canStandAt(endPosition)))
            return new Position[0];

        // time for benchmark
        long nsStart = System.nanoTime();

        uncheckedNodes.add(startNode);

        // cycle through untested nodes until a exit condition is fulfilled
        while (checkedNodes.size() < maxNodeTests && pathFound == false && uncheckedNodes.size() > 0) {
            Node n = uncheckedNodes.get(0);
            for (Node nt : uncheckedNodes)
                if (nt.getEstimatedFinalExpense() < n.getEstimatedFinalExpense())
                    n = nt;

            if (n.estimatedExpenseLeft < 1) {
                pathFound = true;
                endNode = n;

                // print information about last node
                //Bukkit.broadcastMessage(uncheckedNodes.size() + "uc " + checkedNodes.size() + "c " + round(n.expense) + "cne " + round(n.getEstimatedFinalExpense()) + "cnee ");

                break;
            }

            n.getReachablePositions();
            uncheckedNodes.remove(n);
            checkedNodes.add(n);
        }

        // returning if no path has been found
        if (!pathFound) {
            float duration = (System.nanoTime() - nsStart) / 1000000f;
            //System.out.println("TOOK " + duration + " ms not found!");

            return new Position[0];
        }

        // get length of path to create array, 1 because of start
        int length = 1;
        Node n = endNode;
        while (n.origin != null) {
            n = n.origin;
            length++;
        }

        Position[] Positions = new Position[length];

        //fill Array
        n = endNode;
        for (int i = length - 1; i > 0; i--) {
            Positions[i] = n.getPosition();
            n = n.origin;
        }

        Positions[0] = startNode.getPosition();

        // outputting benchmark result
        float duration = (System.nanoTime() - nsStart) / 1000000f;
        //System.out.println("TOOK " + duration + " ms!");

        return Positions;
    }

    private Node getNode(Position loc) {
        Node test = new Node(loc, 0, null);

        for (Node n : checkedNodes)
            if (n.x == test.x && n.y == test.y && n.z == test.z)
                return n;

        return test;
    }

    // ---
    // NODE
    // ---

    public boolean isObstructed(Position loc) {
        //if(loc.getBlock().getType().isSolid())
        //return true;
        short blockId = instance.getBlockId(loc.toBlockPosition());
        return blockId != 0;
    }

    // ---
    // CHECKS
    // ---

    public boolean canStandAt(Position loc) {
        return !(isObstructed(loc) || isObstructed(loc.clone().add(0, 1, 0)) || !isObstructed(loc.clone().add(0, -1, 0)));
    }

    public double distanceTo(Position loc1, Position loc2) {
        double deltaX = Math.abs(loc1.getX() - loc2.getX());
        double deltaY = Math.abs(loc1.getY() - loc2.getY());
        double deltaZ = Math.abs(loc1.getZ() - loc2.getZ());

        // euclidean distance
        double distance2d = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double distance3d = Math.sqrt(distance2d * distance2d + deltaY * deltaY);

        return distance3d;

        // manhattan distance
        //return deltaX + deltaY + deltaZ;
    }

    // ---
    // UTIL
    // ---

    public double round(double d) {
        return ((int) (d * 100)) / 100d;
    }

    public class Node {
        public int x;
        public int y;
        public int z;
        public Node origin;
        public double expense;
        private Position position;
        private BlockPosition blockPosition;
        private double estimatedExpenseLeft = -1;

        // ---
        // CONSTRUCTORS
        // ---

        public Node(Position loc, double expense, Node origin) {
            position = loc;
            blockPosition = loc.toBlockPosition();
            x = blockPosition.getX();
            y = blockPosition.getY();
            z = blockPosition.getZ();

            this.origin = origin;

            this.expense = expense;
        }

        // ---
        // GETTERS
        // ---

        public Position getPosition() {
            return position;
        }

        public double getEstimatedFinalExpense() {
            if (estimatedExpenseLeft == -1)
                estimatedExpenseLeft = distanceTo(position, endPosition);

            return expense + 1.1 * estimatedExpenseLeft;
        }

        // ---
        // PATHFINDING
        // ---

        public void getReachablePositions() {
            //trying to get all possibly walkable blocks
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    if (!(x == 0 && z == 0) && x * z == 0) {
                        Position loc = new Position(blockPosition.getX() + x, blockPosition.getY(), blockPosition.getZ() + z);

                        // usual unchanged y
                        if (canStandAt(loc))
                            reachNode(loc, expense + 1);

                        // one block up
                        if (!isObstructed(loc.clone().add(-x, 2, -z))) // block above current tile, thats why subtracting x and z
                        {
                            Position nLoc = loc.clone().add(0, 1, 0);
                            if (canStandAt(nLoc))
                                reachNode(nLoc, expense + 1.4142);
                        }

                        // one block down or falling multiple blocks down
                        if (!isObstructed(loc.clone().add(0, 1, 0))) // block above possible new tile
                        {
                            Position nLoc = loc.clone().add(0, -1, 0);
                            if (canStandAt(nLoc)) // one block down
                                reachNode(nLoc, expense + 1.4142);
                            else if (!isObstructed(nLoc) && !isObstructed(nLoc.clone().add(0, 1, 0))) // fall
                            {
                                int drop = 1;
                                while (drop <= maxFallDistance && !isObstructed(loc.clone().add(0, -drop, 0))) {
                                    Position locF = loc.clone().add(0, -drop, 0);
                                    if (canStandAt(locF)) {
                                        Node fallNode = addFallNode(loc, expense + 1);
                                        fallNode.reachNode(locF, expense + drop * 2);
                                    }

                                    drop++;
                                }
                            }
                        }

                        //ladder
                        /*if(canClimbLadders)
                            if(loc.clone().add(-x, 0, -z).getBlock().getType() == Material.LADDER)
                            {
                                Position nLoc = loc.clone().add(-x, 0, -z);
                                int up = 1;
                                while(nLoc.clone().add(0, up, 0).getBlock().getType() == Material.LADDER)
                                    up++;

                                reachNode(nLoc.clone().add(0, up, 0), expense + up * 2);
                            }*/
                    }
        }

        public void reachNode(Position locThere, double expenseThere) {
            Node nt = getNode(locThere);

            if (nt.origin == null && nt != startNode) // new node
            {
                nt.expense = expenseThere;
                nt.origin = this;

                uncheckedNodes.add(nt);

                return;
            }

            // no new node
            if (nt.expense > expenseThere) // this way is faster to go there
            {
                nt.expense = expenseThere;
                nt.origin = this;
            }
        }

        public Node addFallNode(Position loc, double expense) {
            Node n = new Node(loc, expense, this);

            return n;
        }

    }

}
