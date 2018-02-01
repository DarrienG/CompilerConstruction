class Transformer {
    /**
     * Uniquify: Remove all variable shadowing.
     */
    fun i0(p: Program) {
        p.uniquify()
    }

    /**
     * Flatten: Remove tree-like structure from program.
     * Will fail if program is not flattened.
     */
    fun i1(p: Program) {
        
    }

    fun compile(p: Program) {
        i0(p)
        i1(p)
    }
}