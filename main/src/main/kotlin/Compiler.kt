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
     * Select: Converts C0 program to almost valid assembly program with variables.
     */
    private fun i2(cP: CProgram): XProgram = cP.select()

    /**
     * Assign: Assigns variables to registers.
     */
    private fun i3(xP: XProgram) = assign(xP)

    /**
     * Fix: Removes double register references in assembly code.
     * @return Returns fully cooked x86 asm.
     */
    private fun i4(xP: XProgram) = fix(xP)

    /**
     * Prints finished assembly.
     */
    private fun printAsm(asm: Asm) = asm.instr.forEach { print(it) }

    /**
     * Compiles a program to assembly.
     * @param p Program to compile.
     * @param verbose Show verbose output when compiling. Put anything in this param to make verbose.
     */
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