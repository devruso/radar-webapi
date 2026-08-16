package com.jangada.RADAR.utils;

import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.jangada.RADAR.models.entities.Horario;
import com.jangada.RADAR.models.entities.Turma;
import com.jangada.RADAR.utils.RecomendacaoUtil.RecomendacaoCriteria;

/** Builds a deterministic schedule with one class per component and no overlaps. */
public final class GradeOptimizer {

    private static final int BEAM_WIDTH = 5_000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private GradeOptimizer() {
    }

    public static List<RecomendacaoCriteria> optimize(
        List<RecomendacaoCriteria> rankedCandidates,
        int maximum,
        String method
    ) {
        if (maximum <= 0 || rankedCandidates == null || rankedCandidates.isEmpty()) {
            return List.of();
        }
        List<RankedCandidate> candidates = new ArrayList<>();
        for (int rank = 0; rank < rankedCandidates.size(); rank++) {
            final int currentRank = rank;
            RecomendacaoCriteria criteria = rankedCandidates.get(rank);
            Optional<List<Meeting>> meetings = parseMeetings(criteria.turma);
            // A grade cannot be declared conflict-free when its schedule is absent or malformed.
            meetings.ifPresent(value -> candidates.add(new RankedCandidate(criteria, value, currentRank)));
        }

        String normalizedMethod = method == null ? "guloso" : method.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedMethod) {
            case "burrinho", "guloso" -> greedy(candidates, maximum);
            case "busca" -> beamSearch(candidates, maximum);
            default -> throw new IllegalArgumentException(
                "Método inválido. Use 'guloso' (ou 'burrinho') ou 'busca'."
            );
        };
    }

    static Optional<List<Meeting>> parseMeetings(Turma turma) {
        Horario horario = turma == null ? null : turma.getHorario();
        if (horario == null || horario.getHorarios() == null || horario.getHorarios().isEmpty()) {
            return Optional.empty();
        }
        List<Meeting> meetings = new ArrayList<>();
        for (Map.Entry<String, String> entry : horario.getHorarios().entrySet()) {
            String day = normalizeDay(entry.getKey());
            String rawIntervals = entry.getValue();
            if (day == null || rawIntervals == null || rawIntervals.isBlank()) {
                return Optional.empty();
            }
            for (String rawInterval : rawIntervals.split("[,;]")) {
                String[] bounds = rawInterval.trim().split("\\s*[-–—]\\s*");
                if (bounds.length != 2) {
                    return Optional.empty();
                }
                try {
                    LocalTime start = LocalTime.parse(bounds[0], TIME_FORMAT);
                    LocalTime end = LocalTime.parse(bounds[1], TIME_FORMAT);
                    if (!start.isBefore(end)) {
                        return Optional.empty();
                    }
                    meetings.add(new Meeting(day, start, end));
                } catch (DateTimeParseException exception) {
                    return Optional.empty();
                }
            }
        }
        return meetings.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(meetings));
    }

    /** Returns whether an offering has a complete, parseable schedule. */
    public static boolean isValidSchedule(Horario horario) {
        if (horario == null) {
            return false;
        }
        return parseMeetings(Turma.builder().horario(horario).build()).isPresent();
    }

    private static List<RecomendacaoCriteria> greedy(List<RankedCandidate> candidates, int maximum) {
        State state = State.empty();
        for (RankedCandidate candidate : candidates) {
            if (state.selected().size() >= maximum) {
                break;
            }
            if (canAdd(state, candidate)) {
                state = state.add(candidate);
            }
        }
        return selectedCriteria(state);
    }

    private static List<RecomendacaoCriteria> beamSearch(List<RankedCandidate> candidates, int maximum) {
        List<State> states = List.of(State.empty());
        for (RankedCandidate candidate : candidates) {
            Map<String, State> next = new LinkedHashMap<>();
            for (State state : states) {
                keepBest(next, state);
                if (state.selected().size() < maximum && canAdd(state, candidate)) {
                    keepBest(next, state.add(candidate));
                }
            }
            states = next.values().stream()
                .sorted(GradeOptimizer::compareStates)
                .limit(BEAM_WIDTH)
                .toList();
        }
        State best = states.stream().min(GradeOptimizer::compareStates).orElse(State.empty());
        return selectedCriteria(best);
    }

    private static boolean canAdd(State state, RankedCandidate candidate) {
        Long componentId = candidate.criteria().turma.getComponenteCurricular().getId();
        if (state.componentIds().contains(componentId)) {
            return false;
        }
        return state.meetings().stream().noneMatch(existing ->
            candidate.meetings().stream().anyMatch(incoming -> conflicts(existing, incoming))
        );
    }

    private static boolean conflicts(Meeting first, Meeting second) {
        return first.day().equals(second.day())
            && first.start().isBefore(second.end())
            && second.start().isBefore(first.end());
    }

    private static void keepBest(Map<String, State> states, State candidate) {
        String signature = candidate.selected().stream()
            .map(item -> String.valueOf(item.criteria().turma.getId()))
            .sorted()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
        State current = states.get(signature);
        if (current == null || compareStates(candidate, current) < 0) {
            states.put(signature, candidate);
        }
    }

    private static int compareStates(State first, State second) {
        for (int priority = 1; priority <= 5; priority++) {
            int firstCount = priorityCount(first, priority);
            int secondCount = priorityCount(second, priority);
            int comparison = Integer.compare(secondCount, firstCount);
            if (comparison != 0) {
                return comparison;
            }
        }
        int size = Integer.compare(second.selected().size(), first.selected().size());
        if (size != 0) {
            return size;
        }
        int cost = Integer.compare(first.rankCost(), second.rankCost());
        if (cost != 0) {
            return cost;
        }
        return selectedIds(first).compareTo(selectedIds(second));
    }

    private static int priorityCount(State state, int priority) {
        return (int) state.selected().stream()
            .filter(item -> item.criteria().prioridadeMatricula.getOrdem() == priority)
            .count();
    }

    private static String selectedIds(State state) {
        return state.selected().stream()
            .map(item -> String.format("%020d", item.criteria().turma.getId()))
            .sorted()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
    }

    private static List<RecomendacaoCriteria> selectedCriteria(State state) {
        return state.selected().stream()
            .sorted(Comparator.comparingInt(RankedCandidate::rank))
            .map(RankedCandidate::criteria)
            .toList();
    }

    private static String normalizeDay(String rawDay) {
        if (rawDay == null) {
            return null;
        }
        String day = Normalizer.normalize(rawDay, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .trim()
            .toUpperCase(Locale.ROOT);
        if (day.length() >= 3) {
            day = day.substring(0, 3);
        }
        return Set.of("SEG", "TER", "QUA", "QUI", "SEX", "SAB").contains(day) ? day : null;
    }

    record Meeting(String day, LocalTime start, LocalTime end) {
    }

    private record RankedCandidate(
        RecomendacaoCriteria criteria,
        List<Meeting> meetings,
        int rank
    ) {
    }

    private record State(
        List<RankedCandidate> selected,
        Set<Long> componentIds,
        List<Meeting> meetings,
        int rankCost
    ) {
        static State empty() {
            return new State(List.of(), Set.of(), List.of(), 0);
        }

        State add(RankedCandidate candidate) {
            List<RankedCandidate> newSelected = new ArrayList<>(selected);
            newSelected.add(candidate);
            Set<Long> newComponentIds = new HashSet<>(componentIds);
            newComponentIds.add(candidate.criteria().turma.getComponenteCurricular().getId());
            List<Meeting> newMeetings = new ArrayList<>(meetings);
            newMeetings.addAll(candidate.meetings());
            return new State(
                List.copyOf(newSelected),
                Set.copyOf(newComponentIds),
                List.copyOf(newMeetings),
                rankCost + candidate.rank()
            );
        }
    }
}
