package uk.ac.ed.acp.cw2.data;

import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;

import java.util.*;
import java.awt.geom.Path2D;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.Shape;

public class FlightPathAlgorithm {
    public static List<LngLat> findPath(LngLat start, LngLat goal, List<RestrictedArea> restrictedAreas) {

        // A simple grid-based A* implementation: snap coordinates to step grid, search 16-direction neighbors.
        //https://www.geeksforgeeks.org/dsa/a-search-algorithm/
        if (start == null || goal == null) return List.of();

        // quick check: if start and goal are effectively the same point, return direct
        if (Math.hypot(start.getLng() - goal.getLng(), start.getLat() - goal.getLat()) <= 0.00015) {
            return List.of(start, goal);
        }

        // Precompute buffered Areas for restricted areas once per call
        final List<Area> bufferedAreas = buildBufferedAreas(restrictedAreas);

        // if there are no restricted areas, return a straight-line path
        // with multiple segments so flight paths show steps instead of a single long move.
        int segments = 4; // default segments for straight-line fallback
        if (bufferedAreas.isEmpty()) {
            List<LngLat> pts = new ArrayList<>(segments + 1);
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / (double) segments;
                double ix = start.getLng() + (goal.getLng() - start.getLng()) * t;
                double iy = start.getLat() + (goal.getLat() - start.getLat()) * t;
                pts.add(LngLat.builder().lng(ix).lat(iy).build());
            }
            return pts;
        }

        // Otherwise (there are restricted areas), sample along the straight line to see if it intersects any buffered area.
        boolean intersects = false;
        int samples = 8; // sample 8 intermediate points
        for (int s = 1; s < samples; s++) {
            double t = (double) s / samples;
            double ix = start.getLng() + (goal.getLng() - start.getLng()) * t;
            double iy = start.getLat() + (goal.getLat() - start.getLat()) * t;
            Node in = new Node(ix, iy);
            if (isInsideAnyArea(in, bufferedAreas)) {
                intersects = true;
                break;
            }
        }
        if (!intersects) {
            List<LngLat> pts = new ArrayList<>(segments + 1);
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / (double) segments;
                double ix = start.getLng() + (goal.getLng() - start.getLng()) * t;
                double iy = start.getLat() + (goal.getLat() - start.getLat()) * t;
                pts.add(LngLat.builder().lng(ix).lat(iy).build());
            }
            return pts;
        }

        Node startNode = Node.fromLngLat(start);
        Node goalNode = Node.fromLngLat(goal);

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Double> gScore = new HashMap<>();

        open.add(startNode);
        gScore.put(startNode, 0.0);

        Set<Node> closed = new HashSet<>();

        int maxIterations = 20000;
        int iterations = 0;

        while (!open.isEmpty() && iterations++ < maxIterations) {
            Node current = open.poll();
            if (current.equals(goalNode)) {
                return reconstructPath(cameFrom, current);
            }

             closed.add(current);

             for (Node neighbor : current.neighbors()) {
                 if (closed.contains(neighbor)) continue;
                 // check collision: if the neighbor point is inside any buffered Area, skip
                 if (isInsideAnyArea(neighbor, bufferedAreas)) continue;

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
         return List.of();
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
            Path2D.Double poly = new Path2D.Double();
            poly.moveTo(ra.getVertices()[0].getLng(), ra.getVertices()[0].getLat());
            for (int i = 1; i < ra.getVertices().length; i++) {
                poly.lineTo(ra.getVertices()[i].getLng(), ra.getVertices()[i].getLat());
            }
            poly.closePath();

            // Using BasicStroke to add a buffer half the size of a step around the polygon like how the width of a pen
            // will give a slight buffer to a shape
            Area area = new Area(poly);
            float strokeWidth = (float) (0.00015);
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

     private static class Node {
         final double x; // lng
         final double y; // lat
         double g = 0;
         double h = 0;
         double f = 0;

         Node(double x, double y) {
             this.x = x;
             this.y = y;
         }

         static Node fromLngLat(LngLat p) {
             double gx = Math.round(p.getLng() / 1.5E-4) * 1.5E-4;
             double gy = Math.round(p.getLat() / 1.5E-4) * 1.5E-4;
             return new Node(gx, gy);
         }

         List<Node> neighbors() {
             // Use 16 compass directions (multiples of 22.5 degrees)
             List<Node> n = new ArrayList<>(16);
             for (int i = 0; i < 16; i++) {
                 double angle = Math.toRadians(i * 22.5);
                 double dx = Math.cos(angle) * 1.5E-4;
                 double dy = Math.sin(angle) * 1.5E-4;
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
             return Double.compare(node.x, x) == 0 && Double.compare(node.y, y) == 0;
         }

         @Override
         public int hashCode() {
             return Objects.hash(x, y);
         }
     }
 }
