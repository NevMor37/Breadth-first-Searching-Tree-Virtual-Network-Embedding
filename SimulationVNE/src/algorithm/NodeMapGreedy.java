package globecom16;

// Define Greedy Node Mapping with the explanations 
// in Ayoubi 2015 Paper(MINTED) and Viet ICCSA 2013
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeMapGreedy {
    private static String virtual;
    private static String substrate;
    private static double totalDistance = 0;
    private static List<String> multicastTree;

    private static double totalPathDistance = 0;
    
    public NodeMapGreedy(String sFile, String vFile){
        this.substrate = sFile;
        this.virtual = vFile;
    }

    public boolean mapping(List<Vertex> substrate, List<Vertex> virtual) {

        Vertex[] mappedNodes = new Vertex[substrate.size()];
        showLists(substrate, virtual);

        mappedNodes = nodeMapping(substrate, virtual, mappedNodes);
        
        if (mappedNodes==null) {
            return false;
        }
        
        List<Vertex> totalPath = linkMapping(substrate, virtual, mappedNodes);
        
        if (totalPath.isEmpty()) {
            return false;
        }
        
        totalPath = updatePath(totalPath, substrate);


        System.out.println("===============================================");
        System.out.println("Total path:" + Arrays.toString(totalPath.toArray()));
        System.out.println("Total transmission distance:" + totalPathDistance);
        totalPathDistance = updateCost(totalPath);
        System.out.println("Total transmission distance After link update:" + totalPathDistance);
        
        return true;

    }

    private double updateCost(List<Vertex> totalPath) {

        double weight = 0;

        for (int i = 0; i < totalPath.size() - 1; i += 2) {
            List<Edge> links = new ArrayList<Edge>();
            links = totalPath.get(i).adjacencies;
            for (Edge edge : links) {
                if (edge.target.name == totalPath.get(i + 1).name) {
                    weight += edge.weight;
                }
//				System.out.println("Total weight="+weight);
            }
//			System.out.println("Total weight="+weight);
        }


        return weight;
    }

    private List<Vertex> updatePath(List<Vertex> totalPath, List<Vertex> substrate) {

        List<String> link = new ArrayList<String>();
        for (int i = 0; i < totalPath.size() - 1; i++) {
            if (totalPath.get(i) != null && totalPath.get(i + 1) != null) {
                link.add(totalPath.get(i) + "-" + totalPath.get(i + 1));
            }
        }

        for (int i = 0; i < link.size() - 1; i++) {
            if (link.get(i).equalsIgnoreCase(reverse(link.get(i + 1)))) {
                link.remove(i);
                i--;
            }
        }

        // System.out.println("Links:"+Arrays.toString(link.toArray()));
        multicastTree = link;
        List<Vertex> tempNodeList = new ArrayList<Vertex>();
        for (String string : link) {

            tempNodeList.add(substrate.get(Integer.valueOf(string.substring(0, 1))));
            tempNodeList.add(substrate.get(Integer.valueOf(string.substring(string.length() - 1, string.length()))));
        }

        // System.out.println("Last node
        // list:"+Arrays.toString(tempNodeList.toArray()));

        return tempNodeList;
    }

    private String reverse(String string) {
        String temp = "";
        for (int i = 0; i < string.length(); i++) {
            temp += string.charAt(string.length() - 1 - i);
        }
        // System.out.println("Coming String:"+string);
        // System.out.println("Reverse of it:"+temp);
        return temp;
    }

    private Vertex[] nodeMapping(List<Vertex> substrate, List<Vertex> virtual, Vertex[] mappedNodes) {

        while (virtual.size() != 0) {

            Vertex node = virtual.get(0);
            CandidateSet cSet = new CandidateSet();
//            List<Vertex> candidateNodes = CandidateSet.getPossibleNodes(CandidateSet.getCandidateList(substrate, node),node);
            List<Vertex> candidateNodes = cSet.getPossibleNodes(cSet.getCandidateList(substrate, node),node);
            node = findBestNode(candidateNodes, mappedNodes);
            System.out.println("Virtual node " + virtual.get(0) + " is mapped onto Substrate Node:" + node);
            if (node != null) {
                mappedNodes[node.name] = virtual.get(0);
            } else {
                System.out.println("There is no available node to map virtual node " + virtual.get(0));
                return null;
            }
            System.out.println("Mapped Nodes:" + Arrays.toString(mappedNodes));
            virtual.remove(0);
        }

        return mappedNodes;
    }

    private List<Vertex> linkMapping(List<Vertex> substrate, List<Vertex> virtual, Vertex[] mapped) {

        List<Vertex> totalPath = new ArrayList<Vertex>();
        List<Vertex> tempPath = new ArrayList<Vertex>();
        List<Vertex> mappedNodes = new ArrayList<Vertex>();
        List<Vertex> IntermediateNodes = new ArrayList<Vertex>();

        for (int i = 0; i < mapped.length; i++) {
            if (mapped[i] != null) {
                mappedNodes.add(substrate.get(i));
            }
        }


        System.out.println("Mapped substrate nodes:" + Arrays.toString(mappedNodes.toArray()));

        IntermediateNodes.addAll(mappedNodes);

        System.out.println("Intermediate Nodes:" + Arrays.toString(IntermediateNodes.toArray()));

        for (int i = 0; i < mappedNodes.size(); i++) {

            int maxBW = findMaxBW(mapped[mappedNodes.get(i).name]);
            Vertex node = mappedNodes.get(i);
            tempPath = findShortestPath(node, IntermediateNodes, maxBW);
//			tempPath = findShortestPath(node, mappedNodes, maxBW);
            
            if (tempPath.isEmpty()) {
                return null;
            }
            totalPath.addAll(tempPath);
            for (Vertex V : tempPath) {
                int found = 0;
                for (Vertex I : IntermediateNodes) {
                    if (I.name == V.name) {
                        found = 1;
                        break;
                    }
                }

                if (found == 0) {
                    System.out.println("Adding New Intermediate Vertex" + V.name);
                    IntermediateNodes.add(V);
                }
            }
            System.out.println("Updated Intermediate Nodes " + Arrays.toString(IntermediateNodes.toArray()));
            System.out.println("Total Path:" + Arrays.toString(totalPath.toArray()));
            totalPath.add(null);
        }

        return totalPath;
    }

    private Vertex findBestNode(List<Vertex> candidateNodes, Vertex[] mapped) {

        Vertex[] allNodes = new Vertex[candidateNodes.size()];

        for (int i = 0; i < candidateNodes.size(); i++) {
            allNodes[i] = candidateNodes.get(i);
        }

        Sort.bubbleSort(allNodes);

        System.out.println("All nodes were ordered by CPU+BW+Degree");
        System.out.println(Arrays.toString(allNodes));

        for (int i = 0; i < allNodes.length; i++) {
            System.out.println("Node " + allNodes[i]);
            System.out.println("Mapped[" + allNodes[i].name + "]=" + mapped[allNodes[i].name]);

            if (mapped[allNodes[i].name] == null) {
                return allNodes[i];
            }

        }

        return null;
    }

    private void showTotalPath(List<String> totalPath) {

        for (String path : totalPath) {
            System.out.println(path);
        }

    }

    private List<Vertex> findShortestPath(Vertex closestVertex, List<Vertex> mappedList, int maxBW) {

        List<Vertex> path = new ArrayList<Vertex>();
        double minDistance = Double.POSITIVE_INFINITY;
        List<Vertex> savedPath = new ArrayList<Vertex>();

        for (Vertex vertex : mappedList) {

            if (closestVertex.name != vertex.name) {

                ShortestPath pathObj = new ShortestPath(substrate);
                path = pathObj.defineShortestPath(closestVertex.name, vertex.name, maxBW);
                System.out.println("Path:" + Arrays.toString(path.toArray()));
                System.out.println("Path from " + closestVertex + " to " + vertex + " with BW=" + maxBW);

                double distance = pathObj.totalDistance(path);
                System.out.println("Distance:" + distance);
                if (distance < minDistance) {
                    minDistance = distance;
                    savedPath = path;
                    System.out.println("Saved Path:==>" + Arrays.toString(path.toArray()));
                    System.out.println("Minimum Distance:" + minDistance);
                }
            } // end-if
        } // end-for mappedList

        totalPathDistance += minDistance;
        
        return savedPath;
    }

    private void showLists(List<Vertex> substrate, List<Vertex> virtual) {
        System.out.println("Substrate Vertex List:" + Arrays.toString(substrate.toArray()));
        System.out.println("Virtual Vertex List:" + Arrays.toString(virtual.toArray()));
    }

    private List<Vertex> checkCandidateSet(List<Vertex> list, List<Vertex> mappedList, Boolean[] mapped) {

        List<Vertex> updateList = new ArrayList<Vertex>();

        for (Vertex vertex : list) {
            Boolean status = false;

            for (int i = 0; i < mapped.length; i++) {
                if (mapped[i] != null && vertex.name == i) {
                    status = true;
                }
            }

            if (!status) {
                updateList.add(vertex);
            }

        }

        return updateList;
    }

    private int findMaxBW(Vertex vertex) {

        int max = Integer.MIN_VALUE;
        for (Edge link : vertex.adjacencies) {

            System.out
                    .println("Edge from " + vertex.toString() + " to " + link.target.toString() + "=" + link.bandWidth);
            if (link.bandWidth >= max) {

                max = link.bandWidth;
            }

        }

        return max;
    }

    public boolean map(int source, int[] dest) {

        multicastTree = new ArrayList<String>();
        GenerateVertex gVertex = new GenerateVertex();
        List<Vertex> substrate = gVertex.setVertices(this.substrate);
        List<Vertex> virtual = gVertex.setVertices(this.virtual);

        List<Vertex> mappedList = new ArrayList<Vertex>();
        List<Vertex> totalPath = new ArrayList<Vertex>();
        Vertex[] mappedNodes = new Vertex[substrate.size()];
        System.out.println("Virtual List:" + Arrays.toString(virtual.toArray()));

        List<Vertex> updatedList = new ArrayList<Vertex>();
        Vertex[] virtualNodes = new Vertex[virtual.size()];

        for (int i = 0; i < virtual.size(); i++) {
            virtualNodes[i] = virtual.get(i);
        }

        System.out.println("Virtual Node Array:" + Arrays.toString(virtualNodes));

        Sort.bubbleSort(virtualNodes);

        System.out.println("Sorted Virtual Node Array:" + Arrays.toString(virtualNodes));

        for (Vertex vertex : virtualNodes) {
            updatedList.add(vertex);
        }

        System.out.println("Virtual Priority List:" + Arrays.toString(updatedList.toArray()));

        return mapping(substrate, updatedList);

    }
    
    public double getTotalDistance() {
        double distance = totalPathDistance;
        totalPathDistance = 0;
        return distance;
    }

    public List<String> getMulticastTree() {
        return multicastTree;
    }
}
