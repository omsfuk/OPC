package cn.omsfuk.compiling.support;

public class Instruction {

    /**
     * OPR 0 0 返回
     * OPR 0 1/2/3/4 加减乘除
     * OPR 0 5/6/7/8/9/10 < <= = >= > #
     * OPR 11 ODD
     * OPR 0 16 读
     * OPR 0 15 写
     */
    public enum OP {
        LIT, LOD, STO, CAL, INT, JMP, JPC, OPR
    }


    private OP op;

    private Integer levelDelta;

    private Integer offset;

    public Instruction(OP op, int levelDelta, int offset) {
        this.op = op;
        this.levelDelta = levelDelta;
        this.offset = offset;
    }

    public OP getOp() {
        return op;
    }

    public void setOp(OP op) {
        this.op = op;
    }

    public Integer getLevelDelta() {
        return levelDelta;
    }

    public void setLevelDelta(Integer levelDelta) {
        this.levelDelta = levelDelta;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("%-10s%-10s%-10s", op, levelDelta, offset);
    }
}
