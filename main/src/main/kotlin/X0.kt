interface XArg

data class XType(private val t: Type): XArg {
    override fun toString(): String {
        return t.toString()
    }
}

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
        return "$offset(%${reg.x})"
    }
}

// Does not need overridden toString. Should not be printed to asm anyway.
data class XVar(val x: String): XArg

data class XLabel(private val x: String): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        // no-op
    }

    override fun emitFix(): Instruction? {
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    fun invoke(): XLabel {
        return XLabel("$x:\n")
    }

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
    override fun convVar(regMap: HashMap<XArg, XArg>) {}

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
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun toString(): String {
        return "addq\t$al, $ar\n"
    }
}

data class XAnd(private var al: XArg, private var ar: XArg): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun toString(): String {
        return "andq\t$al, $ar\n"
    }
}

data class XOrq(private var al: XArg, private var ar: XArg): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun toString(): String {
        return "orq\t$al, $ar\n"
    }
}

data class XXOrq(private var al: XArg, private var ar: XArg): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        if (ar is XInt) {
            val tmp = ar
            ar = al
            al = tmp
        }
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun toString(): String {
        return "xorq\t$al, $ar\n"
    }
}

data class XCmpq(private var al: XArg, private var ar: XArg): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        val lA = al
        val rA = ar
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        if (al is XVar) liveVars.add(al)
        if (ar is XVar) liveVars.add(ar)
    }

    override fun toString(): String {
        return "cmpq\t$al, $ar\n"
    }
}

data class XJmp(private val lab: XLabel): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        // no-op
    }

    override fun emitFix(): Instruction? {
       return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    override fun toString(): String {
        return "jmp\t\t$lab\n"
    }
}

data class XJmpIf(private var cc: CmpType, private val lab: XLabel): Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        // no-op
    }

    override fun emitFix(): Instruction? {
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    // This is not what a toString is supposed to look like
    override fun toString(): String {
        return when(cc) {
            CmpType.EQ -> "je\t\t$lab\n"
            CmpType.NE -> "jne\t\t$lab\n"
            CmpType.GT -> "jg\t\t$lab\n"
            CmpType.GTE -> "jge\t\t$lab\n"
            CmpType.LT -> "jl\t\t$lab\n"
            CmpType.LTE -> "jle\t\t$lab\n"
            CmpType.ZER -> "jz\t\t$lab\n"
            CmpType.NZER -> "jpz\t\t$lab\n"
            CmpType.AND -> "je\t\t$lab\n"
            CmpType.OR -> "jg\t\t$lab\n"
            CmpType.NOT -> "jne\t\t$lab\n"
        }
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
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
    }

    override fun toString(): String {
        return "subq\t$al, $ar\n"
    }
}

data class XCallq(private var a: XLabel): Instruction {
    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    // Do not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(regMap: HashMap<XArg, XArg>) {
    }

    override fun toString(): String {
        return "callq\t$a\n"
    }

    fun emitFullInstrSet(): List<Instruction> {
        return listOf(
                XPushq(XReg("rcx")),
                XPushq(XReg("rdx")),
                XPushq(XReg("rsi")),
                XPushq(XReg("rdi")),
                XPushq(XReg("r8")),
                XPushq(XReg("r9")),
                XPushq(XReg("r10")),
                XPushq(XReg("r11")),
                this,
                XPopq(XReg("r11")),
                XPopq(XReg("r10")),
                XPopq(XReg("r9")),
                XPopq(XReg("r8")),
                XPopq(XReg("rdi")),
                XPopq(XReg("rsi")),
                XPopq(XReg("rdx")),
                XPopq(XReg("rcx"))
        )
    }
}

class XMovzbq: Instruction {
    override fun convVar(regMap: HashMap<XArg, XArg>) {
        //no-op
    }

    override fun emitFix(): Instruction? {
        return null
    }

    override fun liveVars(liveVars: MutableSet<XArg>) {
        // no-op
    }

    override fun toString(): String {
        return "movzbq\t${XReg("al")}, ${XReg("rax")}\n"
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
        regMap[lA]?.let { al = it }
        regMap[rA]?.let { ar = it }
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
        regMap[aV]?.let { a = it }
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
        regMap[aV]?.let { a = it }
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
        regMap[aV]?.let { a = it }
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
                addedEdge?.let { edge -> varMap[startNode]?.edges?.add(edge) }
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