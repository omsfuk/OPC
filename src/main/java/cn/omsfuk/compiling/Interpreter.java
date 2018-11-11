package cn.omsfuk.compiling;

import cn.omsfuk.compiling.analyzer.SyntaxAnalyzer;
import cn.omsfuk.compiling.analyzer.TokenAnalyzer;
import cn.omsfuk.compiling.exception.SyntaxErrorException;
import cn.omsfuk.compiling.support.Instruction;
import cn.omsfuk.compiling.support.Instruction.OP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static cn.omsfuk.compiling.support.Instruction.OP.*;

public class Interpreter {

    private List<Instruction> instructions;

    private int ir; // instruction register

    private int sp; // stack pointer

    private int pc; // program count

    private int bx; // base address

    private int[] stack = new int[1024000];


    private Scanner scanner;

    public Interpreter(String filename) {

    }

    public Interpreter(File file) {

    }

    public Interpreter(List<Instruction> instructions, int pc) {
        this.instructions = instructions;
        this.pc = pc;
    }

    public void setInputStream(InputStream inputStream) {
        scanner = new Scanner(inputStream);
    }

    public void exec() {
        int maxSp = 0;
        do {
            Instruction instruction = instructions.get(pc);
            pc ++;
            dispatch(instruction);
            maxSp = sp > maxSp ? sp : maxSp;
        } while (sp != 0);
        System.out.println("MaxSP: " + maxSp);
    }

    private void dispatch(Instruction instruction) {
        OP op = instruction.getOp();
        if (LIT.equals(op)) {
            lit(instruction.getOffset());
        } else if (LOD.equals(op)) {
            lod(instruction.getLevelDelta(), instruction.getOffset());
        } else if (STO.equals(op)) {
            sto(instruction.getLevelDelta(), instruction.getOffset());
        } else if (CAL.equals(op)) {
            cal(instruction.getLevelDelta(), instruction.getOffset());
        } else if (INT.equals(op)) {
            init(instruction.getOffset());
        } else if (JMP.equals(op)) {
            jmp(instruction.getOffset());
        } else if (JPC.equals(op)) {
            jpc(instruction.getOffset());
        } else if (OPR.equals(op)) {
            opr(instruction.getOffset());
        }
    }

    private void lit(int number) {
        stack[sp ++] = number;
    }

    private void lod(int levelDelta, int offset) {
        // TODO 只有可能是父作用域
        int targetBx = bx;
        while (levelDelta != 0) {
            targetBx = stack[bx];
            levelDelta --;
        }
        stack[sp++] = stack[targetBx + offset];
    }

    private void sto(int levelDelta, int offset) {
        int targetBx = bx;
        while (levelDelta != 0) {
            targetBx = stack[bx];
            levelDelta --;
        }
        stack[targetBx + offset] = stack[--sp];
    }

    private void cal(int levelDelta, int addr) {
        int oldSp = sp;
        if (levelDelta == 0) {
            stack[sp++] = bx;
            stack[sp++] = bx;
            stack[sp++] = pc;
            bx = oldSp;
        } else {
            stack[sp++] = stack[bx];
            stack[sp++] = bx;
            stack[sp++] = pc;
            bx = oldSp;
        }

        pc = addr;
    }

    private void init(int size) {
        sp += size;
    }

    private void jmp(int addr) {
        pc = addr;
    }

    private void jpc(int addr) {
        if (stack[--sp] == 0) {
            pc = addr;
        }
    }

    private void opr(int op) {
        if (op >= 1 && op <= 10) {
            int op2 = stack[--sp];
            int op1 = stack[--sp];
            if (op == 1) {
                stack[sp++] = op1 + op2;
            } else if (op == 2) {
                stack[sp++] = op1 - op2;
            } else if (op == 3) {
                stack[sp++] = op1 * op2;
            } else if (op == 4) {
                stack[sp++] = op1 / op2;
            } else if (op == 5) {
                stack[sp++] = op1 < op2 ? 1 : 0;
            } else if (op == 6) {
                stack[sp++] = op1 <= op2 ? 1 : 0;
            } else if (op == 7) {
                stack[sp++] = op1 == op2 ? 1 : 0;
            } else if (op == 8) {
                stack[sp++] = op1 >= op2 ? 1 : 0;
            } else if (op == 9) {
                stack[sp++] = op1 > op2 ? 1 : 0;
            } else if (op == 10) {
                stack[sp++] = op1 != op2 ? 1 : 0;
            }
        } else if (op == 0) {
            pc = stack[bx + 2];
            sp = bx;
            bx = stack[bx + 1];
        } else if (op == 11) {
            stack[sp - 1] = stack[sp - 1] % 1 == 1 ? 1 : 0;
        } else if (op == 16) {
            stack[sp++] = scanner.nextInt();
        } else if (op == 15) {
            System.out.println(stack[--sp]);
        }
    }

    public static void main(String[] args) throws IOException {
        TokenAnalyzer tokenAnalyzer = new TokenAnalyzer(new FileInputStream("program.fuk"));
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer(tokenAnalyzer);
        analyzer.analyse();
        // analyzer.getInstructions().forEach(System.out::println);
        Interpreter interpreter = new Interpreter(analyzer.getInstructions(), analyzer.getStartPointer());
        interpreter.setInputStream(new FileInputStream("input.txt"));
        interpreter.exec();
    }

}

