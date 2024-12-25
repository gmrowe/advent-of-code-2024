#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <assert.h>

#define BUFFER_SIZE (1024*32) // 32 kb


typedef struct Str
{
    char * data;
    size_t data_length;
} Str;

Str str_copy(const Str orig)
{
    return (Str) {
        .data = orig.data,
        .data_length = orig.data_length,
    };
}

Str str_split_line(Str *str)
{
    size_t index = 0;
    while ((str->data[index] != '\n') && (index < str->data_length))
    {
        index += 1;
    }
    Str line = { .data = str->data, .data_length = index};
    str->data = str->data + index + 1;
    str->data_length = str->data_length - index - 1;
    return line;
}

Str str_from_file(const char *file_path, char buffer[], const size_t buffer_length)
{
    FILE *fp = fopen(file_path, "rb");

    if (!fp)
    {
        fprintf(stderr, "[FATAL ERROR] Could not access file: `%s`\n", file_path);
        exit(1);
    }

    size_t bytes_read = fread(buffer, 1, buffer_length, fp);
    if (ferror(fp))
    {
        fprintf(stderr, "[FATAL ERROR] Error reading file: `%s`\n", file_path);
        fclose(fp);
        exit(1);
    }

    fclose(fp);

    return (Str) {
        .data = buffer,
        .data_length = bytes_read,
    };

}

void str_replace(Str str, size_t index, char replacement)
{
    assert(index < str.data_length);
    str.data[index] = replacement;
}

typedef enum MapElement
{
    ME_OPEN_UNVISITED,
    ME_OBSTRUCTED,
    ME_VISITED_EAST,
    ME_VISITED_WEST,
    ME_VISITED_NORTH,
    ME_VISITED_SOUTH,
    ME_VISITING_EAST,
    ME_VISITING_WEST,
    ME_VISITING_NORTH,
    ME_VISITING_SOUTH,
} MapElement;

typedef struct Grid {
    MapElement *map;
    size_t map_length;
    size_t stride;
    bool guard_is_present;
    bool cycle_detected;
} Grid;

Grid create_grid_from_input(Str input)
{
    MapElement *map = malloc(sizeof(MapElement) * input.data_length);
    size_t map_rows = 0;
    size_t map_length = 0;
    bool guard_is_present = false;
    while (input.data_length > 0)
    {
        Str line = str_split_line(&input);
        for (size_t i = 0; i < line.data_length; i++)
        {
            switch (line.data[i])
            {
            case '.': map[map_length] = ME_OPEN_UNVISITED; break;
            case '#': map[map_length] = ME_OBSTRUCTED; break;
            case '^': map[map_length] = ME_VISITING_NORTH;
                guard_is_present = true;
                break;
            default: fprintf(
                stderr,
                "[FATALERROR] parse_grid_from_input(Str): Unknown char `%c`\n",
                line.data[i]
                );
                free(map);
                exit(1);
            }
            map_length += 1;
        }
        map_rows += 1;
    }
    return (Grid) {
        .map = map,
        .stride = map_length / map_rows,
        .map_length = map_length,
        .guard_is_present = guard_is_present,
        .cycle_detected = false,
    };
}

void free_grid(Grid *g)
{
    free(g->map);
    g = NULL;
}

bool is_visiting(MapElement element)
{
    return element == ME_VISITING_EAST
        || element == ME_VISITING_WEST
        || element == ME_VISITING_NORTH
        || element == ME_VISITING_SOUTH;
}

bool is_visited(MapElement element)
{
    return element == ME_VISITED_EAST
        || element == ME_VISITED_WEST
        || element == ME_VISITED_NORTH
        || element == ME_VISITED_SOUTH;
}


