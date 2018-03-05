interface XArg

data class XInt(private val x: Int): XArg {
    override fun toString(): String {
        return "\$$x"
    }
}
data class XReg(val x: String): XArg {
    override fun toString(): String {
        return "%$x"
    }
}

data class XOffset(private val reg: XReg, private val offset: Int): XArg {
    override fun toString(): String {
        return if (offset > 0) {
            "$offset(%${reg.x})"
        } else {
            "%${reg.x}"
        }
    }
}

// Does not need overridden toString. Should not be printed to asm anyway.
data class XVar(val x: String): XArg

data class XLabel(private val x: String): XArg {
    override fun toString(): String {
        return x
    }
}

interface Instruction {
    /**
     * Grab into the hat of registers with your XArg [XArg] as a token, and see what you get!
     */
    fun convVar(regMap: HashMap<XArg, XArg>)

    /**
     * Fixes invalid instructions and potentially returns an instruction that needs to be placed
     * before just fixed instruction.
     */
    fun emitFix(): Instruction?

    /**
     * Takes a set of live variables and modifies set in place to hold new live vars.
     */
    fun liveVars(liveVars: MutableSet<XArg>)
}

data class XRaw(private val x: String): Instruction {
    // no-op
    override fun liveVars(liveVars: MutableSet<XArg>) {
    }

    // No-op
    override fun convVar(offsetMap: HashMap<XArg, XArg>) {}

    // Does not need fix
    override fun emitFix(): Instruction? = null

    override fun toString(): String {
        return "$x\n"
    }
}

data class XAddq(private var al: XArg, private var ar: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        // Using unsafe casts. If we fail, we messed up earlier and we deserve to fail.
        if (lA is XVar) { regMap[lA]?.let { al = it } }
        if (rA is XVar) { regMap[rA]?.let { ar = it } }
    }

    override fun toString(): String {
        return "addq\t$al, $ar\n"
    }
}

data class XSubq(private var al: XArg, private var ar: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        if (lA is XVar) { regMap[lA]?.let { al = it } }
        if (rA is XVar) { regMap[rA]?.let { ar = it } }
    }

    override fun toString(): String {
        return "subq\t$al, $ar\n"
    }
}

data class XCallq(private var a: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    // Do not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val xV = a
        if (xV is XVar) { regMap[xV]?.let { a = it } }
    }

    override fun toString(): String {
        return "callq\t$a\n"
    }
}

data class XMovq(private var al: XArg, private var ar: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        liveVars.remove(ar)
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        if (lA is XVar) { regMap[lA]?.let { al = it } }
        if (rA is XVar) { regMap[rA]?.let { ar = it } }
    }

    override fun toString(): String {
        return "movq\t$al, $ar\n"
    }
}

data class XPushq(private var a: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    // Do not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val aV = a
        if (aV is XVar) { regMap[aV]?.let { a = it } }
    }

    override fun toString(): String {
        return "pushq\t$a\n"
    }
}

data class XNegq(private var a: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (a is XVar) liveVars.add(a)
    }

    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val aV = a
        if (aV is XVar) { regMap[aV]?.let { a = it } }
    }

    override fun toString(): String {
        return "negq\t$a\n"
    }
}

data class XPopq(private var a: XArg): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
       // no-op
    }

    // Does not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val aV = a
        if (aV is XVar) { regMap[aV]?.let { a = it } }
    }

    override fun toString(): String {
        return "popq\t$a\n"
    }
}

class XRetq: Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    // lol
    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
        // no need to do anything
    }

    override fun toString(): String {
        return "retq\n"
    }
}

data class XProgram(val varList: MutableList<XArg>, val instrList: MutableList<Instruction>)

data class MetaVar(val idx: Int, val edges: MutableSet<Int>)


// TODO GET RID OF ALL CASTS IN CONVVAR??????????

enum class NumRegs(val regCount: Int) {
    NONE(0),
    CALLEE(5),
    CALLER(10),
    MAX(12);

    private val register = listOf(
            "r12", "r13", "r14", "r15", // callee saves
            "r8", "r9", "r10", "r11",   // caller saves
            "rcx", "rdx", "rsi", "rdi") // requires extra logic

    fun getRegSet(cChoice: Int): XArg {
        return if (cChoice >= regCount) {
            XOffset(XReg("rsp"), -8 * (cChoice + 1))
        } else {
            XReg(register[cChoice])
        }
    }
}

fun assign(xp: XProgram, numRegs: NumRegs) {
    val varMap = hashMapOf<XArg, MetaVar>()
    val liveVars = mutableSetOf<XArg>()
    for ((idx, arg) in xp.varList.withIndex()) {
        varMap[arg] = MetaVar(idx, mutableSetOf())
    }

    // This whole set of statements kind of makes me want to die
    // Gets a set of edges for each of the variables in the program
    xp.instrList.reversed().forEach { instr ->
        instr.liveVars(liveVars)
        val iterableArgs = liveVars.toList()

        // Get all of the edges for our graph
        for (i in 0 until iterableArgs.size) {
            for (j in i + 1 until iterableArgs.size) {
                val startNode = iterableArgs[i]
                val addedEdge = varMap[iterableArgs[j]]?.idx
                addedEdge?.let { edge ->
                    varMap[startNode]?.edges?.add(edge)
                }
            }
        }
    }

    val regGraph = RegGraph(xp.varList)
    varMap.forEach {
        it.value.edges.forEach { edge ->
            regGraph.addEdge(it.value.idx, edge)
        }
    }

    val coloredNodes = regGraph.colorAwayMyDudes()
    assert(coloredNodes.size == varMap.size)

    val regMap = hashMapOf<XArg, XArg>()
    varMap.forEach {
        regMap[it.key] = numRegs.getRegSet(coloredNodes[it.component2().idx])
    }

    val varAssign = mutableListOf<Instruction>()
    varAssign.add(XRaw(".globl _main"))
    varAssign.add(XRaw("_main:"))
    varAssign.add(XPushq(XReg("rbp")))
    varAssign.add(XMovq(XReg("rsp"), XReg("rbp")))

    // FUCK OS X
    val distinctVars = coloredNodes.distinct().count()
    var size = if (distinctVars < numRegs.regCount) 0 else (distinctVars - numRegs.regCount) * 8
    if (size % 16 != 0)  {
        size += 8
    }

    if (size > 0) varAssign.add(XSubq(XInt(size), XReg("rsp")))

    // Instructions are registers when applicable now
    xp.instrList.forEach { it.convVar(regMap) }

    xp.instrList.addAll(0, varAssign)

    if (size > 0) xp.instrList.add(XAddq(XInt(size), XReg("rsp")))
    xp.instrList.add(XMovq(XReg("rbp"), XReg("rsp")))
    xp.instrList.add(XPopq(XReg("rbp")))
    xp.instrList.add(XRetq())
}

/**
 * Creates a mapping from variables [XVar] to registers [XReg] when applicable.
 */
fun fix(xp: XProgram): Asm {
    val asm = Asm(mutableListOf())
    xp.instrList.forEach {
        it.emitFix()?.let { fix ->
            asm.instr.add(fix)
        }
        asm.instr.add(it)
    }

    return asm
}

data class Asm(val instr: MutableList<Instruction>)