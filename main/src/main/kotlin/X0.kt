interface XArg

data class XReg(val x: String): XArg
data class XOffset(val offset: Int): XArg
data class XVar(val x: String): XArg

interface Instruction

data class XAddq(val al: XArg, val ar: XArg): Instruction
data class XSubq(val al: XArg, val ar: XArg): Instruction
data class XCallq(val xl: XArg, val xr: XArg): Instruction
data class XMovq(val al: XArg, val ar: XArg): Instruction
data class XPushq(val a: XArg): Instruction
data class XNegq(val a: XArg): Instruction
data class XPopq(val a: XArg): Instruction
data class XRetq(val a: XArg): Instruction

data class XProgram(val varList: MutableList<XArg>, val instrList: MutableList<Instruction>)
