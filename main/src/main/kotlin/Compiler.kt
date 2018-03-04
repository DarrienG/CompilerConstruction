import java.io.File
import kotlin.system.measureTimeMillis

class Compiler() {
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
     * Select: Convert CProgram [CProgram] to an accompanying XProgram [XProgram].
     * New class is a mish mash of almost assembly instructions.
     */
    private fun i2(cP: CProgram): XProgram = cP.select()

    /**
     * Assigns all variables to assembly type "variables"
     * Variables either become registers, or positions in the stack.
     */
    private fun i3(xP: XProgram) = assign(xP)

    /**
     * Fixes illegal assembly instructions and returns a completed set of working
     * assembly instructions in the form of an Asm [Asm] object.
     */
    private fun i4(xP: XProgram) = fix(xP)

    private fun printAsm(asm: Asm, toFile: Boolean = false, fileName: String = "test.s") {
        if (toFile) {
            File(fileName).printWriter().use { out ->
                asm.instr.forEach { out.write(it.toString()) }
            }
        }
        else {
            asm.instr.forEach { print(it) }
        }
    }

    fun compile(p: Program,
                toFile: Boolean = false,
                verbose: Any? = null,
                timed: Any? = null) {

        verbose?.let { println("INPUT\n$p") }
        if (timed != null) {
            println("Uniquify timing: ${measureTimeMillis { i0(p) }}ms")
        } else {
            i0(p)
        }
        verbose?.let { println("\nUNIQUIFIED\n$p") }

        var cProgram  = CProgram(hashSetOf(), mutableListOf(), CVar("null"))
        if (timed != null) {
            println("Flatten timing: ${measureTimeMillis { cProgram = i1(p) }}ms")
        } else {
            cProgram = i1(p)
        }
        verbose?.let { println("\nFLATTENED\n$cProgram") }

        var xProgram = XProgram(mutableListOf(), mutableListOf())
        if (timed != null) {
            println("Select timing: ${measureTimeMillis { xProgram = i2(cProgram) }}ms")
        } else {
            xProgram = i2(cProgram)
        }
        verbose?.let { println("\nSELECTED\n$xProgram") }

        if (timed != null) {
            println("Assign timing: ${measureTimeMillis { i3(xProgram) }}ms")
        } else {
            i3(xProgram)
        }
        verbose?.let { println("\nASSIGNED\n$xProgram") }

        var asm = Asm(mutableListOf())
        if (timed != null) {
            println("Fix timing: ${measureTimeMillis { asm = i4(xProgram) }}ms")
        } else {
            asm = i4(xProgram)
        }
        verbose?.let { println("\nFIXED\n$xProgram") }

        printAsm(asm, toFile)
    }
}