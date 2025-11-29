package uk.ac.ed.acp.cw2.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;

import java.util.*;
import java.awt.geom.Path2D;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.Line2D;

public class FlightPathAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(FlightPathAlgorithm.class);

    // Single step size used for grid snapping and neighbor expansion
    private static final double STEP = 1.5E-4;

    public static List<LngLat> findPath(LngLat start, LngLat goal, List<RestrictedArea> restrictedAreas) {


        // A simple grid-based A* implementation: snap coordinates to step grid, search 16-direction neighbors.
        //https://www.geeksforgeeks.org/dsa/a-search-algorithm/
        if (start == null || goal == null) return List.of();

        // quick check: if start and goal are effectively the same point, return direct
        if (Math.hypot(start.getLng() - goal.getLng(), start.getLat() - goal.getLat()) <= STEP) {
            return List.of(start, goal);
        }

        // Precompute buffered Areas for restricted areas once per call
        final List<Area> bufferedAreas = buildBufferedAreas(restrictedAreas);

        // default segments for straight-line fallback
        int segments = (int) (Distance.calculateEuclideanDistance(start, goal) / STEP);
        if (segments < 1) segments = 1;

        // If there are no restricted areas, just do a straight line
        if (bufferedAreas.isEmpty()) {
            return getStraightlinePath(start, goal, segments);
        }

        Node startNode = Node.fromLngLat(start);
        Node goalNode = Node.fromLngLat(goal);

        // Sample along the straight line to see if it intersects any buffered area.
        boolean intersects = false;
        int samples = segments;
        Node prev = startNode;
        for (int s = 1; s <= samples; s++) {
            double t = (double) s / samples;
            double ix = start.getLng() + (goal.getLng() - start.getLng()) * t;
            double iy = start.getLat() + (goal.getLat() - start.getLat()) * t;
            Node current = new Node(ix, iy);

            if (isInsideAnyArea(current, bufferedAreas) ||
                isSegmentIntersectAnyArea(prev, current, bufferedAreas)) {
                intersects = true;
                break;
            }
            prev = current;
        }
        if (!intersects) {
            return getStraightlinePath(start, goal, segments);
        }

        // Define a simple search bounding box around start/goal and restricted areas to keep search finite
        double minX = Math.min(start.getLng(), goal.getLng());
        double maxX = Math.max(start.getLng(), goal.getLng());
        double minY = Math.min(start.getLat(), goal.getLat());
        double maxY = Math.max(start.getLat(), goal.getLat());
        if (restrictedAreas != null) {
            for (RestrictedArea ra : restrictedAreas) {
                if (ra == null || ra.getVertices() == null) continue;
                for (int i = 0; i < ra.getVertices().length; i++) {
                    RestrictedArea.vertices v = ra.getVertices()[i];
                    if (v == null) continue;
                    minX = Math.min(minX, v.getLng());
                    maxX = Math.max(maxX, v.getLng());
                    minY = Math.min(minY, v.getLat());
                    maxY = Math.max(maxY, v.getLat());
                }
            }
        }
        // add padding of 20 steps around all relevant coordinates
        double padding = 20 * STEP;
        minX -= padding;
        maxX += padding;
        minY -= padding;
        maxY += padding;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Double> gScore = new HashMap<>();
        Set<Node> closed = new HashSet<>();

        open.add(startNode);
        gScore.put(startNode, 0.0);

        int maxIterations = 200000;
        int iterations = 0;

        while (!open.isEmpty() && iterations++ < maxIterations) {
            Node current = open.poll();

            // consider goal reached if the current node equals the goal node (grid equality)
            // OR if it is within one step of the goal
            if (current.equals(goalNode) || current.distanceTo(goalNode) <= STEP) {
                List<LngLat> path = reconstructPath(cameFrom, current);
                // ensure the exact goal coordinates are the final point in the path
                if (path.isEmpty() ||
                        Math.hypot(path.get(path.size() - 1).getLng() - goal.getLng(),
                                path.get(path.size() - 1).getLat() - goal.getLat()) > 0.0) {
                    path.add(goal);
                }
                return path;
            }

            closed.add(current);

            for (Node neighbor : current.neighbors()) {
                // enforce bounding box
                if (neighbor.x < minX || neighbor.x > maxX || neighbor.y < minY || neighbor.y > maxY) {
                    continue;
                }

                if (closed.contains(neighbor)) continue;

                // collision checks: skip neighbors that collide with any buffered Area
                if (isInsideAnyArea(neighbor, bufferedAreas) ||
                    isSegmentIntersectAnyArea(current, neighbor, bufferedAreas)) {
                    continue;
                }

                double tentativeG = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + current.distanceTo(neighbor);
                double existingG = gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY);
                if (tentativeG < existingG) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    neighbor.g = tentativeG;
                    neighbor.h = neighbor.distanceTo(goalNode);
                    neighbor.f = neighbor.g + neighbor.h;
                    // add to open if not present (or re-add to update priority)
                    open.remove(neighbor);
                    open.add(neighbor);
                }
            }
        }

        logger.warn("A* search failed to find a path within {} iterations", maxIterations);
        return List.of();
    }

    public static List<LngLat> getStraightlinePath(LngLat start, LngLat goal, int segments) {
        List<LngLat> pts = new ArrayList<>(segments + 1);
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / (double) segments;
            double ix = start.getLng() + (goal.getLng() - start.getLng()) * t;
            double iy = start.getLat() + (goal.getLat() - start.getLat()) * t;
            pts.add(LngLat.builder().lng(ix).lat(iy).build());
        }
        return pts;
    }

    private static List<LngLat> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        LinkedList<LngLat> path = new LinkedList<>();
        Node c = current;
        while (c != null) {
            path.addFirst(LngLat.builder().lng(c.x).lat(c.y).build());
            c = cameFrom.get(c);
        }
        return path;
    }

    // Build buffered Areas once per call to avoid repeated Path2D/Area creation
    private static List<Area> buildBufferedAreas(List<RestrictedArea> restrictedAreas) {
        List<Area> areas = new ArrayList<>();
        if (restrictedAreas == null) return areas;
        for (RestrictedArea ra : restrictedAreas) {
            if (ra == null || ra.getVertices() == null || ra.getVertices().length == 0) continue;
            Path2D.Double poly = new Path2D.Double();
            poly.moveTo(ra.getVertices()[0].getLng(), ra.getVertices()[0].getLat());
            for (int i = 1; i < ra.getVertices().length; i++) {
                poly.lineTo(ra.getVertices()[i].getLng(), ra.getVertices()[i].getLat());
            }
            poly.closePath();

            // Using BasicStroke to add a buffer the size of a step around the polygon like how the width of a pen
            // will give a slight buffer to a shape
            Area area = new Area(poly);
            float strokeWidth = (float) STEP;
            BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            Shape stroked = stroke.createStrokedShape(poly);
            area.add(new Area(stroked));
            areas.add(area);
        }
        return areas;
    }

    private static boolean isInsideAnyArea(Node p, List<Area> areas) {
        if (areas == null || areas.isEmpty()) return false;
        for (Area a : areas) {
            if (a != null && a.contains(p.x, p.y)) return true;
        }
        return false;
    }

    // check whether the straight line segment between two nodes intersects any buffered restricted Area
    private static boolean isSegmentIntersectAnyArea(Node a, Node b, List<Area> areas) {
        if (areas == null || areas.isEmpty()) return false;
        Line2D.Double line = new Line2D.Double(a.x, a.y, b.x, b.y);
        float strokeWidth = (float) STEP;
        BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Shape strokedLine = stroke.createStrokedShape(line);
        Area lineArea = new Area(strokedLine);

        for (Area area : areas) {
            if (area == null) continue;
            Area copy = new Area(area);
            copy.intersect(lineArea);
            if (!copy.isEmpty()) return true;
        }
        return false;
    }

    private static class Node {
        final double x; // snapped lng
        final double y; // snapped lat
        final int gx;   // grid x index
        final int gy;   // grid y index
        double g = 0;
        double h = 0;
        double f = 0;

        Node(double x, double y) {
            // snap to grid so equals/hashCode work reliably
            int gridX = (int) Math.round(x / STEP);
            int gridY = (int) Math.round(y / STEP);
            this.gx = gridX;
            this.gy = gridY;
            this.x = gridX * STEP;
            this.y = gridY * STEP;
        }

        static Node fromLngLat(LngLat p) {
            return new Node(p.getLng(), p.getLat());
        }

        List<Node> neighbors() {
            // Use 16 compass directions (multiples of 22.5 degrees)
            List<Node> n = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                double angle = Math.toRadians(i * 22.5);
                double dx = Math.cos(angle) * STEP;
                double dy = Math.sin(angle) * STEP;
                n.add(new Node(x + dx, y + dy));
            }
            return n;
        }

        double distanceTo(Node o) {
            double dx = this.x - o.x;
            double dy = this.y - o.y;
            return Math.hypot(dx, dy);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;
            return gx == node.gx && gy == node.gy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(gx, gy);
        }
    }
}

