package com.mycompany.app;

import com.mycompany.app.model.CFGNode;
import org.objectweb.asm.ClassReader;

import org.objectweb.asm.tree.*;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import org.objectweb.asm.tree.analysis.BasicVerifier;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import java.io.*;
import java.util.stream.Collectors;


public class App
{
    public static void main( String[] args )
    {
        trialCFG();
    }

    public static <Frame> void trialCFG(){
        InputStream in= App.class.getResourceAsStream("TestData.class");

        ClassReader classReader= null;
        try {
            classReader = new ClassReader(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        int methodNumber = 8;
        List<String> methodInstructions = new ArrayList<>();
        List<AbstractInsnNode> instructionsInMethod =  new ArrayList<>();

        for (MethodNode method: classNode.methods.subList(methodNumber, classNode.methods.size())){

            final Analyzer a = new Analyzer(new BasicVerifier());
            try { 
                a.analyze(classNode.name, method);
                Frame[] frames = (Frame[]) a.getFrames();

                AbstractInsnNode[] insnNodes = method.instructions.toArray();

                for (int i = 0; i<insnNodes.length; i++ ){
                    // first 2 instructions are beginning
                    AbstractInsnNode node = insnNodes[i];
                    String nodeName = node.getClass().getSimpleName();


                    instructionsInMethod.add(insnNodes[i]);
                    methodInstructions.add(insnNodes[i].toString());

                }

                HashMap<Integer, ArrayList<String>> myMap =  seperateToArrays(methodInstructions);
                HashMap<Integer, ArrayList<AbstractInsnNode>> myMap2 =  seperateToLabelArrays(instructionsInMethod);
                ArrayList<CFGNode> cfgNodes = getAllNodes(myMap2);
                setNextNodesOfCFGNodeList(cfgNodes, myMap2);
                buildGraph(cfgNodes);


            } catch (final AnalyzerException e) {
                System.err.println("// error in method " + classNode.name + "." + method.name
                        + method.desc + ":" + e);
            }
            break;
        }
    }

    public static HashMap<Integer, ArrayList<AbstractInsnNode>> seperateToLabelArrays(List<AbstractInsnNode> instructions){
        HashMap<Integer, ArrayList<AbstractInsnNode>> instructionMap = new HashMap<>();

        int index = 0;
        ArrayList<AbstractInsnNode> currentValues = new ArrayList<>();
        currentValues.add(instructions.get(0));
        for(AbstractInsnNode insnNode : instructions.subList(1, instructions.size())){

            if (insnNode.getClass().getSimpleName().contains("LabelNode")){
                instructionMap.put(index, currentValues);
                currentValues = new ArrayList<>();
                index++;
            }
            currentValues.add(insnNode);
        }
        return instructionMap;
    }

    private static void buildGraph(ArrayList<CFGNode> cfgNodes){
        for (CFGNode node : cfgNodes){
            for (HashMap.Entry<CFGNode,String> entry : node.getNextNodes().entrySet()){
                CFGNode nextNode = entry.getKey();
                String branchName = entry.getValue();
                System.out.println("\""+node.getLineNumber() + " " + node.getLabelNumber()+"\"" +" -> "+"\""+nextNode.getLineNumber() + " " +  nextNode.getLabelNumber()+"\" " + "[label=\""+branchName+"\"]");
            }
        }
    }


    private static CFGNode generateNode(ArrayList<AbstractInsnNode> instructions){

        boolean containsLineNumberNode = instructions.stream().anyMatch(n -> n.getClass().getSimpleName().equals("LineNumberNode"));
        if (containsLineNumberNode){
            CFGNode node = new CFGNode();
            for(AbstractInsnNode insnNode : instructions){
                System.out.println(insnNode + " " + insnNode.getOpcode());
                if (insnNode.getClass().getSimpleName().equals("LabelNode")){
                    // set the label name
                    LabelNode lbl = (LabelNode) insnNode;
                    node.setLabelNumber(lbl.getLabel().toString());
                } else if(insnNode.getClass().getSimpleName().equals("LineNumberNode")){
                    LineNumberNode lineNumberNode = (LineNumberNode) insnNode;
                    node.setLineNumber(Integer.toString(lineNumberNode.line));
                } else if (insnNode.getClass().getSimpleName().equals("JumpInsnNode")){
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) insnNode;
                    node.setOpcode(Integer.toString(jumpInsnNode.getOpcode()));
                }
            }
            return node;
        }
        return null;
    }

    private static ArrayList<CFGNode> getAllNodes(HashMap<Integer, ArrayList<AbstractInsnNode>> mapOfInstructions){
        ArrayList<CFGNode> listOfNodes = new ArrayList<>();
        for (int i = 0; i < mapOfInstructions.size(); i++){
            ArrayList<AbstractInsnNode> insnNodes = mapOfInstructions.get(i);
            CFGNode node = generateNode(insnNodes);
            if (node != null){
                listOfNodes.add(node);
            }

        }
        return listOfNodes;
    }

    private static void setNextNodesOfCFGNodeList(ArrayList<CFGNode> cfgNodeList, HashMap<Integer, ArrayList<AbstractInsnNode>> mapOfInstructions){

        for(int i = 0; i<cfgNodeList.size(); i++){
            CFGNode currentNode = cfgNodeList.get(i);
            ArrayList<AbstractInsnNode> instructionsForNode = mapOfInstructions.get(i);
            boolean containsGoto = false;
            boolean containsIfNode = false;
            List<AbstractInsnNode> jumpAbsInsnNodes = instructionsForNode.stream().filter(node -> node.getClass().getSimpleName().equals("JumpInsnNode")).collect(Collectors.toList());
            if (!jumpAbsInsnNodes.isEmpty()){
                for (AbstractInsnNode jumpNode : jumpAbsInsnNodes){
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) jumpNode;
                    if (jumpInsnNode.getOpcode() == 167){
                        containsGoto = true;
                    } else {
                        containsIfNode = true;
                    }
                    Optional<CFGNode> cfgNextNode = cfgNodeList.stream().filter(n -> n.getLabelNumber().equals(jumpInsnNode.label.getLabel().toString())).findFirst();
                    if (cfgNextNode.isPresent()){
                        CFGNode node = cfgNextNode.get();

                        if(containsGoto){
                            currentNode.addNextNodes(node, "true");
                        }

                        if(containsIfNode){
                            currentNode.addNextNodes(node, "false");
                        }

                    }
                }


            }
            if (!containsGoto && i < cfgNodeList.size()-1){
                CFGNode node = cfgNodeList.get(i+1);
                currentNode.addNextNodes(node, "");
            }

        }
    }

    public static HashMap<Integer, ArrayList<String>> seperateToArrays(List<String> instructions){
        HashMap<Integer, ArrayList<String>> instructionMap = new HashMap<>();
        ArrayList<String> currentValues = new ArrayList<>();
        int index = 0;
        for (String ins : instructions){
            currentValues.add(ins);
            if (ins.contains("LineNumberNode")){
                instructionMap.put(index, currentValues);
                currentValues = new ArrayList<>();
                index++;
            }
        }
        return instructionMap;
    }

    public static ArrayList<ArrayList<String>> readTestData(){
        ArrayList<ArrayList<String>> methods = new ArrayList<>();
        try {
            File code = new File("/Users/gizemayas/Desktop/cfg/my-app/src/hw1testdata.txt");
            Scanner myReader = new Scanner(code);
            ArrayList<String> methodLines = new ArrayList<>();

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if(data.contains("public")) {
                    if (methodLines.size() > 0) {
                        methods.add(methodLines);
                        methodLines = new ArrayList<>();
                    }
                }
                data=data.replace(" ","");
                if (data.length() !=0 && !data.equalsIgnoreCase("}") && !data.contains("else")){
                    methodLines.add(data);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return methods;
    }


}