package com.mycompany.app.model;

import java.util.ArrayList;
import java.util.HashMap;

public class CFGNode {

    private String labelNumber;
    private String lineNumber;

    private HashMap<CFGNode, String> nextNodes = new HashMap<>();
    private String opcode;



    public CFGNode(String labelNumber, String lineNumber,HashMap<CFGNode, String> nextNodes, String opcode) {
        this.labelNumber = labelNumber;
        this.lineNumber = lineNumber;
        this.nextNodes = nextNodes;
        this.opcode = opcode;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public CFGNode() {
    }

    public String getLabelNumber() {
        return labelNumber;
    }

    public void setLabelNumber(String labelNumber) {
        this.labelNumber = labelNumber;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public HashMap<CFGNode, String> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(HashMap<CFGNode, String> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public void addNextNodes(CFGNode cfgNode, String branchName) {
        this.nextNodes.put(cfgNode, branchName);
    }
}
