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
        return "$offset(%${reg.x})"
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
    fun convVar(offsetMap: HashMap<String, XOffset>)
    fun emitFix(): Instruction?
}

data class XRaw(private val x: String): Instruction {
    // No-op
    override fun convVar(offsetMap: HashMap<String, XOffset>) {}

    // Does not need fix
    override fun emitFix(): Instruction? = null

    override fun toString(): String {
        return "$x\n"
    }
}

data class XAddq(private var al: XArg, private var ar: XArg): Instruction {
    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val lA = al
        val rA = ar
        // Using unsafe casts. If we fail, we messed up earlier and we deserve to fail.
        if (lA is XVar) {
            al = offsetMap[lA.x] as XOffset
        }

        if (rA is XVar) {
            ar = offsetMap[rA.x] as XOffset
        }
    }

    override fun toString(): String {
        return "addq\t$al, $ar\n"
    }
}

data class XSubq(private var al: XArg, private var ar: XArg): Instruction {
    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val lA = al
        val rA = ar
        // Using unsafe casts. If we fail, we messed up earlier and we deserve to fail.
        if (lA is XVar) {
            al = offsetMap[lA.x] as XOffset
        }

        if (rA is XVar) {
            ar = offsetMap[rA.x] as XOffset
        }
    }

    override fun toString(): String {
        return "subq\t$al, $ar\n"
    }
}

data class XCallq(private var a: XArg): Instruction {
    // Do not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val xV = a
        if (xV is XVar) {
            a = offsetMap[xV.x] as XOffset
        }
    }

    override fun toString(): String {
        return "callq\t$a\n"
    }
}

data class XMovq(private var al: XArg, private var ar: XArg): Instruction {
    override fun emitFix(): Instruction? {
        if (al is XOffset && ar is XOffset) {
            val movq = XMovq(al, XReg("rax"))
            al = XReg("rax")
            return movq
        }
        return null
    }

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val lA = al
        val rA = ar
        // Using unsafe casts. If we fail, we messed up earlier and we deserve to fail.
        if (lA is XVar) {
            al = offsetMap[lA.x] as XOffset
        }

        if (rA is XVar) {
            ar = offsetMap[rA.x] as XOffset
        }
    }

    override fun toString(): String {
        return "movq\t$al, $ar\n"
    }
}

data class XPushq(private var a: XArg): Instruction {
    // Do not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val aV = a
        if (aV is XVar) {
            a = offsetMap[aV.x] as XOffset
        }
    }

    override fun toString(): String {
        return "pushq\t$a\n"
    }
}

data class XNegq(private var a: XArg): Instruction {
    override fun emitFix(): Instruction? = null

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val aV = a
        if (aV is XVar) {
            a = offsetMap[aV.x] as XOffset
        }
    }

    override fun toString(): String {
        return "negq\t$a\n"
    }
}

data class XPopq(private var a: XArg): Instruction {
    // Does not need fix
    override fun emitFix(): Instruction? = null

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        val aV = a
        if (aV is XVar) {
            a = offsetMap[aV.x] as XOffset
        }
    }

    override fun toString(): String {
        return "popq\t$a\n"
    }
}

class XRetq: Instruction {
    // lol
    override fun emitFix(): Instruction? = null

    override fun convVar(offsetMap: HashMap<String, XOffset>) {
        // no need to do anything
    }

    override fun toString(): String {
        return "retq\n"
    }
}

data class XProgram(val varList: MutableList<XArg>, val instrList: MutableList<Instruction>)

fun assign(xp: XProgram) {
    val offsetMap = hashMapOf<String, XOffset>()
    var counter = 1
    xp.varList.forEach {
        when(it) {
            is XVar -> {
                offsetMap[it.x] = XOffset(XReg("rsp"), -8 * (counter))
                counter += 1
            }
        }
    }

    val varAssign = mutableListOf<Instruction>()

    varAssign.add(XRaw(".globl _main"))
    varAssign.add(XRaw("_main:"))
    varAssign.add(XPushq(XReg("rbp")))
    varAssign.add(XMovq(XReg("rsp"), XReg("rbp")))

    // FUCK OS X
    var size = offsetMap.size * 8
    if (size % 16 != 0)  {
        size += 8
    }

    varAssign.add(XSubq(XInt(size), XReg("rsp")))

    xp.instrList.forEach {
        it.convVar(offsetMap)
    }

    xp.instrList.addAll(0, varAssign)

    xp.instrList.add(XAddq(XInt(size), XReg("rsp")))
    xp.instrList.add(XMovq(XReg("rbp"), XReg("rsp")))
    xp.instrList.add(XPopq(XReg("rbp")))
    xp.instrList.add(XRetq())
}

fun fix(xp: XProgram): Asm {
    val asm = Asm(mutableListOf())
    xp.varList.forEach {

    }
    xp.instrList.forEach {
        it.emitFix()?.let { fix ->
            asm.instr.add(fix)
        }
        asm.instr.add(it)
    }

    return asm
}

data class Asm(val instr: MutableList<Instruction>)