package cn.omsfuk.compiling.support;

public class RefEntry {

    public enum RefType {
        CONST, VARIABLE, PROCEDURE
    }

    private String name;

    private RefType kind;

    private Integer level;

    private Integer adr;

    public RefEntry(String name, RefType kind, Integer level, Integer adr) {
        this.name = name;
        this.kind = kind;
        this.level = level;
        this.adr = adr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RefType getKind() {
        return kind;
    }

    public void setKind(RefType kind) {
        this.kind = kind;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getAdr() {
        return adr;
    }

    public void setAdr(Integer adr) {
        this.adr = adr;
    }

    @Override
    public String toString() {
        return String.format("[%-10s%-10s%-10s%-10s", name, kind, level, adr);
    }
}
