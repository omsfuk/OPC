package cn.omsfuk.compiling.parser;

import java.util.*;

import static cn.omsfuk.compiling.support.Instruction.OP.*;
import static cn.omsfuk.compiling.support.Symbol.*;

import cn.omsfuk.compiling.support.*;
import cn.omsfuk.compiling.support.RefEntry.RefType;

public class SyntaxParser {

    private Iterator<Token> tokens;

    private Symbol symbol;

    private Token token;

    private Integer level = 0;

    private Integer adr = 2;

    private Map<String, RefEntry> refTable = new HashMap<>(); // 符号表

    private List<Instruction> instructions = new LinkedList<>(); // 生成的指令

    private Stack<Symbol> symbolStack = new Stack<>(); // 逆波兰表达式的符号栈

    private boolean inExpression; // 是否是处在表达式中，为建立逆波兰表达式服务

    private int startPointer = -1; // 存放找到的程序入口点

    private boolean findMain; // 用来标示时候已经找到main函数

    private TokenParser tokenParser;

    private boolean debugEnable;

    private Node node = new Node(null);

    public SyntaxParser(List<Token> tokens) {
        this.tokens = tokens.iterator();
        symbolStack.add(SYM_POUND);
    }

    public SyntaxParser(TokenParser tokenParser) {
        this.tokenParser = tokenParser;
        symbolStack.add(SYM_POUND);
    }

    public void setDebugEnable(boolean debugEnable) {
        this.debugEnable = debugEnable;
    }