void grid_move_guard_one_tick(Grid * grid)
{
    if (!grid->guard_is_present)
    {
        return;
    }
    
    // Find current guard state
    size_t guard_row = 0;
    size_t guard_col = 0;
    MapElement guard_state = ME_OPEN_UNVISITED;
    size_t i = 0;
    bool found = false;
    while (!found)
    {
        if (i >= grid->map_length)
        {
            fprintf(
                stderr,
                "[FATALERROR] grid_move_guard_one_tick(Grid): guard not found\n"
            );
            exit(1);
        }
        MapElement current_element = grid->map[i];
        if (is_visiting(current_element))
        {
            guard_row = i / grid->stride;
            guard_col = i % grid->stride;
            guard_state = current_element;
            found = true;
        }
        else
        {
            i += 1;
        }
    }

    // Set some vars based on the current guard state
    MapElement visited_state;
    MapElement turned_state;
    size_t delta_row;
    size_t delta_col;
    switch (guard_state)
    {
    case ME_VISITING_EAST:
        visited_state = ME_VISITED_EAST;
        turned_state = ME_VISITING_SOUTH;
        delta_row = 0;
        delta_col = 1;
        break;
    case ME_VISITING_WEST:
        visited_state = ME_VISITED_WEST;
        turned_state = ME_VISITING_NORTH;
        delta_row = 0;
        delta_col = -1;
        break;
    case ME_VISITING_NORTH:
        visited_state = ME_VISITED_NORTH;
        turned_state = ME_VISITING_EAST;
        delta_row = -1;
        delta_col = 0;
        break;
    case ME_VISITING_SOUTH:
        visited_state = ME_VISITED_SOUTH;
        turned_state = ME_VISITING_WEST;
        delta_row = 1;
        delta_col = 0;
        break;
    default: assert(false); break;  // The current guard_state cannot be anything else
    }

    // Is the next location in bounds
    size_t new_row = guard_row + delta_row;
    size_t new_col = guard_col + delta_col;
    bool target_in_bounds = (new_row >= 0)
        && (new_row < (grid->map_length / grid->stride))
        && (new_col >= 0)
        && (new_col < grid->stride);
    
    // Is there an obstruction or oob where the guard is trying to go?
    if (target_in_bounds)
    {
        size_t target_index = (new_row * grid->stride) + new_col;
        MapElement target_element = grid->map[target_index];
        if (target_element == ME_OBSTRUCTED) // Uh-oh, the path is blocked
        {
            // Turn the guard 90 degrees in place
            grid->map[i] = turned_state;
        }
        else
        {

            
            if (is_visited(target_element))
            {
                switch (target_element)
                {
                case ME_VISITED_EAST: if (guard_state == ME_VISITING_EAST) {
                        grid->cycle_detected = true;
                    }
                    break;
                case ME_VISITED_WEST: if (guard_state == ME_VISITING_WEST) {
                        grid->cycle_detected = true;
                    }
                    break;
                case ME_VISITED_NORTH:if (guard_state == ME_VISITING_NORTH) {
                        grid->cycle_detected = true;
                    }
                    break;
                case ME_VISITED_SOUTH:if (guard_state == ME_VISITING_SOUTH) {
                        grid->cycle_detected = true;
                    } 
                    break;
                default: assert(false); // Unreachable

                }
            }
            // Move the guard to the new location
            grid->map[i] = visited_state;
            grid->map[target_index] = guard_state;
        }
    }
    else
    {
        grid->map[i] = visited_state;
        grid->guard_is_present = false;
    }

}

void grid_simulate_guard_movement(Grid g)
{
    while (g.guard_is_present)
    {
        grid_move_guard_one_tick(&g);
    }
}

size_t grid_count_visited_locations(const Grid grid)
{
    size_t visited_count = 0;
    for (size_t i = 0; i < grid.map_length; i++)
    {
        if (is_visited(grid.map[i]))
        {
            visited_count += 1;
        }
    }
    return visited_count;
}

void dump_grid(const Grid g)
{
    for (size_t i = 0; i < g.map_length; i++)
    {
        switch (g.map[i])
        {
        case ME_OPEN_UNVISITED:putchar('.'); break;
        case ME_OBSTRUCTED:    putchar('#'); break;
        case ME_VISITED_EAST:
        case ME_VISITED_WEST:
        case ME_VISITED_NORTH:
        case ME_VISITED_SOUTH: putchar('@'); break;
        case ME_VISITING_EAST: putchar('>'); break;
        case ME_VISITING_WEST: putchar('<'); break;
        case ME_VISITING_NORTH:putchar('^'); break;
        case ME_VISITING_SOUTH: putchar('V'); break;
        }
        if (((i + 1) % g.stride) == 0)
        {
            putchar('\n');
        }
    }
}

void str_show(const Str input) {
    for (size_t i = 0; i < input.data_length; i++)
    {
        putchar(input.data[i]);
    }
}

void show_strs(const Str strs[], const size_t strs_length)
{
    for (size_t i = 0; i < strs_length; i++)
    {
        str_show(strs[i]);
        putchar('\n');
    }
}

size_t part_01(Str input) {
    Grid g = create_grid_from_input(input);
    grid_simulate_guard_movement(g);
    size_t visited_count = grid_count_visited_locations(g);
    free_grid(&g);
    return visited_count;
}

bool has_cycle(Grid g) {
    while (g.guard_is_present && !g.cycle_detected)
    {
        grid_move_guard_one_tick(&g);
    }
    return g.cycle_detected;
}

size_t part_02(Str input) {
    size_t cycle_count = 0;
    Grid g = create_grid_from_input(input);
    grid_simulate_guard_movement(g);
    for (size_t i = 0; i < g.map_length; i++)
    {
        Grid inner = create_grid_from_input(input);
        if (is_visited(g.map[i]) && !is_visiting(inner.map[i]))
        {
            inner.map[i] = ME_OBSTRUCTED;
            if (has_cycle(inner))
            {
                cycle_count += 1;
            }
        }
        free_grid(&inner);
    }
    free_grid(&g);
    return cycle_count;
}


int main(const int argc, const char *argv[])
{
    if (argc < 2)
    {
        const char *prog = argv[0];
        printf("Usage: %s <path/to/input>", prog);
        exit(1);
    }

    const char *file_path = argv[1];
    char buffer[BUFFER_SIZE] = {0};
    Str input = str_from_file(file_path, buffer, BUFFER_SIZE);
    size_t part_01_result = part_01(input);
    printf("Day-06;Part-01: %zu\n", part_01_result);
    size_t part_02_result = part_02(input);
    printf("Day-06;Part-02: %zu\n", part_02_result);
}
