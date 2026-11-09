package service;

import domain.Friendship;
import domain.Tuple;
import domain.User;
import repository.Repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Analyzes connected communities in an undirected friendship graph.
 */
public class CommunityService {

    private final Repository<Long, User> userRepository;

    private final Repository<Tuple<Long, Long>, Friendship>
            friendshipRepository;

    /**
     * Creates the community service.
     *
     * @param userRepository repository containing users
     * @param friendshipRepository repository containing friendships
     */
    public CommunityService(
            Repository<Long, User> userRepository,
            Repository<Tuple<Long, Long>, Friendship> friendshipRepository) {

        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    private Map<Long, Set<Long>> buildGraph() {

        List<User> users = userRepository.findAll();
        List<Friendship> friendships = friendshipRepository.findAll();

        Set<Long> existingIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return users.stream()
                .collect(Collectors.toMap(

                        User::getId,

                        user -> friendships.stream()
                                .filter(friendship ->
                                        friendship.getUserId1().equals(user.getId())
                                                || friendship.getUserId2().equals(user.getId()))

                                .map(friendship ->
                                        friendship.getUserId1().equals(user.getId())
                                                ? friendship.getUserId2()
                                                : friendship.getUserId1())

                                .filter(existingIds::contains)
                                .collect(Collectors.toSet())
                ));
    }

    private void dfs(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited) {

        if (!visited.add(node)) {
            return;
        }

        graph.getOrDefault(
                node, Collections.emptySet()
        ).stream().forEach(neighbor ->
                dfs(neighbor, graph, visited));
    }

    private Set<Long> connectedComponent(
            Long start,
            Map<Long, Set<Long>> graph) {

        Set<Long> visited = new HashSet<>();
        dfs(start, graph, visited);

        return visited;
    }

    private List<Set<Long>> getComponents(
            Map<Long, Set<Long>> graph) {

        return graph.keySet().stream()
                .map(id -> connectedComponent(id, graph))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calculates the number of connected communities.
     *
     * @return the number of communities
     */
    public int getNumberOfCommunities() {
        return getComponents(buildGraph()).size();
    }

    private int longestPath(
            Long node,
            Map<Long, Set<Long>> graph,
            Set<Long> visited) {

        Set<Long> pathVisited = new HashSet<>(visited);
        pathVisited.add(node);

        return 1 + graph.getOrDefault(
                        node, Collections.emptySet()
                ).stream()

                .filter(neighbor ->
                        !pathVisited.contains(neighbor))

                .mapToInt(neighbor ->
                        longestPath(
                                neighbor,
                                graph,
                                pathVisited
                        ))

                .max()
                .orElse(0);
    }

    private int longestPathLength(
            Set<Long> component,
            Map<Long, Set<Long>> graph) {

        return component.stream()
                .mapToInt(node ->
                        longestPath(
                                node,
                                graph,
                                Collections.emptySet()
                        ))
                .max()
                .orElse(0);
    }

    /**
     * Finds the community with the longest simple path.
     * Ties are resolved using the smallest user identifier.
     *
     * @return the users of the most sociable community, sorted by ID
     */
    public List<User> getMostSociableCommunity() {

        Map<Long, Set<Long>> graph = buildGraph();

        Comparator<Set<Long>> comparator =
                Comparator.<Set<Long>>comparingInt(
                                component ->
                                        longestPathLength(component, graph)
                        )
                        .thenComparing(
                                component -> component.stream()
                                        .min(Long::compareTo)
                                        .orElse(Long.MAX_VALUE),
                                Comparator.reverseOrder()
                        );

        Set<Long> bestComponent = getComponents(graph).stream()
                .max(comparator)
                .orElse(Collections.emptySet());

        return bestComponent.stream()
                .sorted()
                .map(id ->
                        userRepository.findOne(id)
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Missing user: " + id
                                        )
                                )
                )
                .collect(Collectors.toList());
    }
}