    private void advance() {
        if (tokens == null || tokens.hasNext()) {
            if (inExpression) {
                enterStack(token);
            }
            if (tokens == null) {
                try {
                    token = tokenParser.nextToken();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                token = tokens.next();
            }
            symbol = token.getSymbol();
        } else {
            symbol = null;
            tokens = null;
        }
    }

    public Map<String, RefEntry> getRefTable() {
        return refTable;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public int getStartPointer() {
        return startPointer;
    }

    public Node getSyntaxTree() {
        return node;
    }

    public boolean parse() {
        advance();
        program();
        if (symbol == null) {
            return true;
        }
        return false;
    }

    private void program() {
        swap("program");
        subProgram(null);
        if (!SYM_PERIOD.equals(symbol)) {
            node.appendChild(new Node("."));
            error();
        }
        node = node.parent;
    }

    private void subProgram(String name) {
        swap("subProgram");
        boolean isMain = false;
        if (!findMain) {
            isMain = true;
            findMain = true;
        }
        if (SYM_CONT.equals(symbol)) {
            constDesc();
        }
        if (SYM_VAR.equals(symbol)) {
            varDesc();
        }
        int oldAdr = adr;
        if (SYM_PROCEDURE.equals(symbol)) {
            procedureDesc();
        }
        if (isMain) {
            startPointer = instructions.size();
        }

        if (name != null) {
            refTable.get(name).setAdr(instructions.size());
        }
        putInstruction(new Instruction(INT, 0, oldAdr + 1)); // int 0 adr
        statement();
        putInstruction(new Instruction(OPR, 0, 0)); // int 0 adr
        node = node.parent;
    }

    private void constDesc() {
        swap("constDesc");
        if (SYM_CONT.equals(symbol)) {
            appendChild("const");
            advance();
            if (SYM_IDENT.equals(symbol)) {
                constDef();
                while (SYM_COMMA.equals(symbol)) {
                    appendChild(",");
                    advance();
                    constDef();
                }
            } else {
                error();
            }
            if (SYM_SEMICOLON.equals(symbol)) {
                appendChild(";");
                advance();
            } else {
                error();
            }
        }
        node = node.parent;
    }

    private void constDef() {
        swap("constDef");
        if (SYM_IDENT.equals(symbol)) {
            String constName = token.getId();

            identity();

            advance();
            if (SYM_EQUAL.equals(symbol)) {
                appendChild("=");
                advance();
                if (SYM_NUMBER.equals(symbol)) {
                    refTable.put(constName, new RefEntry(constName, RefType.CONST, 0, Integer.parseInt(token.getId())));

                    number();

                    advance();
                } else {
                    error();
                }
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void varDesc() {
        swap("varDesc");
        if (SYM_VAR.equals(symbol)) {
            appendChild("var");
            advance();
            if (SYM_IDENT.equals(symbol)) {
                identity();

                refTable.put(token.getId(), new RefEntry(token.getId(), RefType.VARIABLE, level, ++adr));

                advance();
                while (SYM_COMMA.equals(symbol)) {
                    appendChild(",");
                    advance();
                    if (SYM_IDENT.equals(symbol)) {

                        identity();

                        refTable.put(token.getId(), new RefEntry(
                                token.getId(), RefType.VARIABLE, level, ++adr));

                        advance();
                    } else {
                        error();
                    }
                }
                if (SYM_SEMICOLON.equals(symbol)) {
                    appendChild(";");
                    advance();
                } else {
                    error();
                }
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private String procedureDesc() {
        swap("procedureDesc");
        String name = procedureHeader();

        subProgram(name);

        level --;
        if (SYM_SEMICOLON.equals(symbol)) {
            appendChild(";");
            advance();
            while (SYM_PROCEDURE.equals(symbol)) {
                procedureDesc();
            }
        } else {
            error();
        }
        node = node.parent;
        return name;
    }

    private String procedureHeader() {
        swap("procedureHeader");
        String name = null;
        if (SYM_PROCEDURE.equals(symbol)) {
            appendChild("procedure");
            advance();
            if (SYM_IDENT.equals(symbol)) {
                identity();

                name = token.getId();
                refTable.put(token.getId(), new RefEntry(token.getId(), RefType.PROCEDURE, level, instructions.size()));
                adr = 2;
                level ++;
                advance();
                if (SYM_SEMICOLON.equals(symbol)) {
                    appendChild(";");
                    advance();
                } else {
                    error();
                }
            }
        } else {
            error();
        }
        node = node.parent;
        return name;
    }

    private void statement() {
        swap("statement");
        if (SYM_IDENT.equals(symbol)) {
            assignStatement();
        } else if (SYM_IF.equals(symbol)) {
            conditionStatement();
        } else if (SYM_WHILE.equals(symbol)) {
            whileStatement();
        } else if (SYM_CALL.equals(symbol)) {
            callStatement();
        } else if (SYM_READ.equals(symbol)) {
            readStatement();
        } else if (SYM_WRITE.equals(symbol)) {
            writeStatement();
        } else if (SYM_BEGIN.equals(symbol)) {
            compositeStatement();
        } else {
            System.out.println();
        }
        node = node.parent;
    }

    private void assignStatement() {
        swap("assignStatement");
        if (SYM_IDENT.equals(symbol)) {
            identity();

            assertRef(token, RefType.VARIABLE);
            String varName = token.getId();
            advance();
            if (SYM_ASSIGN.equals(symbol)) {
                appendChild(":=");
                advance();
                inExpression = true;
                expression();
                inExpression = false;
                clear();
            } else {
                error();
            }
            RefEntry var = getRef(varName);
            putInstruction(new Instruction(STO, level - var.getLevel(), var.getAdr())); // STO
        } else {
            error();
        }
        node = node.parent;
    }

    private void compositeStatement() {
        swap("compositeStatement");
        if (SYM_BEGIN.equals(symbol)) {
            advance();
            if (SYM_IDENT.equals(symbol) || SYM_IF.equals(symbol) || SYM_WHILE.equals(symbol) || SYM_CALL.equals(symbol) ||
                    SYM_READ.equals(symbol) || SYM_WRITE.equals(symbol) || SYM_BEGIN.equals(symbol) || SYM_SEMICOLON.equals(symbol)) {
                statement();
                while (SYM_SEMICOLON.equals(symbol)) {
                    advance();
                    if (SYM_IDENT.equals(symbol) || SYM_IF.equals(symbol) || SYM_WHILE.equals(symbol) || SYM_CALL.equals(symbol) ||
                            SYM_READ.equals(symbol) || SYM_WRITE.equals(symbol) || SYM_BEGIN.equals(symbol) || SYM_SEMICOLON.equals(symbol)) {
                        statement();
                    }
                }
            }
            if (SYM_END.equals(symbol)) {
                advance();
            } else {
                error();
            }
        }
        node = node.parent;
    }

    private void condition() {
        swap("condition");
        // TODO 运算指令生成
        if (SYM_PLUS.equals(symbol) || SYM_SUB.equals(symbol) || SYM_IDENT.equals(symbol) ||
                SYM_NUMBER.equals(symbol) || SYM_LEFT_BRACKETS.equals(symbol)) {
            inExpression = true;
            expression();
            inExpression = false;
            clear();
            if (SYM_EQUAL.equals(symbol) || SYM_POUND.equals(symbol) || SYM_LESS.equals(symbol) || SYM_GREATER.equals(symbol) ||
                    SYM_LESS_EQUAL.equals(symbol) || SYM_GREATER_EQUAL.equals(symbol)) {
                relation();
                Symbol operator = symbol;
                advance();
                if (SYM_PLUS.equals(symbol) || SYM_SUB.equals(symbol) || SYM_IDENT.equals(symbol) ||
                    SYM_NUMBER.equals(symbol) || SYM_LEFT_BRACKETS.equals(symbol)) {
                    inExpression = true;
                    expression();
                    inExpression = false;
                    clear();

                    if (SYM_LESS.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 5));
                    } else if (SYM_LESS_EQUAL.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 6));
                    } else if (SYM_EQUAL.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 7));
                    } else if (SYM_GREATER_EQUAL.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 8));
                    } else if (SYM_GREATER.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 9));
                    } else if (SYM_POUND.equals(operator)) {
                        putInstruction(new Instruction(OPR, 0, 10));
                    } else {
                        throw new RuntimeException("Unsupported Operator");
                    }
                } else {
                    error();
                }
            } else {
                error();
            }
        } else if (SYM_ODD.equals(symbol)) {
            advance();
            inExpression = true;
            expression();
            inExpression = false;
            clear();
            putInstruction(new Instruction(OPR, 0, 11));
        } else {
            error();
        }
        node = node.parent;
    }

    private void expression() {
        swap("expression");
        // TODO 运算指令生成
        if (SYM_PLUS.equals(symbol) || SYM_SUB.equals(symbol)) {
            appendChild(token.getId());
            putInstruction(new Instruction(LIT, 0, 0)); // 如果以-/+开头，栈中补0
            advance();
        }
        entry();
        while (SYM_PLUS.equals(symbol) || SYM_SUB.equals(symbol)) {
            addSub();
            advance();
            entry();
        }
        node = node.parent;
    }

    private void entry() {
        swap("entry");
        // TODO 运算指令生成
        if (SYM_IDENT.equals(symbol) || SYM_NUMBER.equals(symbol) || SYM_LEFT_BRACKETS.equals(symbol)) {
            factor();
            while (SYM_MUL.equals(symbol) || SYM_DIV.equals(symbol)) {
                mulDiv();
                advance();
                factor();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void factor() {
        swap("factor");
        // TODO 运算指令生成
        if (SYM_IDENT.equals(symbol)) {
            identity();
            advance();
        } else if (SYM_NUMBER.equals(symbol)) {
            number();
            advance();
        } else if (SYM_LEFT_BRACKETS.equals(symbol)) {
            appendChild("(");
            advance();
            expression();
            if (SYM_RIGHT_BRACKETS.equals(symbol)) {
                appendChild(")");
                advance();
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void conditionStatement() {
        swap("conditionStatement");
        if (SYM_IF.equals(symbol)) {
            appendChild("if");
            advance();
            condition();
            if (SYM_THEN.equals(symbol)) {
                appendChild("then");
                Instruction instruction = new Instruction(JPC, 0, 0); // JPC
                putInstruction(instruction);
                advance();
                statement();
                instruction.setOffset(instructions.size()); // 地址回填
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void callStatement() {
        swap("callStatement");
        if (SYM_CALL.equals(symbol)) {
            appendChild("call");
            advance();
            if (SYM_IDENT.equals(symbol)) {
                identity();
                assertRef(token, RefType.PROCEDURE);
                RefEntry entry = getRef(token);
                putInstruction(new Instruction(CAL, level - entry.getLevel(), entry.getAdr()));// CAL
                advance();
            }
        }
        node = node.parent;
    }

    private void whileStatement() {
        swap("whileStatement");
        if (SYM_WHILE.equals(symbol)) {
            appendChild("while");
            int startPoint = instructions.size(); // while语句起始指令地址
            advance();
            condition();
            Instruction instruction = new Instruction(JPC, 0, 0);
            putInstruction(instruction);
            if (SYM_DO.equals(symbol)) {
                appendChild("do");
                advance();
                statement();
                putInstruction(new Instruction(JMP, 0, startPoint)); // JMP
                instruction.setOffset(instructions.size());
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void readStatement() {
        swap("readStatement");
        if (SYM_READ.equals(symbol)) {
            appendChild("read");
            advance();
            if (SYM_LEFT_BRACKETS.equals(symbol)) {
                appendChild("(");
                advance();
                if (SYM_IDENT.equals(symbol)) {
                    identity();
                    assertRef(token, RefType.VARIABLE);
                    RefEntry entry = getRef(token);
                    putInstruction(new Instruction(OPR, 0, 16));
                    putInstruction(new Instruction(STO, level - entry.getLevel(), entry.getAdr()));
                    advance();
                    while (SYM_COMMA.equals(symbol)) {
                        appendChild(",");
                        advance();
                        if (SYM_IDENT.equals(symbol)) {
                            identity();
                            assertRef(token, RefType.VARIABLE);
                            entry = getRef(token);
                            putInstruction(new Instruction(OPR, 0, 16));
                            putInstruction(new Instruction(STO, level - entry.getLevel(), entry.getAdr()));
                            advance();
                        }
                    }
                    if (SYM_RIGHT_BRACKETS.equals(symbol)) {
                        appendChild(")");
                        advance();
                    }
                } else {
                    error();
                }
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void writeStatement() {
        swap("writeStatement");
        if (SYM_WRITE.equals(symbol)) {
            appendChild("write");
            advance();
            if (SYM_LEFT_BRACKETS.equals(symbol)) {
                appendChild("(");
                advance();
                inExpression = true;
                expression();
                inExpression = false;
                clear();

                putInstruction(new Instruction(OPR, 0, 15)); // OPR 15 write
                while (SYM_COMMA.equals(symbol)) {
                    appendChild(",");
                    advance();
                    inExpression = true;
                    expression();
                    inExpression = false;
                    clear();
                    putInstruction(new Instruction(OPR, 0, 15)); // OPR 15 write
                }
                if (SYM_RIGHT_BRACKETS.equals(symbol)) {
                    appendChild(")");
                    advance();
                } else {
                    error();
                }
            } else {
                error();
            }
        } else {
            error();
        }
        node = node.parent;
    }

    private void identity() {
        swap("identity");
        if (!SYM_IDENT.equals(symbol)) {
            error();
        }
        appendChild(token.getId());
        node = node.parent;
    }

    private void number() {
        swap("number");
        if (!SYM_NUMBER.equals(symbol)) {
            error();
        }
        appendChild(token.getId());
        node = node.parent;
    }

    private void addSub() {
        swap("addSub");
        // TODO
        appendChild(token.getId());
        node = node.parent;
    }

    /**
     * 不起到检查作用，因为已经检查
     */
    private void mulDiv() {
        swap("mulDiv");
        // TODO
        appendChild(token.getId());
        node = node.parent;
    }

    private void relation() {
        swap("relation");
        appendChild(token.getId());
        node = node.parent;
    }
    /**
     * 出错处理
     */
    private void error() {
        throw new RuntimeException(String.format("Error, at symbol %s", token));
    }

    /**
     * 对RefType类型的断言
     * @param token
     * @param type
     */
    private void assertRef(Token token, RefType type) {
        RefEntry entry = refTable.get(token.getId());
        if (entry == null) {
            throw new RuntimeException(String.format("Undefined reference %s, %s:%s", token.getId(), token.getRow(), token.getCol()));
        }
        if (entry.getKind() != type) {
            throw new RuntimeException(String.format("Must be %s, %s:%s", type, token.getRow(), token.getCol()));
        }
    }

    /**
     * 获取运算符优先级
     * @param symbol
     * @return
     */
    private int getPriority(Symbol symbol) {
        if (SYM_POUND.equals(symbol)) {
            return 0;
        } else if (SYM_LEFT_BRACKETS.equals(symbol)) {
            return 1;
        } else if (SYM_PLUS.equals(symbol)) {
            return 2;
        } else if (SYM_SUB.equals(symbol)) {
            return 2;
        } else if (SYM_MUL.equals(symbol)) {
            return 4;
        } else if (SYM_DIV.equals(symbol)) {
            return 4;
        } else if (SYM_RIGHT_BRACKETS.equals(symbol)) {
            return 6;
        } else {
            throw new RuntimeException("Unsupported Operator");
        }
    }

    /**
     * 运算栈中消一项
     */
    private void back() {
        Symbol symbol = symbolStack.pop();

        if (SYM_PLUS.equals(symbol)) {
            putInstruction(new Instruction(OPR, 0, 1));
        } else if (SYM_SUB.equals(symbol)) {
            putInstruction(new Instruction(OPR, 0, 2));
        } else if (SYM_MUL.equals(symbol)) {
            putInstruction(new Instruction(OPR, 0, 3));
        } else if (SYM_DIV.equals(symbol)) {
            putInstruction(new Instruction(OPR, 0, 4));
        }
    }
    
    private void putInstruction(Instruction instruction) {
        if (debugEnable) {
            System.out.println(instruction);
        }
        instructions.add(instruction);
    }

    /**
     * 清空运算栈
     */
    private void clear() {
        while (symbolStack.size() != 1) {
            back();
        }
    }

    /**
     * 从引用表中获得对应项
     * @param token
     * @return
     */
    private RefEntry getRef(Token token) {
        return getRef(token.getId());
    }

    /**
     * 从引用表中获得对应项
     * @param name
     * @return
     */
    private RefEntry getRef(String name) {
        RefEntry entry = refTable.get(name);
        if (entry == null) {
            throw new RuntimeException("Undefined Identity");
        }
        return entry;
    }

    /**
     * 逆波兰项入栈
     * @param token
     */
    private void enterStack(Token token) {
        Symbol symbol = token.getSymbol();
        if (SYM_IDENT.equals(symbol)) {
            RefEntry entry = getRef(token);
            if (RefType.CONST.equals(entry.getKind())) {
                putInstruction(new Instruction(LIT, 0, entry.getAdr())); // 常量
            } else {
                putInstruction(new Instruction(LOD, level - entry.getLevel(), entry.getAdr())); // 标识符
            }
        } else if (SYM_NUMBER.equals(symbol)) {
            putInstruction(new Instruction(LIT, 0, Integer.parseInt(token.getId()))); // 字面量
        } else if (SYM_PLUS.equals(symbol) || SYM_SUB.equals(symbol) || SYM_MUL.equals(symbol)
            || SYM_DIV.equals(symbol) || SYM_LEFT_BRACKETS.equals(symbol) || SYM_RIGHT_BRACKETS.equals(symbol)) {
            if (SYM_RIGHT_BRACKETS.equals(symbol)) { // 如果是右括号，则出栈，直到遇见左括号
                while (!symbolStack.peek().equals(SYM_LEFT_BRACKETS)) {
                    back();
                }
                symbolStack.pop();
            } else {
                if (getPriority(symbol) <= getPriority(symbolStack.peek())) {
                    back();
                }
                symbolStack.push(symbol);
            }
        } else {
            throw new RuntimeException("Unsupported operator");
        }

    }

    /**
     * 递归构造目录树之前的保存工作
     * @param name
     */
    private void swap(String name) {
        Node subNode = new Node(node, name);
        this.node.appendChild(subNode);
        this.node = subNode;
    }

    /**
     * 追加子节点
     * @param name
     */
    private void appendChild(String name) {
        node.appendChild(new Node(node, name));
    }

}
