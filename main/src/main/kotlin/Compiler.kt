class Compiler {
    /**
     * Uniquify: Remove all variable shadowing.
     */
    private fun i0(p: Program) = p.uniquify()


    /**
     * Flatten: Remove tree-like structure from program.
     * Will fail if program is not flattened.
     */
    private fun i1(p: Program): CProgram =  p.flatten()

    /**
     * Select:
     */
    private fun i2(cP: CProgram): XProgram = cP.select()

    private fun i3(xP: XProgram) = assign(xP)

    private fun i4(xP: XProgram) = fix(xP)

    private fun printAsm(asm: Asm) = asm.instr.forEach {print(it) }

    fun compile(p: Program, verbose: Any? = null) {
        verbose?.let { println("INPUT\n$p") }
        i0(p)
        verbose?.let { println("\nUNIQUIFIED\n$p") }
        val cProgram = i1(p)
        verbose?.let { println("\nFLATTENED\n$cProgram") }
        val xProgram = i2(cProgram)
        verbose?.let { println("\nSELECTED\n$xProgram") }
        i3(xProgram)
        verbose?.let { println("\nASSIGNED\n$xProgram") }
        val asm = i4(xProgram)
        verbose?.let { println("\nFIXED\n$xProgram") }
        printAsm(asm)
    }
}