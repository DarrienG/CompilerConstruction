import java.util.Arrays
import java.util.LinkedList

/**
 * Fun simple graph class that gets coloring.
 * The user passes in a list of their data so we can initialize our adjacency list to the size of
 * the list of data.
 */
internal class RegGraph<T>(private val dataList: List<T>) {
    /**
     * Adjacency list/
     */
    private val adj: MutableList<MutableSet<Int>> = mutableListOf()

    init {
        (0 until dataList.size).mapTo(adj) { mutableSetOf() }
    }

    /**
     * Throw that liveness in there. You better hope you keep the index of your shit in the same
     * spot.
     * TODO: Throw all that data around in an object and make the user's life easier.
     * @param v: Node 1
     * @param w: Node 2
     */
    fun addEdge(v: Int, w: Int) {
        adj[v].add(w)
        adj[w].add(v)
    }

    /**
     * COLOR AWAY MY DUDES
     */
    fun colorAwayMyDudes(debug: Any? = null): IntArray {
        val result = IntArray(dataList.size)

        // Initialize all vertices as unassigned
        Arrays.fill(result, -1)

        // Assign the first color to first vertex
        result[0] = 0

        // A temporary array to store the available colors. False
        // value of available[cr] would mean that the color cr is
        // assigned to one of its adjacent vertices
        val available = BooleanArray(dataList.size)

        // Initially, all colors are available
        Arrays.fill(available, true)

        // Assign colors to remaining dataList-1 vertices
        for (u in 1 until dataList.size) {
            // Process all adjacent vertices and flag their colors
            // as unavailable
            val it = adj[u].iterator()
            while (it.hasNext()) {
                val i = it.next()
                if (result[i] != -1)
                    available[result[i]] = false
            }

            // Find the first available color
            var cr = 0
            while (cr < dataList.size) {
                if (available[cr])
                    break
                cr++
            }

            result[u] = cr // Assign the found color

            // Reset the values back to true for the next iteration
            Arrays.fill(available, true)
        }

        debug?.let {
            for (u in 0 until dataList.size) {
                println("Vertex " + u + " --->  Color "
                        + result[u])
            }
        }

        return result
    }
}

