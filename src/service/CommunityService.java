package service;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import repository.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Graph algorithms used to count connected communities and determine the
 * community containing the longest simple path.
 * Exact longest simple path is exponential in the worst case.
 */
public class CommunityService {

    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;

    /**
     * Creates the graph analysis service.
     *
     * @param userRepository user repository
     * @param friendshipRepository friendship repository
     */
    public CommunityService(
            Repository<Long, User> userRepository,
            Repository<Tuple<Long, Long>, Friendship> friendshipRepository) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    private Map<Long, Set<Long>> buildGraph() {

        Map<Long, Set<Long>> graph = new HashMap<Long, Set<Long>>();

        for (User user : userRepository.findAll()) {
            graph.put(user.getId(), new HashSet<Long>());
        }

        for (Friendship friendship : friendshipRepository.findAll()) {
            Long first = friendship.getUserId1();
            Long second = friendship.getUserId2();

            if (graph.containsKey(first) && graph.containsKey(second)) {
                graph.get(first).add(second);
                graph.get(second).add(first);
            }
        }

        return graph;
    }

    private void dfs(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited,
            Set<Long> component) {

        visited.add(node);
        component.add(node);

        for (Long neighbor : graph.get(node)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, graph, visited, component);
            }
        }
    }

    private List<Set<Long>> getComponents(
            Map<Long, Set<Long>> graph) {

        List<Set<Long>> result = new ArrayList<Set<Long>>();
        Set<Long> visited = new HashSet<Long>();

        for (Long node : graph.keySet()) {
            if (!visited.contains(node)) {

                Set<Long> component = new HashSet<Long>();

                dfs(node, graph, visited, component);
                result.add(component);
            }
        }

        return result;
    }

    /**
     * Returns the number of connected components,
     * including isolated users.
     *
     * @return number of communities
     */
    public int getNumberOfCommunities() {
        return getComponents(buildGraph()).size();
    }

    private int longestPath(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited) {

        visited.add(node);

        int maxLength = 1;

        for (Long neighbor : graph.get(node)) {
            if (!visited.contains(neighbor)) {

                int length = 1 + longestPath(
                        neighbor, graph, visited
                );

                maxLength = Math.max(maxLength, length);
            }
        }

        visited.remove(node);
        return maxLength;
    }

    private int getLongestPathLength(
            Set<Long> component,
            Map<Long, Set<Long>> graph) {

        int maxLength = 0;

        for (Long node : component) {
            int length = longestPath(
                    node, graph, new HashSet<Long>()
            );

            maxLength = Math.max(maxLength, length);
        }

        return maxLength;
    }

    /**
     * Finds the community with the longest simple path.
     * Ties are resolved by the smallest user ID in the component.
     *
     * @return users in the selected community, sorted by ID
     */
    public List<User> getMostSociableCommunity() {

        Map<Long, Set<Long>> graph = buildGraph();
        List<Set<Long>> components = getComponents(graph);

        Set<Long> best = new HashSet<Long>();
        int bestLength = 0;
        Long bestMinId = null;

        for (Set<Long> component : components) {

            int length = getLongestPathLength(component, graph);
            Long minId = Collections.min(component);

            if (length > bestLength ||
                    (length == bestLength &&
                            (bestMinId == null || minId < bestMinId))) {

                bestLength = length;
                bestMinId = minId;
                best = component;
            }
        }

        List<Long> orderedIds = new ArrayList<Long>(best);
        Collections.sort(orderedIds);

        List<User> users = new ArrayList<User>();

        for (Long id : orderedIds) {
            Optional<User> user = userRepository.findOne(id);

            if (user.isPresent()) {
                users.add(user.get());
            }
        }

        return users;
    }
}
